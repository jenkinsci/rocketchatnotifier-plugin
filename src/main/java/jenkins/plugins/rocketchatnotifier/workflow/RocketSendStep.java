package jenkins.plugins.rocketchatnotifier.workflow;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.Extension;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import java.util.Collections;
import java.util.List;
import jenkins.plugins.rocketchatnotifier.RocketClient;
import jenkins.plugins.rocketchatnotifier.RocketClientImpl;
import jenkins.plugins.rocketchatnotifier.RocketClientWebhookImpl;
import jenkins.plugins.rocketchatnotifier.model.MessageAttachment;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

public class RocketSendStep extends AbstractStepImpl {

  private final String message;
  private boolean useGlobalWebhookToken;
  private boolean trustSSL;
  private String channel;
  private boolean failOnError;
  @Deprecated
  private String webhookToken;
  private String webhookTokenCredentialId;
  private String credentialsId;
  private String emoji;
  private String avatar;
  private String color;
  private boolean rawMessage;
  private List<MessageAttachment> attachments;

  @DataBoundConstructor
  public RocketSendStep(@NonNull String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public boolean isUseGlobalWebhookToken() {
    return useGlobalWebhookToken;
  }

  public boolean isTrustSSL() {
    return trustSSL;
  }

  public String getChannel() {
    return channel;
  }

  public boolean isFailOnError() {
    return failOnError;
  }

  public String getWebhookToken() {
    return webhookToken;
  }

  public String getWebhookTokenCredentialId() {
    return webhookTokenCredentialId;
  }

  public String getCredentialsId() {
    return credentialsId;
  }

  public String getEmoji() {
    return emoji;
  }

  public String getAvatar() {
    return avatar;
  }

  public String getColor() {
    return color;
  }

  public boolean isRawMessage() {
    return rawMessage;
  }

  public List<MessageAttachment> getAttachments() {
    return attachments;
  }

  @DataBoundSetter
  public void setUseGlobalWebhookToken(boolean useGlobalWebhookToken) {
    this.useGlobalWebhookToken = useGlobalWebhookToken;
  }

  @DataBoundSetter
  public void setTrustSSL(boolean trustSSL) {
    this.trustSSL = trustSSL;
  }

  @DataBoundSetter
  public void setChannel(String channel) {
    this.channel = Util.fixEmpty(channel);
  }

  @DataBoundSetter
  public void setFailOnError(boolean failOnError) {
    this.failOnError = failOnError;
  }

  @DataBoundSetter
  public void setWebhookToken(String webhookToken) {
    this.webhookToken = Util.fixEmpty(webhookToken);
  }

  @DataBoundSetter
  public void setWebhookTokenCredentialId(final String webhookTokenCredentialId) {
    this.webhookTokenCredentialId = Util.fixEmpty(webhookTokenCredentialId);
  }

  @DataBoundSetter
  public void setCredentialsId(final String credentialsId) {
    this.credentialsId = Util.fixEmpty(credentialsId);
  }

  @DataBoundSetter
  public void setEmoji(String emoji) {
    this.emoji = Util.fixEmpty(emoji);
  }

  @DataBoundSetter
  public void setAvatar(String avatar) {
    this.avatar = Util.fixEmpty(avatar);
  }

  @DataBoundSetter
  public void setColor(String color) {
    this.color = Util.fixEmpty(color);
  }

  @DataBoundSetter
  public void setRawMessage(boolean rawMessage) {
    this.rawMessage = rawMessage;
  }

  @DataBoundSetter
  public void setAttachments(List<MessageAttachment> attachments) {
    this.attachments = attachments;
  }

  @Extension
  @Symbol("rocketSend")
  public static class DescriptorImpl extends AbstractStepDescriptorImpl {

    public DescriptorImpl() {
      super(RocketSendStepExecution.class);
    }

    @Override
    public String getFunctionName() {
      return "rocketSend";
    }

    @Override
    public String getDisplayName() {
      return Messages.RocketSendStepDisplayName();
    }

    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath ItemGroup<?> context, @QueryParameter String credentialsId) {
      if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
        return new StandardListBoxModel().includeCurrentValue(credentialsId);
      }
      ItemGroup<?> lookupContext = context != null ? context : Jenkins.get();
      return new StandardListBoxModel()
        .includeEmptyValue()
        .includeMatchingAs(
          ACL.SYSTEM,
          lookupContext,
          StandardUsernamePasswordCredentials.class,
          Collections.<DomainRequirement>emptyList(),
          CredentialsMatchers.always()
        )
        .includeCurrentValue(credentialsId);
    }

    public ListBoxModel doFillWebhookTokenCredentialIdItems(@AncestorInPath ItemGroup<?> context, @QueryParameter String webhookTokenCredentialId) {
      if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
        return new StandardListBoxModel().includeCurrentValue(webhookTokenCredentialId);
      }
      ItemGroup<?> lookupContext = context != null ? context : Jenkins.get();
      return new StandardListBoxModel()
        .includeEmptyValue()
        .includeMatchingAs(
          ACL.SYSTEM,
          lookupContext,
          StringCredentials.class,
          Collections.<DomainRequirement>emptyList(),
          CredentialsMatchers.always()
        )
        .includeCurrentValue(webhookTokenCredentialId);
    }
  }

  public static class RocketSendStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

    private static final long serialVersionUID = 1L;

    @StepContextParameter transient Run<?,?> run;
    @StepContextParameter transient TaskListener listener;
    @javax.inject.Inject transient RocketSendStep step;

    @Override
    protected Void run() throws Exception {
      Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
        LOG.log(Level.SEVERE, t + " runStep threw an exception: ", e);
      });

      Jenkins jenkins = Jenkins.get();
      if (jenkins == null) {
        throw new IllegalStateException("Jenkins instance not available");
      }
      RocketChatNotifier.DescriptorImpl rocketDesc = jenkins.getDescriptorByType(RocketChatNotifier.DescriptorImpl.class);
      String server = step.serverUrl != null ? step.serverUrl : rocketDesc.getRocketServerUrl();
      boolean trustSSL = step.trustSSL || rocketDesc.isTrustSSL();
      String user = rocketDesc.getUsername();
      String password = rocketDesc.getPassword();
      String globalCredentialsId = rocketDesc.getCredentialsId();
      String channel = step.channel != null ? step.channel : rocketDesc.getChannel();
      String jenkinsUrl = rocketDesc.getBuildServerUrl();
      String webhookToken = step.useGlobalWebhookToken ? rocketDesc.getWebhookToken() : step.getWebhookToken();
      String webhookTokenCredentialId = step.useGlobalWebhookToken ? rocketDesc.getWebhookTokenCredentialId() : step.getWebhookTokenCredentialId();
      String effectiveCredentialsId = step.getCredentialsId() != null ? step.getCredentialsId() : globalCredentialsId;

      listener.getLogger().println(Messages.RocketSendStepConfig(server, trustSSL, channel, step.message));

      try {
        RocketClient rocketClient = getRocketClient(
            run.getParent(), server, trustSSL, user, password,
            channel, webhookToken, webhookTokenCredentialId, effectiveCredentialsId);

        String msg = step.message;
        if (!step.rawMessage) {
          msg += "," + run.getFullDisplayName() + "," + jenkinsUrl + run.getUrl();
        }

        boolean publishSuccess = rocketClient.publish(msg, step.emoji, step.avatar,
            MessageAttachment.convertMessageAttachmentsToMaps(step.attachments));
        if (!publishSuccess && step.failOnError) {
          throw new AbortException(Messages.NotificationFailed());
        } else if (!publishSuccess) {
          listener.error(Messages.NotificationFailed());
        }
        return null;
      } catch (Exception ne) {
        listener.error(Messages.NotificationFailedWithException(ne.getMessage()));
        return null;
      }
    }

    // Helper for testing
    RocketClient getRocketClient(Item context, String server, boolean trustSSL, String user, String password, String channel,
                                 String webhookToken, String webhookTokenCredentialId, String credentialsId) throws RocketClientException {
      return RocketChatNotifier.getRocketClient(context, server, user, password, channel,
                                               webhookToken, webhookTokenCredentialId, credentialsId, trustSSL);
    }
  }
}

