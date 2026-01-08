package jenkins.plugins.rocketchatnotifier;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

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

  @Mock
  private RocketChatNotifier.DescriptorImpl descriptor;

  RocketChatNotifier notifier;

  private final List<AutoCloseable> closeableList = new ArrayList<>();

  @BeforeEach
  public void setup() {
    closeableList.add(MockitoAnnotations.openMocks(this));

    MockedStatic<Jenkins> jenkinsMockedStatic = Mockito.mockStatic(Jenkins.class);
    jenkinsMockedStatic.when(Jenkins::get).thenReturn(jenkins);
    closeableList.add(jenkinsMockedStatic);

    closeableList.add(Mockito.mockConstruction(RocketClientImpl.class));

    notifier = new RocketChatNotifier(
      EXPECTED_URL, false,
      "user", "password",
      "jenkins", "rocket.example.com",
      false,
      false, false, false, false, false, false, false, false, false, null, false, false, null, null, null, null) {
      @Override
      public DescriptorImpl getDescriptor() {
        return descriptor;
      }
    };
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
  public void shouldFallbackToJenkinsUrlIfBuildServerUrlIsNotProvived() throws Exception {
    // given
    notifier.setBuildServerUrl(null);

    final JenkinsLocationConfiguration locationConfigMock = Mockito.mock(JenkinsLocationConfiguration.class);
    when(locationConfigMock.getUrl()).thenReturn(EXPECTED_URL);
    try (MockedStatic<JenkinsLocationConfiguration> locationConfigMock2 = Mockito.mockStatic(JenkinsLocationConfiguration.class)) {
      locationConfigMock2.when(JenkinsLocationConfiguration::get).thenReturn(locationConfigMock);

      // when
      String serverUrl = notifier.getBuildServerUrl();
      // then
      assertThat(serverUrl, equalTo(EXPECTED_URL));
    }
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
