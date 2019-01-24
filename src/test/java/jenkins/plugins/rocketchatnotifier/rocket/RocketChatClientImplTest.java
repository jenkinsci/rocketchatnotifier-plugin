package jenkins.plugins.rocketchatnotifier.rocket;

import jenkins.plugins.rocketchatnotifier.model.Info;
import jenkins.plugins.rocketchatnotifier.model.Response;
import jenkins.plugins.rocketchatnotifier.model.Room;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest({RocketChatClientCallBuilder.class, RocketChatClientImpl.class})
public class RocketChatClientImplTest {

  private RocketChatClientImpl rocketChatClient;

  @Mock
  private RocketChatClientCallBuilder callBuilder;

  @Before
  public void setup() throws Exception {
    callBuilder = mock(RocketChatClientCallBuilder.class);
    PowerMockito.whenNew(RocketChatClientCallBuilder.class).withArguments(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString()).thenReturn(callBuilder);
    rocketChatClient = new RocketChatClientImpl("", false, "", "");
  }

  @Test
  public void shouldSendMessageToRoom() throws Exception {
    // given
    final Room room = new Room();
    room.setName("room");
    mockSuccess();
    // when
    rocketChatClient.send(room, "message");
    // then no error
  }

  @Test
  public void shouldWorkWithEmojiAndNoAvatarEmptyAttachments() throws Exception {
    // given
    mockSuccess();
    // when
    rocketChatClient.send("room", "message", "test", null);
    // then no error
  }

  @Test
  public void shouldWorkWithEmojiAndAvatarEmptyAttachments() throws Exception {
    // given
    mockSuccess();
    // when
    rocketChatClient.send("room", "message", "test", "avatar");
    // then no error
  }

  @Test
  public void shouldWorkWithNoEmojiAndNoAvatarEmptyAttachments() throws Exception {
    // given
    mockSuccess();
    // when
    rocketChatClient.send("room", "message");
    // then no error
  }

  @Test
  public void shouldWorkWithEmojiAndAvatarAndAttachments() throws Exception {
    // given
    mockSuccess();
    final List<Map<String, Object>> attachments = new ArrayList<Map<String, Object>>();
    attachments.add(new HashMap<String, Object>());
    // when
    rocketChatClient.send("room", "message", "test", "avatar", attachments);
    // then no error
  }

  @Test(expected = RocketClientException.class)
  public void shouldFailOnEmptyResponseOnVersionInfo() throws Exception {
    // given
    final Response errorResponse = new Response();
    errorResponse.setSuccess(false);
    when(callBuilder.buildCall(RocketChatRestApiV1.Info)).thenReturn(errorResponse);
    // when
    rocketChatClient.getInfo();
    // then error
  }

  @Test(expected = RocketClientException.class)
  public void shouldFailOnEmptyResponseOnSendMessage() throws Exception {
    // given
    final Response infoResponse = new Response();
    infoResponse.setSuccess(true);
    final Info info = new Info();
    info.setVersion("0.52.1");
    infoResponse.setInfo(info);
    final Response errorResponse = new Response();
    errorResponse.setSuccess(false);
    when(callBuilder.buildCall(RocketChatRestApiV1.Info)).thenReturn(infoResponse);
    when(callBuilder.buildCall(any(RocketChatRestApiV1.class), any(RocketChatQueryParams.class), any(Map.class))).thenReturn(errorResponse);
    // when
    rocketChatClient.send("room", "message", null, null, null);
    // then error
  }

  @Test
  public void shouldSendMessageToChannel() throws Exception {
    // given
    Set<String> collectedTargetChannels = mockSuccessAndCollectChannels();
    // when
    rocketChatClient.send("my-channel", "message");
    // then
    assertEquals(collectedTargetChannels.size(), 1);
    assertTrue(collectedTargetChannels.contains("#my-channel"));
  }

  @Test
  public void shouldSendMessageToChannelWithHash() throws Exception {
    // given
    Set<String> collectedTargetChannels = mockSuccessAndCollectChannels();
    // when
    rocketChatClient.send("#my-channel", "message");
    // then
    assertEquals(collectedTargetChannels.size(), 1);
    assertTrue(collectedTargetChannels.contains("#my-channel"));
  }

  @Test
  public void shouldSendMessageToPerson() throws Exception {
    // given
    Set<String> collectedTargetChannels = mockSuccessAndCollectChannels();
    // when
    rocketChatClient.send("@john", "message");
    // then
    assertEquals(collectedTargetChannels.size(), 1);
    assertTrue(collectedTargetChannels.contains("@john"));
  }

  @Test
  public void shouldSendMessageToMultipleRecipients() throws Exception {
    // given
    Set<String> collectedTargetChannels = mockSuccessAndCollectChannels();
    // when
    rocketChatClient.send("#my-channel, my-channel-no-hash, @john", "message");
    // then
    assertEquals(collectedTargetChannels.size(), 3);
    assertTrue(collectedTargetChannels.contains("#my-channel"));
    assertTrue(collectedTargetChannels.contains("#my-channel-no-hash"));
    assertTrue(collectedTargetChannels.contains("@john"));
  }


  private Response mockSuccess() throws Exception {
    final Response response = mockInfoRequest();
    when(callBuilder.buildCall(any(RocketChatRestApiV1.class), any(RocketChatQueryParams.class), any(Map.class))).thenReturn(response);
    return response;
  }

  private Set<String> mockSuccessAndCollectChannels() throws RocketClientException {
    final Response infoResponse = mockInfoRequest();
    final Set<String> collectedTargetChannels = new HashSet<>();
    when(this.callBuilder.buildCall(any(RocketChatRestApiV1.class), any(RocketChatQueryParams.class), any(Map.class)))
      .then(new Answer<Response>() {
        @Override
        public Response answer(InvocationOnMock invocationOnMock) throws Throwable {
          Map<String, String> body = (Map<String, String>) invocationOnMock.getArguments()[2];
          if (body != null) {
            String channelFromBody = body.get("channel");
            collectedTargetChannels.add(channelFromBody);
          }
          return infoResponse;
        }
      });
    return collectedTargetChannels;
  }

  private Response mockInfoRequest() throws RocketClientException {
    final Response infoResponse = new Response();
    infoResponse.setSuccess(true);
    final Info info = new Info();
    info.setVersion("0.52.1");
    infoResponse.setInfo(info);
    final Response response = new Response();
    response.setSuccess(true);
    when(callBuilder.buildCall(RocketChatRestApiV1.Info)).thenReturn(infoResponse);
    return response;
  }


}
