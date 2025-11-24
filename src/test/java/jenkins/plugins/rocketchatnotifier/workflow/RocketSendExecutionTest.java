package jenkins.plugins.rocketchatnotifier.workflow;

import jenkins.plugins.rocketchatnotifier.model.MessageAttachment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ben on 08/02/2017.
 */
public class RocketSendExecutionTest {

  @Test
  public void convertMessageAttachmentsShouldConvertCamelCaseToUnderscore() {
    List<MessageAttachment> attachments = new ArrayList<MessageAttachment>();
    MessageAttachment messageAttachment = new MessageAttachment("title");
    messageAttachment.setAuthorName("John Doe");
    attachments.add(messageAttachment);
    List<Map<String, Object>> maps = MessageAttachment.convertMessageAttachmentsToMaps(attachments);

    Assertions.assertNotNull(maps);
    Assertions.assertTrue(maps.get(0).containsKey("author_name"));

  }

  @Test
  public void convertMessageAttachmentsShouldNotConvertNullPropertie() {
    List<MessageAttachment> attachments = new ArrayList<MessageAttachment>();
    MessageAttachment messageAttachment = new MessageAttachment("title");
    attachments.add(messageAttachment);
    List<Map<String, Object>> maps = MessageAttachment.convertMessageAttachmentsToMaps(attachments);

    Assertions.assertNotNull(maps);
    Assertions.assertFalse(maps.get(0).containsKey("author_name"));
  }

  @Test
  public void convertMessageAttachmentsShouldConvertAllMaps() {
    RocketSendStep.RocketSendStepExecution rocketSendStepExecution = new RocketSendStep.RocketSendStepExecution();
    List<MessageAttachment> attachments = new ArrayList<MessageAttachment>();
    MessageAttachment messageAttachment = new MessageAttachment("title");
    messageAttachment.setText("yolo");
    attachments.add(messageAttachment);

    MessageAttachment messageAttachment2 = new MessageAttachment("title");
    messageAttachment2.setMessageLink("http://github.com");
    attachments.add(messageAttachment2);
    List<Map<String, Object>> maps = MessageAttachment.convertMessageAttachmentsToMaps(attachments);

    Assertions.assertNotNull(maps);
    Assertions.assertEquals(2, maps.size());
    Assertions.assertEquals("yolo", maps.get(0).get("text"));
    Assertions.assertEquals("http://github.com", maps.get(1).get("message_link"));

  }

}
