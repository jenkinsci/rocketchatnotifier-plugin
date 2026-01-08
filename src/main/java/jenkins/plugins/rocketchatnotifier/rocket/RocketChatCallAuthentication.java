package jenkins.plugins.rocketchatnotifier.rocket;

import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import kong.unirest.HttpRequest;

public interface RocketChatCallAuthentication {

  boolean isAuthenticated();

  void doAuthentication() throws RocketClientException;

  String getUrlForRequest(RocketChatRestApiV1 call);

  void addAuthenticationDataToRequest(HttpRequest request);
}
