package jenkins.plugins.rocketchatnotifier.model;

public class Message {

  private String msg;

  public Message() {}

  public Message(String messsage) {
    this.msg = messsage;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  @Override
  public String toString() {
    return "Message{msg=" + msg + "}";
  }
}
