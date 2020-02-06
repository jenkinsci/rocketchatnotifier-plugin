package jenkins.plugins.rocketchatnotifier.rocket;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import jenkins.plugins.rocketchatnotifier.RocketClientImpl;
import jenkins.plugins.rocketchatnotifier.model.Info;
import jenkins.plugins.rocketchatnotifier.model.Response;
import jenkins.plugins.rocketchatnotifier.model.Room;
import jenkins.plugins.rocketchatnotifier.model.User;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONValue;
import sun.security.validator.ValidatorException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Client for Rocket.Chat which relies on the REST API v1.
 * <p>
 * Please note, this client does <strong>not</strong> cache any of the results.
 *
 * @version 0.1.0
 * @since 0.0.1
 */
public class RocketChatClientImpl implements RocketChatClient {

  private static final Logger LOG = Logger.getLogger(RocketClientImpl.class.getName());
  private RocketChatClientCallBuilder callBuilder;

  /**
   * Initialize a new instance of the client providing the server's url along with username and
   * password to use.
   *
   * @param serverUrl of the Rocket.Chat server, with or without it ending in "/api/"
   * @param trustSSL  if set set the SSL certificate of the rpcket server will not be checked
   * @param user      which to authenticate with
   * @param password  of the user to authenticate with
   */
  public RocketChatClientImpl(String serverUrl, boolean trustSSL, String user, String password) throws RocketClientException {
    this.callBuilder = new RocketChatClientCallBuilder(serverUrl, trustSSL, JSONValue.escape(user), JSONValue.escape(password));
  }

  /**
   * Initialize a new instance of the client providing the server's url along with webhook for
   * posting notifications.
   *
   * @param serverUrl    of the Rocket.Chat server, with or without it ending in "/api/"
   * @param trustSSL     if set set the SSL certificate of the rocket server will not be checked
   * @param webhookToken authentication token or URL
   */
  public RocketChatClientImpl(String serverUrl, boolean trustSSL, String webhookToken) throws RocketClientException {
    LOG.info("Creating new instance for rocket " + serverUrl + " (trustSSL: " + trustSSL + ")");
    this.callBuilder = new RocketChatClientCallBuilder(serverUrl, trustSSL, webhookToken);
  }

  @Override
  public User[] getUsers() throws RocketClientException {
    Response res = this.callBuilder.buildCall(RocketChatRestApiV1.UsersList);

    if (!res.isSuccessful()) {
      LOG.severe("Could not read users information: " + res.getMessage().getMsg());
      throw new RocketClientException("The call to get the User's Information was unsuccessful.");
    }
    if (!res.isUsers()) {
      LOG.severe("Failed to read users information: " + res.getMessage().getMsg());
      throw new RocketClientException("Get User Information failed to retrieve a user.");
    }
    return res.getUsers();
  }

  @Override
  public User getUser(String userId) throws RocketClientException {
    Response res = this.callBuilder.buildCall(RocketChatRestApiV1.UsersInfo,
      new RocketChatQueryParams("userId", userId));

    if (!res.isSuccessful()) {
      LOG.severe("Could not read user information: " + res.getMessage().getMsg());
      throw new RocketClientException("The call to get the User's Information was unsuccessful.");
    }
    if (!res.isUser()) {
      LOG.severe("Failed to read users information: " + res.getMessage().getMsg());
      throw new RocketClientException("Get User Information failed to retrieve a user.");
    }
    return res.getUser();
  }

  @Override
  public Room[] getChannels() throws RocketClientException {
    Response res = this.callBuilder.buildCall(RocketChatRestApiV1.ChannelsList);

    if (!res.isSuccessful()) {
      LOG.severe("Could not read channels information: " + res.getMessage().getMsg());
      throw new RocketClientException("The call to get the all Channel Information was unsuccessful.");
    }
    return res.getChannels();
  }

  @Override
  public String getInfo() throws RocketClientException {
    Response res;
    try {
      res = this.callBuilder.buildCall(RocketChatRestApiV1.Info);
    } catch (Exception e) {
      // FIXME, drop usage in future plugin releases
      // fallback to old path
      GetRequest req = Unirest.get(this.callBuilder.getServerUrl() + "/api/v1/info");
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      try {
        res = objectMapper.readValue(req.asString().getBody(), Response.class);
      } catch (IOException | UnirestException ex) {
        throw new RocketClientException(e);
      }
    }
    if (res.isSuccessful()) {
      return res.getVersion();
    }
    LOG.severe("Could not read information: " + res);
    throw new RocketClientException("The call to get informations was unsuccessful.");
  }

  @Override
  public void send(Room room, String message) throws ValidatorException, RocketClientException {
    this.send(room.getName(), message);
  }

  @Override
  public void send(String channelName, String message) throws ValidatorException, RocketClientException {
    this.send(channelName, message, null, null);
  }

  @Override
  public void send(final String channelName, final String message, final String emoji, final String avatar)
    throws ValidatorException, RocketClientException {
    this.send(channelName, message, emoji, avatar, null);
  }

  @Override
  public void send(final String channelName, final String message, final String emoji, final String avatar, final List<Map<String, Object>> attachments)
    throws ValidatorException, RocketClientException {
    if (!channelName.contains(",")) {
      sendSingleMessage(channelName, message, emoji, avatar, attachments);
      return;
    }

    for (String singleChanelName : channelName.split(",")) {
      sendSingleMessage(singleChanelName, message, emoji, avatar, attachments);
    }
  }

  private void sendSingleMessage(final String singleChannelName, final String message, final String emoji, final String avatar, final List<Map<String, Object>> attachments) throws RocketClientException {
    Map body = new HashMap<String, String>();
    if (!StringUtils.isEmpty(singleChannelName)) {
      String targetChannelName = singleChannelName.trim();
      if (!targetChannelName.matches("^[@#].+")) {
        targetChannelName = "#" + targetChannelName;
      }

      body.put("channel", targetChannelName);
    }

    body.put("text", message);
    if (emoji != null) {
      body.put("emoji", emoji);
    } else if (avatar != null) {
      body.put("avatar", avatar);
    }
    if (attachments != null && attachments.size() > 0) {
      body.put("attachments", attachments);
    }
    final Response res = this.callBuilder.buildCall(RocketChatRestApiV1.PostMessage, null, body);

    if (res.isSuccessful()) {
      LOG.fine("Message sent was successfull.");
    } else {
      LOG.severe("Could not send message: " + res);
      throw new RocketClientException("The send of the message was unsuccessful. " + res);
    }
  }

}
