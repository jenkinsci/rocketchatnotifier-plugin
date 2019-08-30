package jenkins.plugins.rocketchatnotifier;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, JenkinsLocationConfiguration.class, RocketClientImpl.class, RocketClientWebhookImpl.class, RocketChatNotifier.class})
public class RocketChatNotifierTest {

  private static final String EXPECTED_URL = "rocket.example.com";

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
    PowerMockito.mockStatic(Jenkins.class, JenkinsLocationConfiguration.class);
    PowerMockito.whenNew(RocketClientImpl.class).withAnyArguments().thenReturn(rocketClient);
    PowerMockito.whenNew(RocketClientWebhookImpl.class).withAnyArguments().thenReturn(rocketClientWithWebhook);
    PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins);
    File rootPath = new File(System.getProperty("java.io.tmpdir"));
    when(jenkins.getRootDir()).thenReturn(rootPath);
    notifier = new RocketChatNotifier(
      EXPECTED_URL, false,
      "user", "password",
      "jenkins", "rocket.example.com",
      false,
      false, false, false, false, false, false, false, false, false, null, false, false, null, null, null, null);
  }

  @Test
  public void shouldFallbackToJenkinsUrlIfBuildServerUrlIsNotProvived() throws Exception {
    // given
    notifier.setBuildServerUrl(null);
    JenkinsLocationConfiguration locationConfigMock = PowerMockito.mock(JenkinsLocationConfiguration.class);
    PowerMockito.when(locationConfigMock.getUrl()).thenReturn(EXPECTED_URL);
    PowerMockito.when(JenkinsLocationConfiguration.get()).thenReturn(locationConfigMock);
    // when
    String serverUrl = notifier.getBuildServerUrl();
    // then
    assertThat(serverUrl, equalTo(EXPECTED_URL));
  }

  @Test
  public void shouldProvideBuildServerUrl() throws Exception {
    // given
    // when
    String serverUrl = notifier.getBuildServerUrl();
    // then
    assertThat(serverUrl, equalTo(EXPECTED_URL));
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
      false, false, false, false, false, false, false, false, false, null, false, false, null, null, "42", "23");
    // when
    RocketClient client = notifier.newRocketChatClient(build, listener);
    // then
    assertThat(client, is(not(nullValue())));
  }
}
