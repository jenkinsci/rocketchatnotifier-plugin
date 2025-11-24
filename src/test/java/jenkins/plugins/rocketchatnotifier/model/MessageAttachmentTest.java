package jenkins.plugins.rocketchatnotifier.model;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MessageAttachmentTest {

  @Mock
  private Jenkins jenkins;

  private final List<AutoCloseable> closeableList = new ArrayList<>();

  @Before
  public void setup() throws Exception {
    closeableList.add(MockitoAnnotations.openMocks(this));

    MockedStatic<Jenkins> jenkinsMockedStatic = Mockito.mockStatic(Jenkins.class);
    jenkinsMockedStatic.when(Jenkins::get).thenReturn(jenkins);
    closeableList.add(jenkinsMockedStatic);
  }

  @After
  public void tearDown() throws Exception {
    for (AutoCloseable autoCloseable : closeableList) {
      if (autoCloseable != null) {
        autoCloseable.close();
      }
    }
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
