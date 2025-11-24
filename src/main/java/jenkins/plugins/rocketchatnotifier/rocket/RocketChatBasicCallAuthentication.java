package jenkins.plugins.rocketchatnotifier.rocket;

import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import kong.unirest.HttpRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

/**
 * Authentication using username/ password
 */
public class RocketChatBasicCallAuthentication implements RocketChatCallAuthentication {
  private final String serverUrl;
  private final String user;
  private final String password;
  private String authToken = "";
  private String userId = "";

  public RocketChatBasicCallAuthentication(String serverUrl, String user, String password) {
    super();
    if (!serverUrl.endsWith("/")) {
      serverUrl += "/";
    }
    if (!serverUrl.startsWith("http")) {
      serverUrl = "https://" + serverUrl;
    }
    if (!serverUrl.endsWith("api/")) {
      this.serverUrl = serverUrl + "api/";
    } else {
      this.serverUrl = serverUrl;
    }
    this.user = user;
    this.password = password;
  }

  @Override
  public boolean isAuthenticated() {
    return !authToken.isEmpty() && !userId.isEmpty();
  }

  @Override
  public void doAuthentication() throws RocketClientException {
    HttpResponse<JsonNode> loginResult;
    String apiURL = serverUrl + "v1/login";
    String userInfoUrl = serverUrl + "v1/me";
    try {
      loginResult = Unirest.post(apiURL).field("user", user).field("password", password).asJson();
    } catch (UnirestException e) {
      throw new RocketClientException("Please check if the server API " + apiURL + " is correct: (Login-Error 1)", e);
    }

    if (loginResult.getStatus() == 401) {
      // try via token
      try {
        loginResult = Unirest.get(userInfoUrl).header("X-User-Id", user).header("X-Auth-Token", password).asJson();
        if (loginResult.getStatus() == 401) {
          throw new RocketClientException("The username and password provided are incorrect.");
        }
        this.userId = user;
        this.authToken = password;
      } catch (UnirestException e) {
        // TODO more logging
        throw new RocketClientException("Please check if the server API " + apiURL + " is correct (Login-Error 2)", e);
      }
    } else {
      JSONObject data = loginResult.getBody().getObject().getJSONObject("data");
      this.userId = data.getString("userId");
      this.authToken = data.getString("authToken");
    }

    if (loginResult.getStatus() != 200) {
      throw new RocketClientException("The login failed with a result of (Login-Error 3): " + loginResult.getStatus());
    }
  }

  @Override
  public String getUrlForRequest(RocketChatRestApiV1 call) {
    // /api/info should be used without prefix, see https://rocket.chat/docs/developer-guides/rest-api/miscellaneous/info/
    if (call.getMethodName().contains("/info")) {
      return serverUrl + "info";
    } else {
      return serverUrl + call.getMethodName();
    }
  }

  @Override
  public void addAuthenticationDataToRequest(HttpRequest request) {
    request.header("X-Auth-Token", authToken);
    request.header("X-User-Id", userId);
  }
}
