package jenkins.plugins.rocketchatnotifier.rocket;

import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import kong.unirest.HttpRequest;

public class RocketChatWebhookAuthentication implements RocketChatCallAuthentication {
  private static final String HOOKS_PATH = "hooks/";

  private String serverUrl;
  private String webhookUrl;

  public RocketChatWebhookAuthentication(String serverUrl, String webhookToken) {
    super();
    this.serverUrl = serverUrl + (serverUrl.endsWith("/") ? "" : "/");
    this.webhookUrl = webhookToken.contains(HOOKS_PATH) ? webhookToken : (this.serverUrl + HOOKS_PATH + webhookToken);
  }

  @Override
  public boolean isAuthenticated() {
    // No authentication needed
    return true;
  }

  @Override
  public void doAuthentication() throws RocketClientException {
    // No authentication needed
  }

  @Override
  public String getUrlForRequest(RocketChatRestApiV1 call) {
    switch (call) {
      case Info:
        return serverUrl + "/api/" + call.getMethodName();
      case PostMessage:
        return webhookUrl;
      default:
        throw new UnsupportedOperationException("Unable to handle request " + call.name() + " using webhook");
    }
  }

  @Override
  public void addAuthenticationDataToRequest(HttpRequest request) {
    // No authentication needed
  }
}
