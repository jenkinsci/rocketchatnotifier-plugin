package jenkins.plugins.rocketchatnotifier.model;

import java.util.Arrays;

public class Response {
  private boolean success;
  private String version;
  private Message[] messages;
  private Message message;
  private User[] users;
  private User user;
  private Room[] channels;
  private Room channel;
  private String error;

  public void setSuccess(boolean result) {
    this.success = result;
  }

  public boolean isSuccessful() {
    return this.success;
  }

  public void setMessages(final Message[] messages) {
    this.messages = messages.clone();
  }

  public Message[] getMessages() {
    return this.messages.clone();
  }

  public boolean isMessages() {
    return this.messages != null;
  }

  public void setMessage(final Message message) {
    this.message = message;
  }

  public Message getMessage() {
    return this.message;
  }

  public boolean isMessage() {
    return this.message != null;
  }

  public void setUsers(final User[] users) {
    this.users = users.clone();
  }

  public User[] getUsers() {
    return this.users.clone();
  }

  public Room[] getChannels() {
    return this.channels.clone();
  }

  public boolean isUsers() {
    return this.users != null;
  }

  public void setUser(final User user) {
    this.user = user;
  }

  public User getUser() {
    return this.user;
  }

  public boolean isUser() {
    return this.user != null;
  }

  public String getVersion() {
    return this.version;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  @Override
  public String toString() {
    return "Response{"
        + "success="
        + success
        + ", messages="
        + Arrays.toString(messages)
        + ", message="
        + message
        + ", users="
        + Arrays.toString(users)
        + ", user="
        + user
        + ", channels="
        + Arrays.toString(channels)
        + ", channel="
        + channel
        + ", version="
        + version
        + ", error="
        + error
        + "}";
  }
}
