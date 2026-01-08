package jenkins.plugins.rocketchatnotifier.utils;

import hudson.ProxyConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class NetworkUtilsTest {

  public static Stream<Arguments> data() {
    return Stream.of(
      Arguments.of(new ProxyConfiguration("sample1", 1234, null, null, "*.test.com|localhost"), "http://rocket.test.com", true),
      Arguments.of(new ProxyConfiguration("sample1", 1234, null, null, "*.test.*|localhost"), "http://rocket.test.com", true),
      Arguments.of(new ProxyConfiguration("sample1", 1234, null, null, "rocket.test.com"), "http://rocket.test.com", true),
      Arguments.of(new ProxyConfiguration("sample1", 1234, null, null, "rocket.test.com"), "https://rocket.test.com", true),
      Arguments.of(new ProxyConfiguration("sample1", 1234, null, null, "rocket.test.com"), "https://rocket.test.com:8443", true),
      Arguments.of(new ProxyConfiguration("sample1", 1234, null, null, "rocket.test.com"), "https://rocket.test.com/nestedUrl", true),
      Arguments.of(new ProxyConfiguration("sample1", 1234, null, null, "rocket.test.com"), "https://rocket.test.com:8443/nestedUrl", true),
      Arguments.of(new ProxyConfiguration("sample1", 1234, null, null, "*.test.com|localhost"), "http://rocket.test2.com", false)
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  public void test(ProxyConfiguration proxyConfiguration, String host, boolean shouldUseProxy) {
    // given
    // when
    boolean needsProxy = NetworkUtils.isHostOnNoProxyList(host, proxyConfiguration);
    // then
    assertThat(needsProxy, is(shouldUseProxy));
  }
}
