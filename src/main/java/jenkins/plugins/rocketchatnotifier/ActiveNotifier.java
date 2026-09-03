package jenkins.plugins.rocketchatnotifier;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;
import hudson.triggers.SCMTrigger;
import jenkins.plugins.rocketchatnotifier.model.MessageAttachment;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Martin Reinhardt (hypery2k)
 */
public class ActiveNotifier implements FineGrainedNotifier {

  private static final Logger LOGGER = Logger.getLogger(RocketChatNotifier.class.getName());

  RocketChatNotifier notifier;
  BuildListener listener;

  public ActiveNotifier(RocketChatNotifier notifier, BuildListener listener) {
    super();
    this.notifier = notifier;
    this.listener = listener;
  }

  private RocketClient getRocket(AbstractBuild r) throws RocketClientException {
    return notifier.newRocketChatClient(r, listener);
  }

  public void deleted(AbstractBuild r) {
  }

  public void started(AbstractBuild build) {

    try {
      CauseAction causeAction = build.getAction(CauseAction.class);

      if (causeAction != null) {
        Cause scmCause = causeAction.findCause(SCMTrigger.SCMTriggerCause.class);
        if (scmCause == null) {
          MessageBuilder message = new MessageBuilder(notifier, build, false);
          message.append(causeAction.getShortDescription());
          notifyStart(build, message.appendOpenLink().toString());
          // Cause was found, exit early to prevent double-message
          return;
        }
      }

      String changes = getChanges(build, notifier.includeCustomMessage(), false);
      if (changes != null) {
        notifyStart(build, changes);
      }
      else {
        notifyStart(build, getBuildStatusMessage(build, false, notifier.includeCustomMessage(), false));
      }
    }
    catch (RocketClientException e) {
      LOGGER.warning("Could not send rocket message: " + e.getMessage());
    }
  }

  private void notifyStart(AbstractBuild build, String message) throws RocketClientException {
    getRocket(build).publish(message, MessageAttachment.convertMessageAttachmentsToMaps(this.notifier.getAttachments()));
  }

  public void finalized(AbstractBuild r) {
  }

  public void completed(AbstractBuild r) {
    try {
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("Build completed. Checking for rocket notifiers");
      }
      if (r != null) {
        AbstractProject<?, ?> project = r.getProject();
        Result result = r.getResult();
        if (project != null) {
          AbstractBuild<?, ?> previousBuild = project.getLastBuild();
          if (previousBuild != null) {
            do {
              previousBuild = previousBuild.getPreviousCompletedBuild();
            } while (previousBuild != null && previousBuild.getResult() == Result.ABORTED);
            Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
            if ((result == Result.ABORTED && notifier.getNotifyAborted())
              || (result == Result.FAILURE //notify only on single failed build
              && previousResult != Result.FAILURE
              && notifier.getNotifyFailure())
              || (result == Result.FAILURE //notify only on repeated failures
              && previousResult == Result.FAILURE
              && notifier.getNotifyRepeatedFailure())
              || (result == Result.NOT_BUILT && notifier.getNotifyNotBuilt())
              || (result == Result.SUCCESS
              && (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE)
              && notifier.getNotifyBackToNormal())
              || (result == Result.SUCCESS && notifier.getNotifySuccess())
              || (result == Result.UNSTABLE && notifier.getNotifyUnstable())) {
              getRocket(r).publish(getBuildStatusMessage(r, notifier.includeTestSummary(),
                notifier.includeCustomMessage(), true), MessageAttachment.convertMessageAttachmentsToMaps(this.notifier.getAttachments()));//, getBuildColor(r));
              if (notifier.getCommitInfoChoice() != null &&
                notifier.getCommitInfoChoice().showAnything()) {
                getRocket(r).publish(getCommitList(r), MessageAttachment.convertMessageAttachmentsToMaps(this.notifier.getAttachments()));//, getBuildColor(r));
              }
              if (result == Result.FAILURE && notifier.includeTestLog()) {
                getRocket(r).publish(getLog(r), MessageAttachment.convertMessageAttachmentsToMaps(this.notifier.getAttachments()));//, getBuildColor(r));
              }
            }
          }
        }
      }
    }
    catch (RocketClientException e) {
      LOGGER.warning("Could not send rocket message: " + e.getMessage());
    }
  }

  String getChanges(AbstractBuild r, boolean includeCustomMessage, boolean finished) {
    if (!r.hasChangeSetComputed()) {
      LOGGER.info("No change set computed...");
      return null;
    }
    ChangeLogSet changeSet = r.getChangeSet();
    List<Entry> entries = new LinkedList<Entry>();
    Set<AffectedFile> files = new HashSet<AffectedFile>();
    for (Object o : changeSet.getItems()) {
      Entry entry = (Entry) o;
      LOGGER.info("Entry " + o);
      entries.add(entry);
      files.addAll(entry.getAffectedFiles());
    }
    if (entries.isEmpty()) {
      LOGGER.info("Empty change...");
      return null;
    }
    Set<String> authors = new HashSet<String>();
    for (Entry entry : entries) {
      authors.add(entry.getAuthor().getDisplayName());
    }
    MessageBuilder message = new MessageBuilder(notifier, r, finished);
    message.append("Started by changes from ");
    message.append(StringUtils.join(authors, ", "));
    message.append(" (");
    message.append(files.size());
    message.append(" file(s) changed)");
    message.appendOpenLink();
    if (includeCustomMessage) {
      message.appendCustomMessage();
    }
    return message.toString();
  }

  String getLog(AbstractBuild r) {
    MessageBuilder message = new MessageBuilder(notifier, r, true);
    message.appendLog();
    return message.toString();
  }

  String getCommitList(AbstractBuild r) {
    ChangeLogSet changeSet = r.getChangeSet();
    List<Entry> entries = new LinkedList<Entry>();
    for (Object o : changeSet.getItems()) {
      Entry entry = (Entry) o;
      LOGGER.info("Entry " + o);
      entries.add(entry);
    }
    if (entries.isEmpty()) {
      LOGGER.info("Empty change...");
      Cause.UpstreamCause c = (Cause.UpstreamCause) r.getCause(Cause.UpstreamCause.class);
      if (c == null) {
        return "No Changes.";
      }
      String upProjectName = c.getUpstreamProject();
      int buildNumber = c.getUpstreamBuild();
      AbstractProject project = Hudson.getInstance().getItemByFullName(upProjectName, AbstractProject.class);
      if (project != null) {
        AbstractBuild upBuild = project.getBuildByNumber(buildNumber);
        return getCommitList(upBuild);
      }
    }
    Set<String> commits = new HashSet<String>();
    for (Entry entry : entries) {
      StringBuffer commit = new StringBuffer();
      CommitInfoChoice commitInfoChoice = notifier.getCommitInfoChoice();
      if (commitInfoChoice.showTitle()) {
        commit.append(entry.getMsg());
      }
      if (commitInfoChoice.showAuthor()) {
        commit.append(" [").append(entry.getAuthor().getDisplayName()).append("]");
      }
      commits.add(commit.toString());
    }
    MessageBuilder message = new MessageBuilder(notifier, r, true);
    message.append("Changes:\n- ");
    message.append(StringUtils.join(commits, "\n- "));
    return message.toString();
  }

  String getBuildStatusMessage(AbstractBuild r, boolean includeTestSummary, boolean includeCustomMessage, boolean finished) {
    MessageBuilder message = new MessageBuilder(notifier, r, finished);
    message.appendStatusMessage();
    message.appendDuration();
    message.appendOpenLink();
    if (includeTestSummary) {
      message.appendTestSummary();
    }
    if (includeCustomMessage) {
      message.appendCustomMessage();
    }
    return message.toString();
  }

}
