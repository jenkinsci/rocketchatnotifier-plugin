package jenkins.plugins.rocketchatnotifier.rocket;

import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

public class RocketChatClientCallBuilderIT {

  @Test(expected = RocketClientException.class)
  public void shouldEscapeSpecialCharacters() throws Exception {
    // given
    RocketChatClientCallBuilder rocketCallBuilder = new RocketChatClientCallBuilder("http://localhost", false, "]\",", "]\",");
    // when
    rocketCallBuilder.buildCall(RocketChatRestApiV1.ChannelsList);
    // then error
  }
  @Test(expected = RocketClientException.class)
  public void shouldWorkWithProxy() throws Exception {
    // given
    Jenkins jenkinsMock = mock(Jenkins.class);
    ProxyConfiguration proxyConf = mock(ProxyConfiguration.class);
    //when(Jenkins.getInstanceOrNull()).thenReturn(jenkinsMock);
    try(MockedStatic<Jenkins> jenkinsMockedStatic = Mockito.mockStatic(Jenkins.class);){
      jenkinsMockedStatic.when(Jenkins::get).thenReturn(jenkinsMock);

      jenkinsMock.proxy = proxyConf;
      RocketChatClientCallBuilder rocketCallBuilder = new RocketChatClientCallBuilder("http://localhost", false, "]\",", "]\",");
      // when
      rocketCallBuilder.buildCall(RocketChatRestApiV1.ChannelsList);
      // then error
    }
  }
}
