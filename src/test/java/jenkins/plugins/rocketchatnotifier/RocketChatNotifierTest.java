package jenkins.plugins.rocketchatnotifier;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, RocketClientImpl.class, RocketClientWebhookImpl.class, RocketChatNotifier.class})
public class RocketChatNotifierTest {

  @Mock
  private Jenkins jenkins;

  @Mock
  private RocketClientImpl rocketClient;

  @Mock
  private RocketClientWebhookImpl rocketClientWithWebhook;

  @Mock
  private AbstractBuild build;

  @Mock
  private BuildListener listener;

  RocketChatNotifier notifier;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    PowerMockito.mockStatic(Jenkins.class);
    PowerMockito.mock(RocketClientImpl.class);
    PowerMockito.mock(RocketClientWebhookImpl.class);
    PowerMockito.whenNew(RocketClientImpl.class).withAnyArguments().thenReturn(rocketClient);
    PowerMockito.whenNew(RocketClientWebhookImpl.class).withAnyArguments().thenReturn(rocketClientWithWebhook);
    PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins);
    File rootPath = new File(System.getProperty("java.io.tmpdir"));
    when(jenkins.getRootDir()).thenReturn(rootPath);
    notifier = new RocketChatNotifier(
      "rocket.example.com", false,
      "user", "password",
      "jenkins", "rocket.example.com",
      false,
      false, false, false, false, false, false, false, false, null, false, false, null, null, null, null);
  }

  @Test
  public void shouldFallbackToJenkinsUrlIfBuildServerUrlIsNotProvided() throws Exception {
    // given
    notifier.setBuildServerUrl(null);
    // when
    notifier.getBuildServerUrl();
    // then
  }

  @Test
  public void shouldProvideBuildServerUrl() throws Exception {
    // given
    // when
    String serverUrl = notifier.getBuildServerUrl();
    // then
    assertThat(serverUrl, is(not(nullValue())));
  }

  @Test
  public void shouldCreateRocketClientWithUsernameAndPassword() throws Exception {
    // given
    EnvVars envVars = new EnvVars();
    when(build.getEnvironment(listener)).thenReturn(envVars);
    // when
    RocketClient client = notifier.newRocketChatClient(build, listener);
    // then
    assertThat(client, is(not(nullValue())));
  }

  @Test
  public void shouldCreateRocketClientWithWebhook() throws Exception {
    // given
    EnvVars envVars = new EnvVars();
    when(build.getEnvironment(listener)).thenReturn(envVars);

    notifier = new RocketChatNotifier(
      "rocket.example.com", false,
      "user", "password",
      "jenkins", "rocket.example.com",
      false,
      false, false, false, false, false, false, false, false, null, false, false, null, null, "42", "23");
    // when
    RocketClient client = notifier.newRocketChatClient(build, listener);
    // then
    assertThat(client, is(not(nullValue())));
  }
}
