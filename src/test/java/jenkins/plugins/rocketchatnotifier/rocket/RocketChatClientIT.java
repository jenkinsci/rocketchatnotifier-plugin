package jenkins.plugins.rocketchatnotifier.rocket;

import jenkins.plugins.rocketchatnotifier.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by mreinhardt on 26.12.16.
 */
public class RocketChatClientIT {

  private RocketChatClient client;

  @BeforeEach
  public void setup() throws Exception {
    this.client = new RocketChatClientImpl("http://localhost:44443/api/", false, "admin", "supersecret");
  }

  @Test
  public void shouldReadRooms() throws Exception {
    Room[] rooms = this.client.getChannels();
    assertThat(rooms.length, is(1));
  }

  @Test
  public void shouldSendMessage() throws Exception {
    this.client.send("general", "test");
  }

  @Test
  public void shouldSendMessageAndEmojiAndAvatar() throws Exception {
    this.client.send("general", "test", ":sod:",
      "https://talks.bitexpert.de/zendcon16-jenkins-for-php-projects/images/jenkins.png");
  }

  @Test
  public void shouldSendMessageWithAttachment() throws Exception {
    List<Map<String, Object>> attachments = new ArrayList<Map<String, Object>>();
    Map<String, Object> attachment = new HashMap<String, Object>();
    attachment.put("color", "green");
    attachment.put("text", "Ok les gars");
    attachment.put("collapsed", Boolean.TRUE);
    attachments.add(attachment);
    this.client.send("general", "test", null, null, attachments);
  }

}
