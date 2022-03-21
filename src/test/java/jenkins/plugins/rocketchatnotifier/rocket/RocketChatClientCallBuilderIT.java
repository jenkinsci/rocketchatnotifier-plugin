package jenkins.plugins.rocketchatnotifier.rocket;

import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Jenkins.class, ProxyConfiguration.class })
public class RocketChatClientCallBuilderIT {


  @Before
  public void setup() {
    PowerMockito.mockStatic(Jenkins.class);
    PowerMockito.mockStatic(ProxyConfiguration.class);
  }



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
    when(Jenkins.getInstanceOrNull()).thenReturn(jenkinsMock);
    jenkinsMock.proxy = proxyConf;
    RocketChatClientCallBuilder rocketCallBuilder = new RocketChatClientCallBuilder("http://localhost", false, "]\",", "]\",");
    // when
    rocketCallBuilder.buildCall(RocketChatRestApiV1.ChannelsList);
    // then error
  }
}
