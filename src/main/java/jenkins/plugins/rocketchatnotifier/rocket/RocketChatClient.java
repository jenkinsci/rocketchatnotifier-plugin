package jenkins.plugins.rocketchatnotifier.rocket;

import jenkins.plugins.rocketchatnotifier.model.Info;
import jenkins.plugins.rocketchatnotifier.model.Room;
import jenkins.plugins.rocketchatnotifier.model.User;
import jenkins.plugins.rocketchatnotifier.rocket.errorhandling.RocketClientException;
import sun.security.validator.ValidatorException;

import java.util.List;
import java.util.Map;

/**
 * API used by this Jenkins plugin to communicate to RocketChat server backend
 *
 * @author Martin Reinhardt (hypery2k)
 */
public interface RocketChatClient {
  /**
   * Gets <strong>all</strong> of the users from a Rocket.Chat server, if you have a ton this will
   * take some time.
   *
   * @return an array of {@link User}s
   * @throws RocketClientException is thrown if there was a problem connecting, including if the result
   *                               wasn't successful
   */
  User[] getUsers() throws RocketClientException;

  /**
   * Retrieves a {@link User} from the Rocket.Chat server.
   *
   * @param userId of the user to retrieve
   * @return an instance of the {@link User}
   * @throws RocketClientException is thrown if there was a problem connecting, including if the result
   *                               wasn't successful or there is no user
   */
  User getUser(String userId) throws RocketClientException;

  /**
   * @return an array of channels as room object
   * @throws RocketClientException in case of communication errors with the RocketChat server backend
   */
  Room[] getChannels() throws RocketClientException;

  /**
   * sends a message to a channel
   *
   * @param room    to use (aka channel)
   * @param message to send
   * @throws ValidatorException    in case of SSL errors
   * @throws RocketClientException in case of communication errors with the RocketChat server backend
   */
  void send(Room room, String message) throws ValidatorException, RocketClientException;

  /**
   * sends a message to a channel
   *
   * @param channelName to use
   * @param message     to send
   * @throws ValidatorException    in case of SSL errors
   * @throws RocketClientException in case of communication errors with the RocketChat server backend
   */
  void send(String channelName, String message) throws ValidatorException, RocketClientException;

  /**
   * sends a message to a channel.
   * If emoji and avatar are defined, only emoji will be displayed
   *
   * @param channelName to use
   * @param message     to send
   * @param emoji       to display
   * @param avatar      to display
   * @throws ValidatorException    in case of SSL errors
   * @throws RocketClientException in case of communication errors with the RocketChat server backend
   */
  void send(String channelName, String message, String emoji, String avatar) throws ValidatorException, RocketClientException;

  /**
   * sends a message to a channel.
   * If emoji and avatar are defined, only emoji will be displayed
   *
   * @param channelName to use
   * @param message     to send
   * @param emoji       to display
   * @param avatar      to display
   * @param attachments to send
   * @throws ValidatorException    in case of SSL errors
   * @throws RocketClientException in case of communication errors with the RocketChat server backend
   */
  void send(String channelName, String message, String emoji, String avatar, List<Map<String, Object>> attachments)
    throws ValidatorException, RocketClientException;

  /**
   * Retrieves server information
   *
   * @return Info
   * @throws RocketClientException in case of communications errors
   */
  Info getInfo() throws RocketClientException;

}
