package jenkins.plugins.rocketchatnotifier;

import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class RocketChatNotifierDecriptorImplTest {

  @Mock
  private Jenkins jenkins;

  @SuppressWarnings("rawtypes")
  @Mock
  private StaplerRequest staplerRequest;

  private final List<AutoCloseable> closeableList = new ArrayList<>();

  private Descriptor descriptor;

  @BeforeEach
  public void setup() throws Exception {
    closeableList.add(MockitoAnnotations.openMocks(this));

    MockedStatic<Jenkins> jenkinsMockedStatic = Mockito.mockStatic(Jenkins.class);
    jenkinsMockedStatic.when(Jenkins::get).thenReturn(jenkins);
    closeableList.add(jenkinsMockedStatic);

    File rootPath = new File(System.getProperty("java.io.tmpdir"));
    when(jenkins.getRootDir()).thenReturn(rootPath);
    when(staplerRequest.getParameter("buildServerUrl")).thenReturn("jenkins.example.com");
    descriptor = new RocketChatNotifier.DescriptorImpl();
  }

  @AfterEach
  public void tearDown() throws Exception {
    for (AutoCloseable autoCloseable : closeableList) {
      if (autoCloseable != null) {
        autoCloseable.close();
      }
    }
  }


  @Test
  public void shouldWorkWithEmptyFormData() throws Exception {
    // given
    // when
    boolean result = descriptor.configure(staplerRequest, null);
    // then
    assertThat(result, is(true));
  }

  @Test
  public void shouldHandleNotExistingFormData() throws Exception {
    // given
    // when
    boolean result = descriptor.configure(staplerRequest, new JSONObject());
    // then
    assertThat(result, is(true));
  }
}
