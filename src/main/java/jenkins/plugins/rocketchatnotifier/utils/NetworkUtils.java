package jenkins.plugins.rocketchatnotifier.utils;

import hudson.ProxyConfiguration;

import java.net.URI;
import java.util.regex.Pattern;

public class NetworkUtils {

  /**
   * Returns whether or not host is on the noProxy list
   * as defined in the Jenkins proxy settings
   *
   * @param proxy the ProxyConfiguration
   * @return whether or not the host is on the noProxy list
   */
  public static boolean isHostOnNoProxyList(String host, ProxyConfiguration proxy) {
    if (host != null && proxy.noProxyHost != null) {
      for (Pattern p : ProxyConfiguration.getNoProxyHostPatterns(proxy.noProxyHost)) {
        if (p.matcher(URI.create(host).getHost()).matches()) {
          return true;
        }
      }
    }
    return false;
  }
}
