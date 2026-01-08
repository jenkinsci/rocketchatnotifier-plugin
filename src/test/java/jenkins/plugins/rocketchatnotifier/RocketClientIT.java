package jenkins.plugins.rocketchatnotifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by mreinhardt on 27.12.16.
 */
public class RocketClientIT {

  private RocketClient client;

  @BeforeEach
  public void setup() throws Exception {
    this.client = new RocketClientImpl("http://localhost:44443/", false, "admin", "supersecret",
      "general"); // TODO read from env
  }

  @Test
  public void shouldSendMessage() throws Exception {
    this.client.publish("test", null);
  }

  @Test
  public void shouldNotFailWhenSendMessageWithEmptyList() throws Exception {
    this.client.publish("test", ":sob:", null, new ArrayList<Map<String, Object>>());
  }

}
