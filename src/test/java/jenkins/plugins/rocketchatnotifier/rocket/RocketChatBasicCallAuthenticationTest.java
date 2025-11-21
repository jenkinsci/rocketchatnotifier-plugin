package jenkins.plugins.rocketchatnotifier.rocket;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Unirest.class })
public class RocketChatBasicCallAuthenticationTest {


  @Before
  public void setup() {
    PowerMockito.mockStatic(Unirest.class);
  }

  @Test
  public void shouldTryTokenBasedAuthToo() throws Exception {
    prepareMock(401,200);
    RocketChatBasicCallAuthentication chatBasicCallAuthentication = new RocketChatBasicCallAuthentication("example.com/", "a", "b");
    assertThat(chatBasicCallAuthentication.getUrlForRequest(RocketChatRestApiV1.ChannelsList), is(equalTo("https://example.com/api/v1/channels.list")));
  }

  @Test
  public void shouldNotAppendSlashToRootUrlIfAlreadyGiven() throws Exception {
    RocketChatBasicCallAuthentication chatBasicCallAuthentication = new RocketChatBasicCallAuthentication("example.com/", "a", "b");
    String sampleCall = chatBasicCallAuthentication.getUrlForRequest(RocketChatRestApiV1.ChannelsList);
    assertThat(sampleCall, is(equalTo("https://example.com/api/v1/channels.list")));
  }

  @Test
  public void shouldAppendApiPathIfNotGiven() throws Exception {
    RocketChatBasicCallAuthentication chatBasicCallAuthentication = new RocketChatBasicCallAuthentication("example.com", "a", "b");
    String sampleCall = chatBasicCallAuthentication.getUrlForRequest(RocketChatRestApiV1.ChannelsList);
    assertThat(sampleCall, is(equalTo("https://example.com/api/v1/channels.list")));
  }

  @Test
  public void shouldNotAppendApiPathIfiven() throws Exception {
    RocketChatBasicCallAuthentication chatBasicCallAuthentication = new RocketChatBasicCallAuthentication("example.com/api", "a", "b");
    String sampleCall = chatBasicCallAuthentication.getUrlForRequest(RocketChatRestApiV1.ChannelsList);
    assertThat(sampleCall, is(equalTo("https://example.com/api/v1/channels.list")));
  }

  @Test(expected = RocketClientException.class)
  public void shouldAutoPrefixWithHttpsIfNotGiven() throws Exception {
    prepareMock(401,401);
    new RocketChatBasicCallAuthentication("example.com", "a", "b").doAuthentication();
  }

  private void prepareMock(int desiredPostStatusReturn, int desiredGetStatusReturn) throws UnirestException {
    HttpResponse<JsonNode> response = mock(HttpResponse.class);
    when(response.getStatus()).thenReturn(desiredPostStatusReturn);
    HttpRequestWithBody request = mock(HttpRequestWithBody.class);
    MultipartBody body = mock(MultipartBody.class);
    when(request.field(anyString(),anyString())).thenReturn(body);
    when(body.asJson()).thenReturn(response);
    when(body.field(anyString(),anyString())).thenReturn(body);
    when(Unirest.post( "https://example.com/api/v1/login")).thenReturn(request);
    GetRequest getRequest = mock(GetRequest.class);
    HttpResponse<JsonNode> getResponse = mock(HttpResponse.class);
    when(Unirest.get( "https://example.com/api/v1/me")).thenReturn(getRequest);
    when(getRequest.header(anyString(),anyString())).thenReturn(getRequest);
    when(getResponse.getStatus()).thenReturn(desiredGetStatusReturn);
    when(getRequest.asJson()).thenReturn(getResponse);
  }
}
