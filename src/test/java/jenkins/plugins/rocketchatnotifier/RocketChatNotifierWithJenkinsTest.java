package jenkins.plugins.rocketchatnotifier;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import hudson.model.FreeStyleProject;
import hudson.util.Secret;
import junit.framework.Assert;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

public class RocketChatNotifierWithJenkinsTest {

  @Rule
  public JenkinsRule j = new JenkinsRule();

  @Before
  public void setup(){
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    root.setLevel(ch.qos.logback.classic.Level.INFO);
  }

  @Test
  public void testConfigurationRoundTrip() throws Exception {

    StringCredentials stringCredentials = new StringCredentialsImpl(CredentialsScope.GLOBAL, "id 1", "description 1", Secret.fromString("secret 1"));
    SystemCredentialsProvider.getInstance().getCredentials().add(stringCredentials);

    FreeStyleProject p = j.createFreeStyleProject();
    RocketChatNotifier before = new RocketChatNotifier(
      "rocket server url 1",
      false,
      "username 1",
      "password 1",
      "channel 1",
      "build server url 1",
      false,
      false,
      false,
      false,
      false,
      false,
      false,
      false,
      false,
      false,
      CommitInfoChoice.NONE,
      false,
      false,
      "custom message 1",
      null,
      "webhookToken 1",
      "webhookTokenCredentialId 1");
    p.getPublishersList().add(before);

    JenkinsRule.WebClient webClient = j.createWebClient();

    HtmlPage page = webClient.getPage(p, "configure");
    HtmlForm form = page.getFormByName("config");

    HtmlSelect htmlSelectInfoChoices = form.getSelectByName("commitInfoChoice");
    assertEquals(3, htmlSelectInfoChoices.getOptionSize());

    // entry element name is webhookTokenCredentialId in config.jelly
    HtmlSelect htmlSelectCredentials = form.getSelectByName("_.webhookTokenCredentialId");
    assertEquals(2, htmlSelectCredentials.getOptionSize());
    assertEquals("id 1", htmlSelectCredentials.getOption(1).getValueAttribute());

    form.getInputByName("notifyStart").setAttribute("checked", "checked");
    form.getInputByName("notifyAborted").setAttribute("checked", "checked");
    form.getInputByName("notifyFailure").setAttribute("checked", "checked");
    form.getInputByName("notifyNotBuilt").setAttribute("checked", "checked");
    form.getInputByName("notifySuccess").setAttribute("checked", "checked");
    form.getInputByName("notifyUnstable").setAttribute("checked", "checked");
    form.getInputByName("notifyBackToNormal").setAttribute("checked", "checked");
    form.getInputByName("notifyRepeatedFailure").setAttribute("checked", "checked");
    form.getInputByName("includeTestSummary").setAttribute("checked", "checked");
    form.getInputByName("includeTestLog").setAttribute("checked", "checked");
    form.getInputByName("rawMessage").setAttribute("checked", "checked");
    form.getInputByName("includeCustomMessage").setAttribute("checked", "checked");
    form.getTextAreaByName("customMessage").setText("custom message 2");
    // TODO: test attachments
    form.getSelectByName("commitInfoChoice").setSelectedIndex(2);
    form.getInputByName("rocketServerUrl").setValueAttribute("rocket server url 2");
    form.getInputByName("trustSSL").setAttribute("checked", "checked");
    form.getInputByName("channel").setValueAttribute("channel 2");
    form.getInputByName("webhookToken").setValueAttribute("webhookToken 2");
    form.getSelectByName("_.webhookTokenCredentialId").setSelectedIndex(1);

    j.submit(form);

    RocketChatNotifier after = p.getPublishersList().get(RocketChatNotifier.class);

    assertFalse(before.getNotifyStart());
    assertTrue(after.getNotifyStart());

    assertFalse(before.getNotifyAborted());
    assertTrue(after.getNotifyAborted());

    assertFalse(before.getNotifyFailure());
    assertTrue(after.getNotifyFailure());

    assertFalse(before.getNotifyNotBuilt());
    assertTrue(after.getNotifyNotBuilt());

    assertFalse(before.getNotifySuccess());
    assertTrue(after.getNotifySuccess());

    assertFalse(before.getNotifyUnstable());
    assertTrue(after.getNotifyUnstable());

    assertFalse(before.getNotifyBackToNormal());
    assertTrue(after.getNotifyBackToNormal());

    assertFalse(before.getNotifyRepeatedFailure());
    assertTrue(after.getNotifyRepeatedFailure());

    assertFalse(before.includeTestSummary());
    assertTrue(after.includeTestSummary());

    assertFalse(before.includeTestLog());
    assertTrue(after.includeTestLog());

    assertFalse(before.isRawMessage());
    assertTrue(after.isRawMessage());

    assertFalse(before.includeCustomMessage());
    assertTrue(after.includeCustomMessage());

    assertEquals("custom message 1", before.getCustomMessage());
    assertEquals("custom message 2", after.getCustomMessage());

    // TODO: test attachments
    assertNull(before.getAttachments());
    assertNull(after.getAttachments());

    assertEquals(CommitInfoChoice.NONE, before.getCommitInfoChoice());
    assertEquals(CommitInfoChoice.AUTHORS_AND_TITLES, after.getCommitInfoChoice());

    assertEquals("rocket server url 1", before.getRocketServerUrl());
    assertEquals("rocket server url 2", after.getRocketServerUrl());

    assertFalse(before.isTrustSSL());
    assertTrue(after.isTrustSSL());

    assertEquals("channel 1", before.getChannel());
    assertEquals("channel 2", after.getChannel());

    assertEquals("webhookToken 1", before.getWebhookToken());
    assertEquals("webhookToken 2", after.getWebhookToken());

    assertEquals("webhookTokenCredentialId 1", before.getWebhookTokenCredentialId());
    assertEquals("id 1", after.getWebhookTokenCredentialId());
  }
}
