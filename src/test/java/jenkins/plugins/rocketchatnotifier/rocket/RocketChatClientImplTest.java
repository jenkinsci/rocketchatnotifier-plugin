package jenkins.plugins.rocketchatnotifier.rocket;

import jenkins.plugins.rocketchatnotifier.model.Response;
import jenkins.plugins.rocketchatnotifier.model.Room;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RocketChatClientImplTest {

  private RocketChatClientImpl rocketChatClient;

  @Mock
  private RocketChatClientCallBuilder callBuilder;

  private final List<AutoCloseable> closeableList = new ArrayList<>();

  @BeforeEach
  public void setup() throws Exception {
    callBuilder = mock(RocketChatClientCallBuilder.class);
    closeableList.add(Mockito.mockConstruction(RocketChatClientCallBuilder.class, (builder,context) -> {
      callBuilder = builder;
    }));
    rocketChatClient = new RocketChatClientImpl("", false, "", "");
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
    final List<Map<String, Object>> attachements = new ArrayList<Map<String, Object>>();
    attachements.add(new HashMap<String, Object>());
    // when
    rocketChatClient.send("room", "message", "test", "avatar", attachements);
    // then no error
  }

  @Test
  public void shouldFailOnEmptyResponseOnVersionInfo() throws Exception {
    // given
    final Response errorResponse = new Response();
    errorResponse.setSuccess(false);
    when(callBuilder.buildCall(RocketChatRestApiV1.Info)).thenReturn(errorResponse);
    // when
    Assertions.assertThrowsExactly(RocketClientException.class, () ->
      rocketChatClient.getInfo());
    // then error
  }

  @Test
  public void shouldFailOnEmptyResponseOnSendMessage() throws Exception {
    // given
    final Response infoResponse = new Response();
    infoResponse.setSuccess(true);
    infoResponse.setVersion("0.52.1");
    final Response errorResponse = new Response();
    errorResponse.setSuccess(false);
    when(callBuilder.buildCall(RocketChatRestApiV1.Info)).thenReturn(infoResponse);
    when(callBuilder.buildCall(any(RocketChatRestApiV1.class), any(), any(Map.class))).thenReturn(errorResponse);
    // when
    Assertions.assertThrowsExactly(RocketClientException.class, () ->
      rocketChatClient.send("room", "message", null, null, null));
    // then error
  }

  @Test
  public void shouldSendMessageToChannel() throws Exception {
    // given
    Set<String> collectedTargetChannels = mockSuccessAndCollectChannels();
    // when
    rocketChatClient.send("my-channel", "message");
    // then
    assertEquals(1, collectedTargetChannels.size());
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
    when(callBuilder.buildCall(any(RocketChatRestApiV1.class), any(), any(Map.class))).thenReturn(response);
    return response;
  }

  private Set<String> mockSuccessAndCollectChannels() throws RocketClientException {
    final Response infoResponse = mockInfoRequest();
    final Set<String> collectedTargetChannels = new HashSet<>();
    when(this.callBuilder.buildCall(any(RocketChatRestApiV1.class), any(), any(Map.class)))
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
    infoResponse.setVersion("0.52.1");
    final Response response = new Response();
    response.setSuccess(true);
    when(callBuilder.buildCall(RocketChatRestApiV1.Info)).thenReturn(infoResponse);
    return response;
  }


}
