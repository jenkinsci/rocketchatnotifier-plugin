package jenkins.plugins.rocketchatnotifier;

import hudson.model.Descriptor;
import hudson.model.ItemGroup;
import hudson.model.listeners.SaveableListener;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, SaveableListener.class, RocketChatNotifier.DescriptorImpl.class})
public class RocketChatNotifierDecriptorImplTest {

  @Mock
  private Jenkins jenkins;

  @SuppressWarnings("rawtypes")
  @Mock
  private StaplerRequest staplerRequest;

  private Descriptor descriptor;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    PowerMockito.mockStatic(Jenkins.class);
    PowerMockito.mockStatic(SaveableListener.class);
    PowerMockito.when(Jenkins.getInstanceOrNull()).thenReturn(jenkins);
    PowerMockito.when(Jenkins.get()).thenReturn(jenkins);
    File rootPath = new File(System.getProperty("java.io.tmpdir"));
    when(jenkins.getRootDir()).thenReturn(rootPath);
    when(staplerRequest.getParameter("buildServerUrl")).thenReturn("jenkins.example.com");
    descriptor = new RocketChatNotifier.DescriptorImpl();
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
