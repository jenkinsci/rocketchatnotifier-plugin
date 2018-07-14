package jenkins.plugins.rocketchatnotifier.utils;

import hudson.ProxyConfiguration;
import hudson.util.Secret;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest({Secret.class, NetworkUtils.class})
public class NetworkUtilsTest {

  private ProxyConfiguration proxyConfiguration;
  private String host;
  private boolean shouldUseProxy;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    PowerMockito.mockStatic(Secret.class);
    return Arrays.asList(new Object[][]{
      {new ProxyConfiguration("sample1", 1234, null, null, "*.test.com|localhost"), "http://rocket.test.com", true},
      {new ProxyConfiguration("sample1", 1234, null, null, "*.test.*|localhost"), "http://rocket.test.com", true},
      {new ProxyConfiguration("sample1", 1234, null, null, "rocket.test.com"), "http://rocket.test.com", true},
      {new ProxyConfiguration("sample1", 1234, null, null, "rocket.test.com"), "https://rocket.test.com", true},
      {new ProxyConfiguration("sample1", 1234, null, null, "rocket.test.com"), "https://rocket.test.com:8443", true},
      {new ProxyConfiguration("sample1", 1234, null, null, "rocket.test.com"), "https://rocket.test.com/nestedUrl", true},
      {new ProxyConfiguration("sample1", 1234, null, null, "rocket.test.com"), "https://rocket.test.com:8443/nestedUrl", true},
      {new ProxyConfiguration("sample1", 1234, null, null, "*.test.com|localhost"), "http://rocket.test2.com", false},

    });
  }

  @Test
  public void test() {
    // given
    // when
    boolean needsProxy = NetworkUtils.isHostOnNoProxyList(host, proxyConfiguration);
    // then
    assertThat(needsProxy, is(shouldUseProxy));
  }

  public NetworkUtilsTest(ProxyConfiguration proxyConfiguration, String host, boolean shouldUseProxy) {
    this.proxyConfiguration = proxyConfiguration;
    this.host = host;
    this.shouldUseProxy = shouldUseProxy;
  }
}
