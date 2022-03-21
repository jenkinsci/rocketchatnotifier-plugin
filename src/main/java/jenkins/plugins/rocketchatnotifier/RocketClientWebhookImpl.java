package jenkins.plugins.rocketchatnotifier;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import jenkins.plugins.rocketchatnotifier.rocket.RocketChatClient;
import jenkins.plugins.rocketchatnotifier.rocket.RocketChatClientImpl;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RocketClientWebhookImpl implements RocketClient {

  private static final Logger LOGGER = Logger.getLogger(RocketClientImpl.class.getName());

  private RocketChatClient client;

  private String channel;

  public RocketClientWebhookImpl(String serverUrl, boolean trustSSL, String token, String webhookTokenCredentialId, String channel) throws RocketClientException {
    client = new RocketChatClientImpl(serverUrl, trustSSL, getTokenToUse(webhookTokenCredentialId, token));
    this.channel = channel;
  }

  public boolean publish(String message, List<Map<String, Object>> attachments) {
    try {
      LOGGER.fine("Starting sending message to webhook");
      this.client.send(this.channel, message);
      return true;
    } catch (RocketClientException e) {
      LOGGER.log(Level.SEVERE, "I/O error error during publishing message", e);
      return false;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unknown error error during publishing message", e);
      return false;
    }
  }

  @Override
  public boolean publish(final String message, final String emoji, final String avatar,
                         final List<Map<String, Object>> attachments) {
    try {
      LOGGER.fine("Starting sending message to webhook");
      this.client.send(this.channel, message, emoji, avatar, attachments);
      return true;
    } catch (RocketClientException e) {
      LOGGER.log(Level.SEVERE, "I/O error error during publishing message", e);
      return false;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unknown error error during publishing message", e);
      return false;
    }
  }

  @Override
  public void validate() throws CertificateException, RocketClientException {
    this.client.send("", "Test message from Jenkins via Webhook");
  }

  private String getTokenToUse(String webhookTokenCredentialId, String tokenString) {
    if (!StringUtils.isEmpty(webhookTokenCredentialId)) {
      StringCredentials credentials = lookupCredentials(webhookTokenCredentialId);
      if (credentials != null) {
        LOGGER.fine("Using Integration Token Credential ID.");
        return credentials.getSecret().getPlainText();
      }
    }

    LOGGER.fine("Using Integration Token.");

    return tokenString;
  }

  private StringCredentials lookupCredentials(String credentialId) {
    List<StringCredentials> credentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
      StringCredentials.class, Jenkins.getInstanceOrNull(), ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
    CredentialsMatcher matcher = CredentialsMatchers.withId(credentialId);
    return CredentialsMatchers.firstOrNull(credentials, matcher);
  }
}
