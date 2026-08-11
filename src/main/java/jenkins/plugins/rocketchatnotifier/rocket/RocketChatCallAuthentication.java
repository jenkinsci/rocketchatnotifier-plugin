package jenkins.plugins.rocketchatnotifier.rocket;

import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import kong.unirest.HttpRequest;
import kong.unirest.UnirestInstance;

public interface RocketChatCallAuthentication {

  boolean isAuthenticated();

  /**
   * @param unirest the Unirest instance configured (trustSSL, proxy, ...) for the target server;
   *                implementations that need to make their own HTTP calls (e.g. a login request)
   *                must use this instance rather than the shared static {@link kong.unirest.Unirest}.
   */
  void authenticate(UnirestInstance unirest) throws RocketClientException;

  String getUrlForRequest(RocketChatRestApiV1 call);

  void addAuthenticationDataToRequest(HttpRequest request);
}
