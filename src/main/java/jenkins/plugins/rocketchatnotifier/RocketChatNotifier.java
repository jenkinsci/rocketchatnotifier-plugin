package jenkins.plugins.rocketchatnotifier;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import hudson.tasks.Notifier;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.tasks.Notifier;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import hudson.tasks.Notifier;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.tasks.Notifier;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.tasks.Notifier;
import hudson.EnvVars;
import hudson.tasks.Notifier;
import hudson.Extension;
import hudson.tasks.Notifier;
import hudson.model.AbstractBuild;
import hudson.tasks.Notifier;
import hudson.model.AbstractProject;
import hudson.tasks.Notifier;
import hudson.model.BuildListener;
import hudson.tasks.Notifier;
import hudson.model.Descriptor;
import hudson.tasks.Notifier;
import hudson.model.Item;
import hudson.tasks.Notifier;
import hudson.model.ItemGroup;
import hudson.tasks.Notifier;
import hudson.model.Run;
import hudson.tasks.Notifier;
import hudson.security.ACL;
import hudson.tasks.Notifier;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.Notifier;
import hudson.model.JobProperty;
import hudson.tasks.Notifier;
import jenkins.model.Jenkins;
import hudson.tasks.Notifier;
import jenkins.model.JenkinsLocationConfiguration;
import hudson.tasks.Notifier;
import jenkins.plugins.rocketchatnotifier.model.MessageAttachment;
import hudson.tasks.Notifier;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import hudson.tasks.Notifier;
import net.sf.json.JSONObject;
import hudson.tasks.Notifier;
import org.apache.commons.lang3.BooleanUtils;
import hudson.tasks.Notifier;
import org.apache.commons.lang3.StringUtils;
import hudson.tasks.Notifier;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import hudson.tasks.Notifier;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.tasks.Notifier;
import org.kohsuke.stapler.DataBoundSetter;
import hudson.tasks.Notifier;
import org.kohsuke.stapler.QueryParameter;
import hudson.tasks.Notifier;
import org.kohsuke.stapler.AncestorInPath;
import hudson.tasks.Notifier;
import hudson.security.csrf.RequirePOST;
import hudson.tasks.Notifier;
import org.kohsuke.stapler.StaplerRequest;
import hudson.tasks.Notifier;
import org.kohsuke.stapler.export.Exported;
import hudson.tasks.Notifier;

import javax.net.ssl.SSLHandshakeException;
import hudson.tasks.Notifier;
import java.security.cert.CertificateException;
import hudson.tasks.Notifier;
import java.util.Collections;
import hudson.tasks.Notifier;
import java.util.List;
import hudson.tasks.Notifier;
import java.util.logging.Level;
import hudson.tasks.Notifier;
import java.util.logging.Logger;
import hudson.tasks.Notifier;
import hudson.util.FormValidation;
import hudson.tasks.Notifier;
import hudson.util.ListBoxModel;
import hudson.tasks.Notifier;

/**
 * RocketChat notifier for Jenkins builds (freestyle jobs).
 */
public class RocketChatNotifier extends Notifier {

  private static final Logger LOGGER = Logger.getLogger(RocketChatNotifier.class.getName());

  private String rocketServerUrl;
  private boolean trustSSL;
  private String username;
  private String password;
  private String channel;
  private boolean notifyStart;
  private boolean notifySuccess;
  private boolean notifyRepeatedFailure;
  private boolean includeTestSummary;
  private boolean includeTestLog;
  private boolean includeCustomMessage;
  private boolean notifyAborted;
  private boolean notifyFailure;
  private boolean notifyNotBuilt;
  private boolean notifyUnstable;
  private boolean notifyBackToNormal;
  private String customMessage;
  private boolean rawMessage;
  private List<MessageAttachment> attachments;
  @Deprecated
  private String webhookToken;
  private String webhookTokenCredentialId;
  private String credentialsId;

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  public String getRocketServerUrl() {
    return rocketServerUrl;
  }

  public boolean isTrustSSL() {
    return trustSSL;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getChannel() {
    return channel;
  }

  public boolean isNotifyStart() {
    return notifyStart;
  }

  public boolean isNotifySuccess() {
    return notifySuccess;
  }

  public boolean isNotifyRepeatedFailure() {
    return notifyRepeatedFailure;
  }

  public boolean isIncludeTestSummary() {
    return includeTestSummary;
  }

  public boolean isIncludeTestLog() {
    return includeTestLog;
  }

  public boolean isIncludeCustomMessage() {
    return includeCustomMessage;
  }

  public boolean isNotifyAborted() {
    return notifyAborted;
  }

  public boolean isNotifyFailure() {
    return notifyFailure;
  }

  public boolean isNotifyNotBuilt() {
    return notifyNotBuilt;
  }

  public boolean isNotifyUnstable() {
    return notifyUnstable;
  }

  public boolean isNotifyBackToNormal() {
    return notifyBackToNormal;
  }

  public String getCustomMessage() {
    return customMessage;
  }

  public boolean isRawMessage() {
    return rawMessage;
  }

  public List<MessageAttachment> getAttachments() {
    return attachments;
  }

  public String getWebhookTokenCredentialId() {
    return webhookTokenCredentialId;
  }

  public String getCredentialsId() {
    return credentialsId;
  }

  public String getBuildServerUrl() {
    LOGGER.log(Level.FINE, "Getting build server URL");
    if (buildServerUrl == null || buildServerUrl.equalsIgnoreCase("")) {
      // Use the globally configured Jenkins URL
      return getJenkinsLocationConfiguration().getUrl();
    } else {
      return buildServerUrl;
    }
  }

  private JenkinsLocationConfiguration getJenkinsLocationConfiguration() {
    JenkinsLocationConfiguration jlc = JenkinsLocationConfiguration.get();
    if (jlc == null) {
      throw new IllegalStateException("JenkinsLocationConfiguration not available");
    }
    return jlc;
  }

  @DataBoundSetter
  public void setCredentialsId(String credentialsId) {
    this.credentialsId = credentialsId;
  }

  @Override
  public BuildStepMonitor getRequiredMonitorService() {
    return BuildStepMonitor.NONE;
  }

  @Override
  public RocketClient newRocketChatClient(AbstractBuild<?, ?> build, BuildListener listener) throws RocketClientException {
    String serverUrl = this.rocketServerUrl;
    if (StringUtils.isEmpty(serverUrl)) {
      serverUrl = getDescriptor().getRocketServerUrl();
    }
    String channel = this.channel;
    if (StringUtils.isEmpty(channel)) {
      channel = getDescriptor().getChannel();
    }
    String webhookTokenCredentialId = this.webhookTokenCredentialId;
    if (StringUtils.isEmpty(webhookTokenCredentialId)) {
      webhookTokenCredentialId = getDescriptor().getWebhookTokenCredentialId();
    }
    String credentialsId = this.credentialsId;
    if (StringUtils.isEmpty(credentialsId)) {
      credentialsId = getDescriptor().getCredentialsId();
    }
    String username = this.username;
    if (StringUtils.isEmpty(username)) {
      username = getDescriptor().getUsername();
    }
    String password = this.password;
    if (StringUtils.isEmpty(password)) {
      password = getDescriptor().getPassword();
    }
    String webhookToken = this.webhookToken;
    if (StringUtils.isEmpty(webhookToken)) {
      webhookToken = getDescriptor().getWebhookToken();
    }
    EnvVars env;
    try {
      env = build.getEnvironment(listener);
    } catch (Exception e) {
      listener.getLogger().println("Error retrieving environment vars: " + e.getMessage());
      env = new EnvVars();
    }
    serverUrl = env.expand(serverUrl);
    channel = env.expand(channel);
    username = env.expand(username);
    password = env.expand(password);

    return getRocketClient(build, serverUrl, username, password, channel,
                       webhookToken, webhookTokenCredentialId, credentialsId, trustSSL);
  }

  public static RocketClient getRocketClient(
      Run<?, ?> context,
      String serverUrl,
      String username,
      String password,
      String channel,
      String webhookToken,
      String webhookTokenCredentialId,
      String credentialsId,
      boolean trustSSL) throws RocketClientException {

    // Prefer webhook via Secret Text credential
    if (StringUtils.isNotEmpty(webhookTokenCredentialId)) {
      return new RocketClientWebhookImpl(serverUrl, trustSSL, null, webhookTokenCredentialId, channel);
    }

    // Legacy: webhook token stored directly
    if (StringUtils.isNotEmpty(webhookToken)) {
      return new RocketClientWebhookImpl(serverUrl, trustSSL, webhookToken, null, channel);
    }

    // Username/Password credentials
    if (StringUtils.isNotEmpty(credentialsId)) {
      StandardUsernamePasswordCredentials c = CredentialsProvider.findCredentialById(
        credentialsId,
        StandardUsernamePasswordCredentials.class,
        Jenkins.get(),
        Collections.<DomainRequirement>emptyList()
      );
      if (c == null) {
        throw new RocketClientException("Configured credentialsId '" + credentialsId + "' not found or not usable.");
      }
      return new RocketClientImpl(serverUrl, trustSSL, c.getUsername(), c.getPassword().getPlainText(), channel);
    }

    // Legacy: plaintext username/password
    return new RocketClientImpl(serverUrl, trustSSL, username, password, channel);
  }

  /**
   * Backward compatible overload.
   */
  public static RocketClient getRocketClient(String serverUrl, String username, String password, String channel, String webhookToken, String webhookTokenCredentialId, boolean trustSSL) throws RocketClientException {
    return getRocketClient(null, serverUrl, username, password, channel, webhookToken, webhookTokenCredentialId, null, trustSSL);
  }

  @Extension
  public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

    private String rocketServerUrl;
    private String credentialsId;
    private boolean trustSSL;
    private String username;
    private String password;
    private String channel;
    private String buildServerUrl;
    @Deprecated
    private String webhookToken;
    private String webhookTokenCredentialId;

    public String getRocketServerUrl() {
      return rocketServerUrl;
    }
    public String getCredentialsId() {
      return credentialsId;
    }
    public boolean isTrustSSL() {
      return trustSSL;
    }
    public String getUsername() {
      return username;
    }
    public String getPassword() {
      return password;
    }
    public String getChannel() {
      return channel;
    }
    public String getWebhookToken() {
      return webhookToken;
    }
    public String getWebhookTokenCredentialId() {
      return webhookTokenCredentialId;
    }

    public String getBuildServerUrl() {
      if (buildServerUrl == null || buildServerUrl.equalsIgnoreCase("")) {
        // Return global Jenkins URL if blank
        return JenkinsLocationConfiguration.get().getUrl();
      } else {
        return buildServerUrl;
      }
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
      this.credentialsId = credentialsId;
    }

    @DataBoundSetter
    public void setUsername(String username) {
      this.username = username;
    }
    @DataBoundSetter
    public void setPassword(String password) {
      this.password = password;
    }
    @DataBoundSetter
    public void setChannel(String channel) {
      this.channel = channel;
    }
    @DataBoundSetter
    public void setTrustSSL(boolean trustSSL) {
      this.trustSSL = trustSSL;
    }
    @DataBoundSetter
    public void setRocketServerUrl(String rocketServerUrl) {
      this.rocketServerUrl = rocketServerUrl;
    }
    @DataBoundSetter
    public void setBuildServerUrl(String buildServerUrl) {
      this.buildServerUrl = buildServerUrl;
      if (buildServerUrl == null || buildServerUrl.equalsIgnoreCase("")) {
        this.buildServerUrl = JenkinsLocationConfiguration.get().getUrl();
      }
      if (buildServerUrl != null && !buildServerUrl.endsWith("/")) {
        this.buildServerUrl = buildServerUrl + "/";
      }
    }
    @DataBoundSetter
    public void setWebhookTokenCredentialId(String webhookTokenCredentialId) {
      this.webhookTokenCredentialId = webhookTokenCredentialId;
    }
    @DataBoundSetter
    public void setWebhookToken(String webhookToken) {
      this.webhookToken = webhookToken;
    }

    @Override
    public String getDisplayName() {
      return "RocketChat Notifications";
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      return true;
    }

    @RequirePOST
    public boolean configure(StaplerRequest req, JSONObject json) {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      if (json != null) {
        try {
          req.bindJSON(this, json);
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Failed to bind JSON", e);
          return false;
        }
      }
      save();
      return true;
    }

    @RequirePOST
    public FormValidation doTestConnection(@QueryParameter("rocketServerUrl") final String rocketServerUrl,
                                           @QueryParameter("trustSSL") final String trustSSL,
                                           @QueryParameter("channel") final String channel,
                                           @QueryParameter("buildServerUrl") final String buildServerUrl,
                                           @QueryParameter("webhookToken") final String token,
                                           @QueryParameter("webhookTokenCredentialId") final String webhookTokenCredentialId,
                                           @QueryParameter("credentialsId") final String credentialsId) throws FormValidation {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      try {
        String targetServerUrl = rocketServerUrl + RocketClientImpl.API_PATH;
        if (StringUtils.isEmpty(rocketServerUrl)) {
          targetServerUrl = this.rocketServerUrl + RocketClientImpl.API_PATH;
        }
        boolean targetTrustSSL = this.trustSSL;
        if (StringUtils.isNotEmpty(trustSSL)) {
          targetTrustSSL = BooleanUtils.toBoolean(trustSSL);
        }
        String targetChannel = channel;
        if (StringUtils.isEmpty(targetChannel)) {
          targetChannel = this.channel;
        }
        String targetBuildServerUrl = buildServerUrl;
        if (StringUtils.isEmpty(targetBuildServerUrl)) {
          targetBuildServerUrl = this.buildServerUrl;
        }
        String targetWebhookTokenCredentialId = webhookTokenCredentialId;
        if (StringUtils.isEmpty(targetWebhookTokenCredentialId)) {
          targetWebhookTokenCredentialId = this.webhookTokenCredentialId;
        }
        String targetCredentialsId = credentialsId;
        if (StringUtils.isEmpty(targetCredentialsId)) {
          targetCredentialsId = this.credentialsId;
        }
        String targetUsername = username;
        if (StringUtils.isEmpty(targetUsername)) {
          targetUsername = this.username;
        }
        String targetPassword = password;
        if (StringUtils.isEmpty(targetPassword)) {
          targetPassword = this.password;
        }
        String targetWebhookToken = token;
        if (StringUtils.isEmpty(targetWebhookToken)) {
          targetWebhookToken = this.webhookToken;
        }

        RocketClient rocketChatClient;
        if (StringUtils.isNotEmpty(targetWebhookTokenCredentialId)) {
          rocketChatClient = new RocketClientWebhookImpl(targetServerUrl, targetTrustSSL, null, targetWebhookTokenCredentialId, targetChannel);
        } else if (StringUtils.isNotEmpty(targetWebhookToken)) {
          rocketChatClient = new RocketClientWebhookImpl(targetServerUrl, targetTrustSSL, targetWebhookToken, null, targetChannel);
        } else if (StringUtils.isNotEmpty(targetCredentialsId)) {
          StandardUsernamePasswordCredentials c = CredentialsProvider.findCredentialById(
            targetCredentialsId,
            StandardUsernamePasswordCredentials.class,
            Jenkins.get(),
            Collections.<DomainRequirement>emptyList()
          );
          if (c == null) {
            return FormValidation.error("Configured credentialsId '%s' not found or not usable.", targetCredentialsId);
          }
          rocketChatClient = new RocketClientImpl(targetServerUrl, targetTrustSSL, c.getUsername(), c.getPassword().getPlainText(), targetChannel);
        } else {
          rocketChatClient = new RocketClientImpl(targetServerUrl, targetTrustSSL, targetUsername, targetPassword, targetChannel);
        }

        String message = "RocketChat/Jenkins plugin: you're all set on " + targetBuildServerUrl;
        LOGGER.fine("Validating configuration for Rocket.Chat");
        rocketChatClient.validate();
        LOGGER.fine("Configuration validated, sending test message");
        rocketChatClient.publish(message, null);
        return FormValidation.ok("Success");
      } catch (Exception e) {
        if (e.getCause() != null && (e.getCause().getClass() == SSLHandshakeException.class || e.getCause().getClass() == CertificateException.class)) {
          LOGGER.log(Level.SEVERE, "SSL error during connection test", e);
          return FormValidation.error(e, "SSL error: " + e.getMessage());
        } else {
          LOGGER.log(Level.SEVERE, "Error testing Rocket.Chat connection", e);
          return FormValidation.error(e, "Client error - Could not send test message");
        }
      }
    }

    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath ItemGroup<?> context, @QueryParameter String credentialsId) {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
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

  @Deprecated
  public static class RocketJobProperty extends hudson.model.JobProperty<AbstractProject<?, ?>> {

    // Deprecated job property, kept for compatibility
    private String rocketServerUrl;
    private String username;
    private String password;
    private String channel;
    private boolean trustSSL;
    private boolean startNotification;
    private boolean completeNotification;
    private boolean notifyRepeatedFailure;
    private boolean includeTestSummary;
    private boolean includeTestLog;
    private boolean includeCustomMessage;
    private String customMessage;
    private boolean notifyAborted;
    private boolean notifyFailure;
    private boolean notifyNotBuilt;
    private boolean notifyUnstable;
    private boolean notifyBackToNormal;

    public RocketJobProperty(String rocketServerUrl, String username, String password, String channel,
                             boolean trustSSL, boolean startNotification, boolean completeNotification,
                             boolean notifyRepeatedFailure, boolean notifyAborted, boolean notifyFailure,
                             boolean notifyNotBuilt, boolean notifyUnstable, boolean notifyBackToNormal,
                             boolean includeTestSummary, boolean includeTestLog, boolean includeCustomMessage,
                             String customMessage) {
      this.rocketServerUrl = rocketServerUrl;
      this.username = username;
      this.password = password;
      this.channel = channel;
      this.trustSSL = trustSSL;
      this.startNotification = startNotification;
      this.completeNotification = completeNotification;
      this.notifyRepeatedFailure = notifyRepeatedFailure;
      this.notifyAborted = notifyAborted;
      this.notifyFailure = notifyFailure;
      this.notifyNotBuilt = notifyNotBuilt;
      this.notifyUnstable = notifyUnstable;
      this.notifyBackToNormal = notifyBackToNormal;
      this.includeTestSummary = includeTestSummary;
      this.includeTestLog = includeTestLog;
      this.includeCustomMessage = includeCustomMessage;
      this.customMessage = customMessage;
    }

    public String getRocketServerUrl() {
      return rocketServerUrl;
    }

    @Exported
    public boolean isTrustSSL() {
      return trustSSL;
    }

    @Exported
    public String getUsername() {
      return username;
    }

    @Exported
    public String getPassword() {
      return password;
    }

    @Exported
    public String getChannel() {
      return channel;
    }

    @Exported
    public boolean getNotifyStart() {
      return startNotification;
    }

    @Exported
    public boolean getNotifySuccess() {
      return completeNotification;
    }

    @Exported
    public boolean getShowCommitList() {
      return false;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
      return super.prebuild(build, listener);
    }

    @Exported
    public boolean getNotifyAborted() {
      return notifyAborted;
    }

    @Exported
    public boolean getNotifyFailure() {
      return notifyFailure;
    }

    @Exported
    public boolean getNotifyNotBuilt() {
      return notifyNotBuilt;
    }

    @Exported
    public boolean getNotifyUnstable() {
      return notifyUnstable;
    }

    @Exported
    public boolean getNotifyBackToNormal() {
      return notifyBackToNormal;
    }

    @Exported
    public boolean includeTestSummary() {
      return includeTestSummary;
    }

    @Exported
    public boolean includeTestLog() {
      return includeTestLog;
    }

    @Exported
    public boolean getNotifyRepeatedFailure() {
      return notifyRepeatedFailure;
    }

    @Exported
    public boolean includeCustomMessage() {
      return includeCustomMessage;
    }

    @Exported
    public String getCustomMessage() {
      return customMessage;
    }
  }
}

