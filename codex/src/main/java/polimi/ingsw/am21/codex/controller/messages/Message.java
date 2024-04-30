package polimi.ingsw.am21.codex.controller.messages;

import java.io.Serializable;

public abstract class Message implements Serializable {
  /**
   * Describe the message to the client
   */
  private String message;

  @Override
  public String toString() {
    return "Message{" +
      "message='" + message + '\'' +
      '}';
  }
}
