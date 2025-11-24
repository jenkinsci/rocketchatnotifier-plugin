package jenkins.plugins.rocketchatnotifier.rocket;

import jenkins.plugins.rocketchatnotifier.model.Room;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.model.HttpClassCallback.callback;
import static org.mockserver.model.HttpRequest.request;


public class RocketChatClientImplAcceptanceTest {

  private static ClientAndServer mockServer;

  @BeforeClass
  public static void startServer() {
    mockServer = ClientAndServer.startClientAndServer(1080);
    mockServer.when(
      request().withPath("/api/v1/info")
    ).respond(
      callback().withCallbackClass("jenkins.plugins.rocketchatnotifier.rocket.expectations.InfoExpectationCallback")
    );
    mockServer.when(
      request().withPath("/api/v1/login")
    ).respond(
      callback().withCallbackClass("jenkins.plugins.rocketchatnotifier.rocket.expectations.LoginExpectationCallback")
    );
    mockServer.when(
      request().withPath("/api/v1/chat.postMessage")
    ).respond(
      callback().withCallbackClass("jenkins.plugins.rocketchatnotifier.rocket.expectations.MessageExpectationCallback")
    );
  }

  @AfterClass
  public static void stopServer() {
    mockServer.stop();
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
