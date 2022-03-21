package jenkins.plugins.rocketchatnotifier;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import jenkins.plugins.rocketchatnotifier.model.MessageAttachment;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

public class RocketChatNotifier extends Notifier {

  private static final Logger LOGGER = Logger.getLogger(RocketChatNotifier.class.getName());

  private String rocketServerUrl;
  private boolean trustSSL;
  private String username;
  private String password;
  private String channel;
  private String buildServerUrl;
  private boolean notifyStart;
  private boolean notifySuccess;
  private boolean notifyAborted;
  private boolean notifyNotBuilt;
  private boolean notifyUnstable;
  private boolean notifyFailure;
  private boolean notifyBackToNormal;
  private boolean notifyRepeatedFailure;
  private boolean includeTestSummary;
  private boolean includeTestLog;
  private CommitInfoChoice commitInfoChoice;
  private boolean includeCustomMessage;
  private String customMessage;
  private boolean rawMessage;
  private List<MessageAttachment> attachments;
  private String webhookToken;
  private String webhookTokenCredentialId;

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  public String getBuildServerUrl() {
    LOGGER.log(Level.FINE, "Getting build server URL");
    if (buildServerUrl == null || buildServerUrl.equalsIgnoreCase("")) {
      return getJenkinsLocationConfiguration().getUrl();
    } else {
      return buildServerUrl;
    }
  }

  /**
   * Method added to pass findbugs verification when compiling against 1.642.1
   *
   * @return The JenkinsLocationConfiguration object.
   * @throws IllegalStateException if the object is not available (e.g., Jenkins not fully initialized).
   */
  @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
    justification = "False positive. See https://sourceforge.net/p/findbugs/bugs/1411/")
  private JenkinsLocationConfiguration getJenkinsLocationConfiguration() {
    final JenkinsLocationConfiguration jlc = JenkinsLocationConfiguration.get();
    if (jlc == null) {
      throw new IllegalStateException("JenkinsLocationConfiguration not available");
    }
    return jlc;
  }

  public String getChannel() {
    return channel;
  }

  public boolean isRawMessage() {
    return rawMessage;
  }

  public List<MessageAttachment> getAttachments() {
    return attachments;
  }

  public boolean getNotifyStart() {
    return notifyStart;
  }

  public boolean getNotifySuccess() {
    return notifySuccess;
  }

  public CommitInfoChoice getCommitInfoChoice() {
    return commitInfoChoice;
  }

  public boolean getNotifyAborted() {
    return notifyAborted;
  }

  public boolean getNotifyFailure() {
    return notifyFailure;
  }

  public boolean getNotifyNotBuilt() {
    return notifyNotBuilt;
  }

  public boolean getNotifyUnstable() {
    return notifyUnstable;
  }

  public boolean getNotifyBackToNormal() {
    return notifyBackToNormal;
  }

  public boolean includeTestSummary() {
    return includeTestSummary;
  }

  public boolean includeTestLog() {
    return includeTestLog;
  }

  public boolean getNotifyRepeatedFailure() {
    return notifyRepeatedFailure;
  }

  public boolean includeCustomMessage() {
    return includeCustomMessage;
  }

  public String getCustomMessage() {
    return customMessage;
  }

  public String getWebhookToken() {
    return webhookToken;
  }

  public String getWebhookTokenCredentialId() {
    return webhookTokenCredentialId;
  }

  public String getRocketServerUrl() {
    return rocketServerUrl;
  }

  public boolean isTrustSSL() {
    return trustSSL;
  }

  @DataBoundSetter
  public void setChannel(String channel) {
    this.channel = channel;
  }

  public boolean isNotifyStart() {
    return notifyStart;
  }

  @DataBoundSetter
  public void setNotifyStart(boolean notifyStart) {
    this.notifyStart = notifyStart;
  }

  public boolean isNotifySuccess() {
    return notifySuccess;
  }

  @DataBoundSetter
  public void setNotifySuccess(boolean notifySuccess) {
    this.notifySuccess = notifySuccess;
  }

  public boolean isNotifyAborted() {
    return notifyAborted;
  }

  @DataBoundSetter
  public void setNotifyAborted(boolean notifyAborted) {
    this.notifyAborted = notifyAborted;
  }

  public boolean isNotifyNotBuilt() {
    return notifyNotBuilt;
  }

  @DataBoundSetter
  public void setNotifyNotBuilt(boolean notifyNotBuilt) {
    this.notifyNotBuilt = notifyNotBuilt;
  }

  public boolean isNotifyUnstable() {
    return notifyUnstable;
  }

  @DataBoundSetter
  public void setNotifyUnstable(boolean notifyUnstable) {
    this.notifyUnstable = notifyUnstable;
  }

  public boolean isNotifyFailure() {
    return notifyFailure;
  }

  @DataBoundSetter
  public void setNotifyFailure(boolean notifyFailure) {
    this.notifyFailure = notifyFailure;
  }

  public boolean isNotifyBackToNormal() {
    return notifyBackToNormal;
  }

  @DataBoundSetter
  public void setNotifyBackToNormal(boolean notifyBackToNormal) {
    this.notifyBackToNormal = notifyBackToNormal;
  }

  public boolean isNotifyRepeatedFailure() {
    return notifyRepeatedFailure;
  }

  @DataBoundSetter
  public void setNotifyRepeatedFailure(boolean notifyRepeatedFailure) {
    this.notifyRepeatedFailure = notifyRepeatedFailure;
  }

  public boolean isIncludeTestSummary() {
    return includeTestSummary;
  }

  @DataBoundSetter
  public void setIncludeTestSummary(boolean includeTestSummary) {
    this.includeTestSummary = includeTestSummary;
  }

  public boolean isIncludeTestLog() {
    return includeTestLog;
  }

  @DataBoundSetter
  public void setIncludeTestLog(boolean includeTestLog) {
    this.includeTestLog = includeTestLog;
  }

  @DataBoundSetter
  public void setCommitInfoChoice(CommitInfoChoice commitInfoChoice) {
    this.commitInfoChoice = commitInfoChoice;
  }

  @DataBoundSetter
  public void setRawMessage(final boolean rawMessage) {
    this.rawMessage = rawMessage;
  }

  public boolean isIncludeCustomMessage() {
    return includeCustomMessage;
  }

  @DataBoundSetter
  public void setIncludeCustomMessage(boolean includeCustomMessage) {
    this.includeCustomMessage = includeCustomMessage;
  }

  @DataBoundSetter
  public void setCustomMessage(String customMessage) {
    this.customMessage = customMessage;
  }

  @DataBoundSetter
  public void setAttachments(final List<MessageAttachment> attachments) {
    this.attachments = attachments;
  }

  @DataBoundSetter
  public void setWebhookToken(String webhookToken) {
    this.webhookToken = webhookToken;
  }

  @DataBoundSetter
  public void setWebhookTokenCredentialId(String webhookTokenCredentialId) {
    this.webhookTokenCredentialId = webhookTokenCredentialId;
  }

  @DataBoundConstructor
  public RocketChatNotifier() {
    super();
  }

  @DataBoundSetter
  public void setRocketServerUrl(String rocketServerUrl) {
    this.rocketServerUrl = rocketServerUrl;
  }

  @DataBoundSetter
  public void setTrustSSL(boolean trustSSL) {
    this.trustSSL = trustSSL;
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
  public void setBuildServerUrl(String buildServerUrl) {
    this.buildServerUrl = buildServerUrl;
    if (buildServerUrl == null || buildServerUrl.equalsIgnoreCase("")) {
      JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();
      this.buildServerUrl = jenkinsConfig.getUrl();
    }
    if (buildServerUrl != null && !buildServerUrl.endsWith("/")) {
      this.buildServerUrl = buildServerUrl + "/";
    }
  }

  public RocketChatNotifier(final String rocketServerUrl, final boolean trustSSL, final String username, final String password, final String channel, final String buildServerUrl,
                            final boolean notifyStart, final boolean notifyAborted, final boolean notifyFailure,
                            final boolean notifyNotBuilt, final boolean notifySuccess, final boolean notifyUnstable, final boolean notifyBackToNormal,
                            final boolean notifyRepeatedFailure, final boolean includeTestSummary, final boolean includeTestLog, CommitInfoChoice commitInfoChoice,
                            boolean includeCustomMessage, final boolean rawMessage, String customMessage, List<MessageAttachment> attachments, String webhookToken, String webhookTokenCredentialId) {
    super();
    this.rocketServerUrl = rocketServerUrl;
    this.trustSSL = trustSSL;
    this.username = username;
    this.password = password;
    this.buildServerUrl = buildServerUrl;
    this.channel = channel;
    this.notifyStart = notifyStart;
    this.notifyAborted = notifyAborted;
    this.notifyFailure = notifyFailure;
    this.notifyNotBuilt = notifyNotBuilt;
    this.notifySuccess = notifySuccess;
    this.notifyUnstable = notifyUnstable;
    this.notifyBackToNormal = notifyBackToNormal;
    this.notifyRepeatedFailure = notifyRepeatedFailure;
    this.includeTestSummary = includeTestSummary;
    this.includeTestLog = includeTestLog;
    this.commitInfoChoice = commitInfoChoice;
    this.includeCustomMessage = includeCustomMessage;
    this.rawMessage = rawMessage;
    this.customMessage = customMessage;
    this.attachments = attachments;
    this.webhookToken = webhookToken;
    this.webhookTokenCredentialId = webhookTokenCredentialId;
  }

  public BuildStepMonitor getRequiredMonitorService() {
    return BuildStepMonitor.NONE;
  }

  public RocketClient newRocketChatClient(AbstractBuild r, BuildListener listener) throws RocketClientException {
    String serverUrl = this.rocketServerUrl;
    if (StringUtils.isEmpty(serverUrl)) {
      serverUrl = getDescriptor().getRocketServerUrl();
    }
    String username = this.username;
    if (StringUtils.isEmpty(username)) {
      username = getDescriptor().getUsername();
    }
    String password = this.password;
    if (StringUtils.isEmpty(password)) {
      password = getDescriptor().getPassword();
    }
    String channel = this.channel;
    if (StringUtils.isEmpty(channel)) {
      channel = getDescriptor().getChannel();
    }
    String webhookTokenCredentialId = this.webhookTokenCredentialId;
    if (StringUtils.isEmpty(webhookTokenCredentialId)) {
      webhookTokenCredentialId = getDescriptor().getWebhookTokenCredentialId();
    }
    String webhookToken = this.webhookToken;
    if (StringUtils.isEmpty(webhookToken)) {
      webhookToken = getDescriptor().getWebhookToken();
    }

    EnvVars env;
    try {
      env = r.getEnvironment(listener);
    } catch (Exception e) {
      listener.getLogger().println("Error retrieving environment vars: " + e.getMessage());
      env = new EnvVars();
    }
    serverUrl = env.expand(serverUrl);
    username = env.expand(username);
    password = env.expand(password);

    return getRocketClient(serverUrl, username, password, channel, webhookToken, webhookTokenCredentialId, trustSSL);
  }

  public static RocketClient getRocketClient(String serverUrl, String username, String password, String channel, String webhookToken, String webhookTokenCredentialId, boolean trustSSL) throws RocketClientException {
    if (!StringUtils.isEmpty(webhookToken) || !StringUtils.isEmpty(webhookTokenCredentialId)) {
      return new RocketClientWebhookImpl(serverUrl, trustSSL, webhookToken, webhookTokenCredentialId, channel);
    }
    return new RocketClientImpl(serverUrl, trustSSL, username, password, channel);
  }

  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
    Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
    for (Publisher publisher : map.values()) {
      if (publisher instanceof RocketChatNotifier) {
        LOGGER.info("Invoking Completed...");
        new ActiveNotifier((RocketChatNotifier) publisher, listener).completed(build);
      }
    }
    return true;
  }

  @Override
  public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
    if (notifyStart) {
      Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
      for (Publisher publisher : map.values()) {
        if (publisher instanceof RocketChatNotifier) {
          LOGGER.info("Invoking Started...");
          new ActiveNotifier((RocketChatNotifier) publisher, listener).started(build);
        }
      }
    }
    return super.prebuild(build, listener);
  }

  @Extension
  public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

    private String rocketServerUrl;
    private boolean trustSSL;
    private String username;
    private String password;
    private String channel;
    private String buildServerUrl;
    private String webhookToken;
    private String webhookTokenCredentialId;

    public static final CommitInfoChoice[] COMMIT_INFO_CHOICES = CommitInfoChoice.values();

    public DescriptorImpl() {
      load();
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

    public String getWebhookToken() {
      return webhookToken;
    }

    public String getWebhookTokenCredentialId() {
      return webhookTokenCredentialId;
    }


    public String getBuildServerUrl() {
      if (buildServerUrl == null || buildServerUrl.equalsIgnoreCase("")) {
        JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();
        return jenkinsConfig.getUrl();
      } else {
        return buildServerUrl;
      }
    }

    @DataBoundSetter
    public void setRocketServerUrl(String rocketServerUrl) {
      this.rocketServerUrl = rocketServerUrl;
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
    public void setBuildServerUrl(String buildServerUrl) {
      this.buildServerUrl = buildServerUrl;
      if (buildServerUrl == null || buildServerUrl.equalsIgnoreCase("")) {
        JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();
        this.buildServerUrl = jenkinsConfig.getUrl();
      }
      if (buildServerUrl != null && !buildServerUrl.endsWith("/")) {
        this.buildServerUrl = buildServerUrl + "/";
      }
    }

    @DataBoundSetter
    public void setTrustSSL(boolean trustSSL) {
      this.trustSSL = trustSSL;
    }

    @DataBoundSetter
    public void setWebhookToken(String webhookToken) {
      this.webhookToken = webhookToken;
    }

    @DataBoundSetter
    public void setWebhookTokenCredentialId(String webhookTokenCredentialId) {
      this.webhookTokenCredentialId = webhookTokenCredentialId;
    }

    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
      return true;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) {
      req.bindJSON(this, json);
      save();
      return true;
    }

    @Override
    public String getDisplayName() {
      return "RocketChat Notifications";
    }

    @RequirePOST
    public FormValidation doTestConnection(@QueryParameter("rocketServerUrl") final String rocketServerUrl,
                                           @QueryParameter("trustSSL") final String trustSSL,
                                           @QueryParameter("username") final String username,
                                           @QueryParameter("password") final String password,
                                           @QueryParameter("channel") final String channel,
                                           @QueryParameter("buildServerUrl") final String buildServerUrl,
                                           @QueryParameter("webhookToken") final String token,
                                           @QueryParameter("webhookTokenCredentialId") final String webhookTokenCredentialId) throws FormException {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      try {
        String targetServerUrl = rocketServerUrl + RocketClientImpl.API_PATH;
        if (StringUtils.isEmpty(rocketServerUrl)) {
          targetServerUrl = this.rocketServerUrl;
        }
        boolean targetTrustSSL = this.trustSSL;
        if (StringUtils.isNotEmpty(trustSSL)) {
          targetTrustSSL = BooleanUtils.toBoolean(trustSSL);
        }
        String targetUsername = username;
        if (StringUtils.isEmpty(targetUsername)) {
          targetUsername = this.username;
        }
        String targetPassword = password;
        if (StringUtils.isEmpty(targetPassword)) {
          targetPassword = this.password;
        }
        String targetChannel = channel;
        if (StringUtils.isEmpty(targetChannel)) {
          targetChannel = this.channel;
        }
        String targetBuildServerUrl = buildServerUrl;
        if (StringUtils.isEmpty(targetBuildServerUrl)) {
          targetBuildServerUrl = this.buildServerUrl;
        }
        String targetWebhookToken = token;
        if (StringUtils.isEmpty(targetWebhookToken)) {
          targetWebhookToken = this.webhookToken;
        }
        String targetWebhookTokenCredentialId = webhookTokenCredentialId;
        if (StringUtils.isEmpty(targetWebhookTokenCredentialId)) {
          targetWebhookTokenCredentialId = this.webhookTokenCredentialId;
        }

        RocketClient rocketChatClient;
        if (!StringUtils.isEmpty(targetWebhookToken) || !StringUtils.isEmpty(targetWebhookTokenCredentialId)) {
          rocketChatClient = new RocketClientWebhookImpl(targetServerUrl, targetTrustSSL, targetWebhookToken, targetWebhookTokenCredentialId, channel);
        } else {
          rocketChatClient = new RocketClientImpl(targetServerUrl, targetTrustSSL, targetUsername, targetPassword, targetChannel);
        }
        String message = "RocketChat/Jenkins plugin: you're all set on " + targetBuildServerUrl;
        LOGGER.fine("Start validating config");
        rocketChatClient.validate();
        LOGGER.fine("Done validating config");
        LOGGER.fine("Start publishing message");
        rocketChatClient.publish(message, null);
        LOGGER.fine("Done publishing message");
        return FormValidation.ok("Success");
      } catch (Exception e) {
        if (e.getCause() != null &&
          (e.getCause().getClass() == SSLHandshakeException.class || e.getCause().getClass() == CertificateException.class)) {
          LOGGER.log(Level.SEVERE, "SSL error during trying to send rocket message", e);
          return FormValidation.error(e, "SSL error", e);
        } else {
          LOGGER.log(Level.SEVERE, "Client error during trying to send rocket message", e);
          return FormValidation.error(e, "Client error - Could not send message");
        }
      }
    }

    public ListBoxModel doFillWebhookTokenCredentialIdItems() {
      if (!Jenkins.getInstanceOrNull().hasPermission(Jenkins.ADMINISTER)) {
        return new ListBoxModel();
      }
      return new StandardListBoxModel()
        .withEmptySelection()
        .withAll(lookupCredentials(
          StringCredentials.class,
          Jenkins.getInstanceOrNull(),
          ACL.SYSTEM,
          Collections.<DomainRequirement>emptyList())
        );
    }

    //WARN users that they should not use the plain/exposed token, but rather the token credential id
    public FormValidation doCheckWebhookToken(@QueryParameter String value) {
      if (StringUtils.isEmpty(value)) {
        return FormValidation.ok();
      }
      return FormValidation.warning("Exposing your Integration Token is a security risk. Please use the Webhook Token Credential ID");
    }
  }

  @Deprecated
  public static class RocketJobProperty extends hudson.model.JobProperty<AbstractProject<?, ?>> {

    private String rocketServerUrl;
    private String username;
    private String password;
    private String channel;
    private boolean trustSSL;
    private boolean notifyStart;
    private boolean notifySuccess;
    private boolean notifyAborted;
    private boolean notifyNotBuilt;
    private boolean notifyUnstable;
    private boolean notifyFailure;
    private boolean notifyBackToNormal;
    private boolean notifyRepeatedFailure;
    private boolean includeTestSummary;
    private boolean includeTestLog;
    private boolean showCommitList;
    private boolean includeCustomMessage;
    private String customMessage;

    @DataBoundConstructor
    public RocketJobProperty(String rocketServerUrl,
                             boolean trustSSL,
                             String username,
                             String password,
                             String channel,
                             boolean notifyStart,
                             boolean notifyAborted,
                             boolean notifyFailure,
                             boolean notifyNotBuilt,
                             boolean notifySuccess,
                             boolean notifyUnstable,
                             boolean notifyBackToNormal,
                             boolean notifyRepeatedFailure,
                             boolean includeTestSummary,
                             boolean includeTestLog,
                             boolean showCommitList,
                             boolean includeCustomMessage,
                             String customMessage) {
      this.rocketServerUrl = rocketServerUrl;
      this.trustSSL = trustSSL;
      this.username = username;
      this.password = password;
      this.channel = channel;
      this.notifyStart = notifyStart;
      this.notifyAborted = notifyAborted;
      this.notifyFailure = notifyFailure;
      this.notifyNotBuilt = notifyNotBuilt;
      this.notifySuccess = notifySuccess;
      this.notifyUnstable = notifyUnstable;
      this.notifyBackToNormal = notifyBackToNormal;
      this.notifyRepeatedFailure = notifyRepeatedFailure;
      this.includeTestSummary = includeTestSummary;
      this.includeTestLog = includeTestLog;
      this.showCommitList = showCommitList;
      this.includeCustomMessage = includeCustomMessage;
      this.customMessage = customMessage;
    }

    @Exported
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
      return notifyStart;
    }

    @Exported
    public boolean getNotifySuccess() {
      return notifySuccess;
    }

    @Exported
    public boolean getShowCommitList() {
      return showCommitList;
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
