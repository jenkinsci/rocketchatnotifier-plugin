package jenkins.plugins.rocketchatnotifier.rocket.expectations;

import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import static org.mockserver.model.HttpResponse.response;

public class LoginExpectationCallback implements ExpectationResponseCallback {

  public HttpResponse handle(HttpRequest httpRequest) {
    return response()
      .withBody("{\"success\":true,\"data\":{\"authToken\":\"\",\"userId\":\"\"}}")
      .withStatusCode(200);
  }
}
