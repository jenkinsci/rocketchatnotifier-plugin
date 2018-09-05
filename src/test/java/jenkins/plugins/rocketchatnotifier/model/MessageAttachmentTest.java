package jenkins.plugins.rocketchatnotifier.model;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, MessageAttachment.class})
public class MessageAttachmentTest {

  @Mock
  private Jenkins jenkins;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    PowerMockito.mockStatic(Jenkins.class);
    PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins);

  }

  @Test
  public void fromJSONWithAllFields() {
    MessageAttachment messageAttachment = new MessageAttachment("test");
    messageAttachment.setColor("color");
    messageAttachment.setText("text");
    messageAttachment.setThumbUrl("thumbUrl");
    messageAttachment.setMessageLink("messageLink");
    messageAttachment.setCollapsed(true);
    messageAttachment.setAuthorName("AuthorName");
    messageAttachment.setAuthorIcon("AuthorIcon");
    messageAttachment.setAuthorLink("AuthorLink");
    messageAttachment.setTitleLink("titleLink");
    messageAttachment.setTitleLinkDownload(false);
    messageAttachment.setImageUrl("imageUrl");
    messageAttachment.setAudioUrl("audioUrl");
    messageAttachment.setVideoUrl("videoUrl");
    assertThat(MessageAttachment.fromJSON(JSONObject.fromObject(messageAttachment)), is(equalTo(messageAttachment)));
  }

  @Test
  public void fromJSONWithRequiredFields() {
    MessageAttachment messageAttachment = new MessageAttachment("test");
    assertThat(MessageAttachment.fromJSON(JSONObject.fromObject(messageAttachment)), is(equalTo(messageAttachment)));
  }
}
