package jenkins.plugins.rocketchatnotifier.rocket;

import jenkins.plugins.rocketchatnotifier.model.Room;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.netty.MockServer;

import static org.mockserver.model.HttpClassCallback.callback;
import static org.mockserver.model.HttpRequest.request;


public class RocketChatClientImplAcceptanceTest {

  private static MockServer mockServer;
  private static MockServerClient mockServerClient;

  @BeforeClass
  public static void startServer() {
    mockServer = new MockServer(1080);
    mockServerClient = new MockServerClient("localhost", mockServer.getLocalPort());

    mockServerClient.when(
      request().withPath("/api/v1/info")
    ).respond(
      callback().withCallbackClass("jenkins.plugins.rocketchatnotifier.rocket.expectations.InfoExpectationCallback")
    );
    mockServerClient.when(
      request().withPath("/api/v1/login")
    ).respond(
      callback().withCallbackClass("jenkins.plugins.rocketchatnotifier.rocket.expectations.LoginExpectationCallback")
    );
    mockServerClient.when(
      request().withPath("/api/v1/chat.postMessage")
    ).respond(
      callback().withCallbackClass("jenkins.plugins.rocketchatnotifier.rocket.expectations.MessageExpectationCallback")
    );
  }

  @AfterClass
  public static void stopServer() {
    mockServer.stop();
    mockServerClient.stop();
  }

  @Test(expected = RocketClientException.class)
  public void shouldFailWithSSLError() throws Exception {
    // given
    final RocketChatClientImpl rocketChatClient = new RocketChatClientImpl("127.0.0.1:1080", false, "", "");
    final Room room = new Room();
    room.setName("room");
    // when
    rocketChatClient.send(room, "message");
    // then no error
  }

  @Test
  public void shouldSendMessageToRoom() throws Exception {
    // given
    final RocketChatClientImpl rocketChatClient = new RocketChatClientImpl("127.0.0.1:1080", true, "", "");
    final Room room = new Room();
    room.setName("room");
    // when
    rocketChatClient.send(room, "message");
    // then no error
  }


}
