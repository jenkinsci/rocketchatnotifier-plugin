package jenkins.plugins.rocketchatnotifier.rocket;

import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.MultipartBody;
import kong.unirest.Unirest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RocketChatBasicCallAuthenticationTest {

  @Test
  public void shouldTryTokenBasedAuthToo() throws Exception {
    try (MockedStatic<Unirest> unirestMockedStatic = Mockito.mockStatic(Unirest.class)) {
      HttpResponse<JsonNode> response = mock(HttpResponse.class);
      when(response.getStatus()).thenReturn(401);
      HttpRequestWithBody request = mock(HttpRequestWithBody.class);
      MultipartBody body = mock(MultipartBody.class);
      when(request.field(anyString(),anyString())).thenReturn(body);
      when(body.asJson()).thenReturn(response);
      when(body.field(anyString(),anyString())).thenReturn(body);
      //when(Unirest.post( "https://example.com/api/v1/login")).thenReturn(request);
      unirestMockedStatic.when(() -> Unirest.post("https://example.com/api/v1/login")).thenReturn(request);

      GetRequest getRequest = mock(GetRequest.class);
      HttpResponse<JsonNode> getResponse = mock(HttpResponse.class);
      unirestMockedStatic.when(() -> Unirest.get( "https://example.com/api/v1/me")).thenReturn(getRequest);
      when(getRequest.header(anyString(),anyString())).thenReturn(getRequest);
      when(getResponse.getStatus()).thenReturn(200);
      when(getRequest.asJson()).thenReturn(getResponse);

      RocketChatBasicCallAuthentication chatBasicCallAuthentication = new RocketChatBasicCallAuthentication("example.com/", "a", "b");
      assertThat(chatBasicCallAuthentication.getUrlForRequest(RocketChatRestApiV1.ChannelsList), is(equalTo("https://example.com/api/v1/channels.list")));
    }
  }

  @Test
  public void shouldNotAppendSlashToRootUrlIfAlreadyGiven() throws Exception {
    RocketChatBasicCallAuthentication chatBasicCallAuthentication = new RocketChatBasicCallAuthentication("example.com/", "a", "b");
    String sampleCall = chatBasicCallAuthentication.getUrlForRequest(RocketChatRestApiV1.ChannelsList);
    assertThat(sampleCall, is(equalTo("https://example.com/api/v1/channels.list")));
  }

  @Test
  public void shouldAppendApiPathIfNotGiven() throws Exception {
    RocketChatBasicCallAuthentication chatBasicCallAuthentication = new RocketChatBasicCallAuthentication("example.com", "a", "b");
    String sampleCall = chatBasicCallAuthentication.getUrlForRequest(RocketChatRestApiV1.ChannelsList);
    assertThat(sampleCall, is(equalTo("https://example.com/api/v1/channels.list")));
  }

  @Test
  public void shouldNotAppendApiPathIfiven() throws Exception {
    RocketChatBasicCallAuthentication chatBasicCallAuthentication = new RocketChatBasicCallAuthentication("example.com/api", "a", "b");
    String sampleCall = chatBasicCallAuthentication.getUrlForRequest(RocketChatRestApiV1.ChannelsList);
    assertThat(sampleCall, is(equalTo("https://example.com/api/v1/channels.list")));
  }

  @Test
  public void shouldAutoPrefixWithHttpsIfNotGiven() throws Exception {
    try (MockedStatic<Unirest> unirestMockedStatic = Mockito.mockStatic(Unirest.class)) {
      HttpResponse<JsonNode> response = mock(HttpResponse.class);
      when(response.getStatus()).thenReturn(401);
      HttpRequestWithBody request = mock(HttpRequestWithBody.class);
      MultipartBody body = mock(MultipartBody.class);
      when(request.field(anyString(),anyString())).thenReturn(body);
      when(body.asJson()).thenReturn(response);
      when(body.field(anyString(),anyString())).thenReturn(body);
      unirestMockedStatic.when(() -> Unirest.post("https://example.com/api/v1/login")).thenReturn(request);

      GetRequest getRequest = mock(GetRequest.class);
      HttpResponse<JsonNode> getResponse = mock(HttpResponse.class);
      unirestMockedStatic.when(() -> Unirest.get( "https://example.com/api/v1/me")).thenReturn(getRequest);
      when(getRequest.header(anyString(),anyString())).thenReturn(getRequest);
      when(getResponse.getStatus()).thenReturn(401);
      when(getRequest.asJson()).thenReturn(getResponse);

      Assertions.assertThrowsExactly(RocketClientException.class, () ->
        new RocketChatBasicCallAuthentication("example.com", "a", "b").doAuthentication());
    }
  }
}
