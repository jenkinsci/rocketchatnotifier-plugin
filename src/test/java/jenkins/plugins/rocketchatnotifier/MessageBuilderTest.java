package jenkins.plugins.rocketchatnotifier;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Martin Reinhardt (hypery2k)
 */
@RunWith(MockitoJUnitRunner.class)
//@PrepareForTest({AbstractItem.class})
public class MessageBuilderTest {

  @Mock
  private RocketChatNotifier notifier;

  @SuppressWarnings("rawtypes")
  @Mock
  private AbstractBuild build;

  @Mock
  private AbstractProject project;
  @Mock
  private Jenkins parent;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(build.getProject()).thenReturn(project);
    when(parent.getFullDisplayName()).thenReturn("test-project");
    when(project.getParent()).thenReturn(parent);
    when(project.getLastBuild()).thenReturn(build);
    when(project.getFullDisplayName()).thenReturn("test-project");
    when(build.getDisplayName()).thenReturn("test-job");
  }

  @Test
  public void shouldDefaultToUnknownMessage() throws Exception {
    String msg = new MessageBuilder(this.notifier, this.build, false).getStatusMessage();
    assertThat(msg, is(MessageBuilder.UNKNOWN_STATUS_MESSAGE));
  }

  @Test
  public void shouldShowStartingMessageOnBuilding() throws Exception {
    when(build.isBuilding()).thenReturn(true);
    String msg = new MessageBuilder(this.notifier, this.build, false).getStatusMessage();
    assertThat(msg, is(MessageBuilder.STARTING_STATUS_MESSAGE));
  }

  @Test
  public void shouldShowFinishMessageOnBuildingDone() throws Exception {
    when(build.isBuilding()).thenReturn(true);
    String msg = new MessageBuilder(this.notifier, this.build, true).getStatusMessage();
    assertThat(msg, is(MessageBuilder.END_STATUS_MESSAGE));
  }
}
