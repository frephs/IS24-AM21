package polimi.ingsw.am21.codex.controller.messages;

import java.io.Serializable;

public abstract class Message implements Serializable {

  /**
   * Describe the message to the client
   */
  private String message;

  private final MessageType type;

  public Message(MessageType type) {
    super();
    this.type = type;
  }

  @Override
  public String toString() {
    return "Message{" + "message='" + message + '\'' + '}';
  }

  public MessageType getType() {
    return this.type;
  }
}
