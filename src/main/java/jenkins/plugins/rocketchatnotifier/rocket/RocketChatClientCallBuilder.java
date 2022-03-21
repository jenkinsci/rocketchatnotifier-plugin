package jenkins.plugins.rocketchatnotifier.rocket;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import jenkins.plugins.rocketchatnotifier.model.Response;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import jenkins.plugins.rocketchatnotifier.utils.NetworkUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * The call builder for the {@link RocketChatClient} and is only supposed to be
 * used internally.
 *
 * @author Bradley Hilton (graywolf336)
 * @version 0.0.1
 * @since 0.1.0
 */
public class RocketChatClientCallBuilder {

  private static final Logger logger = Logger.getLogger(RocketChatClientCallBuilder.class.getName());

  private String serverUrl;

  private final ObjectMapper objectMapper;

  private final RocketChatCallAuthentication authentication;

  protected RocketChatClientCallBuilder(String serverUrl, boolean trustSSL, String user, String password) throws RocketClientException {
    this(new RocketChatBasicCallAuthentication(serverUrl, user, password), serverUrl, trustSSL);
    this.serverUrl = serverUrl;
  }

  protected RocketChatClientCallBuilder(String serverUrl, boolean trustSSL, String webhookToken) throws RocketClientException {
    this(new RocketChatWebhookAuthentication(serverUrl, webhookToken), serverUrl, trustSSL);
    this.serverUrl = serverUrl;
  }

  protected RocketChatClientCallBuilder(RocketChatCallAuthentication authentication, String serverUrl,
                                        boolean trustSSL) throws RocketClientException {
    this.authentication = authentication;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    this.serverUrl = serverUrl;

    try {
      Unirest.setHttpClient(createHttpClient(serverUrl, trustSSL));
    } catch (Exception e) {
      throw new RocketClientException(e);
    }
  }

  protected Response buildCall(RocketChatRestApiV1 call) throws RocketClientException {
    return this.buildCall(call, null, null);
  }

  protected Response buildCall(RocketChatRestApiV1 call, RocketChatQueryParams queryParams) throws RocketClientException {
    return this.buildCall(call, queryParams, null);
  }

  protected Response buildCall(RocketChatRestApiV1 call, RocketChatQueryParams queryParams, Object body)
    throws RocketClientException {
    if (call.requiresAuth() && !authentication.isAuthenticated()) {
      authentication.doAuthentication();
    }

    switch (call.getHttpMethod()) {
      case GET:
        return this.buildGetCall(call, queryParams);
      case POST:
        return this.buildPostCall(call, queryParams, body);
      default:
        throw new RocketClientException("Http Method " + call.getHttpMethod().toString() + " is not supported.");
    }
  }

  private Response buildGetCall(RocketChatRestApiV1 call, RocketChatQueryParams queryParams) throws RocketClientException {
    GetRequest req = Unirest.get(authentication.getUrlForRequest(call));

    if (call.requiresAuth()) {
      authentication.addAuthenticationDataToRequest(req);
    }

    if (queryParams != null && !queryParams.isEmpty()) {
      for (Entry<? extends String, ? extends String> e : queryParams.get().entrySet()) {
        req.queryString(e.getKey(), e.getValue());
      }
    }

    try {
      return objectMapper.readValue(req.asString().getBody(), Response.class);
    } catch (Exception e) {
      throw new RocketClientException(e);
    }
  }

  private Response buildPostCall(RocketChatRestApiV1 call, RocketChatQueryParams queryParams, Object body)
    throws RocketClientException {
    HttpRequestWithBody req = Unirest.post(authentication.getUrlForRequest(call)).header("Content-Type",
      "application/json");

    if (call.requiresAuth()) {
      authentication.addAuthenticationDataToRequest(req);
    }

    if (queryParams != null && !queryParams.isEmpty()) {
      for (Entry<? extends String, ? extends String> e : queryParams.get().entrySet()) {
        req.queryString(e.getKey(), e.getValue());
      }
    }

    try {

      if (body != null) {
        req.body(objectMapper.writeValueAsString(body));
      }
      HttpResponse<String> res = req.asString();

      return objectMapper.readValue(res.getBody(), Response.class);
    } catch (Exception e) {
      throw new RocketClientException(e);
    }
  }

  private static HttpClient createHttpClient(String serverUrl, boolean trustSSL) throws KeyManagementException, NoSuchAlgorithmException {
    final HttpClientBuilder httpClientBuilder = HttpClients.custom();

    setConnectionManager(httpClientBuilder, trustSSL);

    ProxyConfiguration proxyConfiguration = getProxyConfiguration(serverUrl);
    if (proxyConfiguration != null) {
      setProxy(httpClientBuilder, proxyConfiguration);
    }

    return httpClientBuilder.build();
  }

  private static void setConnectionManager(HttpClientBuilder httpClientBuilder, boolean trustSSL)
    throws NoSuchAlgorithmException, KeyManagementException {

    PoolingHttpClientConnectionManager manager;
    if (trustSSL) {
      SSLContext sslContext = createAlwaysTrustingSSLContext();

      Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        .register("https", new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
        .register("http", PlainConnectionSocketFactory.getSocketFactory())
        .build();

      manager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

    } else {
      manager = new PoolingHttpClientConnectionManager();
    }

    manager.setDefaultMaxPerRoute(20);

    httpClientBuilder.setConnectionManager(manager);
  }

  private static SSLContext createAlwaysTrustingSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext sslContext = SSLContext.getInstance("SSL");

    // set up a TrustManager that trusts everything
    sslContext.init(null, new TrustManager[]{new X509TrustManager() {
      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }

      public void checkClientTrusted(X509Certificate[] certs, String authType) {
        // intentionally left blank
      }

      public void checkServerTrusted(X509Certificate[] certs, String authType) {
        // intentionally left blank
      }
    }}, new SecureRandom());

    return sslContext;
  }

  private static ProxyConfiguration getProxyConfiguration(String serverUrl) {
    Jenkins jenkinsInstance = Jenkins.getInstanceOrNull();
    return jenkinsInstance != null && jenkinsInstance.proxy != null && !NetworkUtils.isHostOnNoProxyList(serverUrl, jenkinsInstance.proxy)
      ? jenkinsInstance.proxy
      : null;
  }

  private static void setProxy(HttpClientBuilder httpClientBuilder, ProxyConfiguration proxyConfiguration) {
    final HttpHost proxyHost = new HttpHost(proxyConfiguration.name, proxyConfiguration.port);
    final HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
    httpClientBuilder.setRoutePlanner(routePlanner);

    final String username = proxyConfiguration.getUserName();
    if (username != null && !username.trim().isEmpty()) {
      logger.info("Using proxy authentication (user=" + username + ")");

      final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(new AuthScope(proxyHost), new UsernamePasswordCredentials(username, proxyConfiguration.getPassword()));
      httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
    }
  }

  public String getServerUrl() {
    return serverUrl;
  }
}
