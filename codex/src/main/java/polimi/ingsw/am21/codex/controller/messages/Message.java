package polimi.ingsw.am21.codex.controller.messages;

import java.io.Serializable;

public abstract class Message implements Serializable {

  private final MessageType type;

  public Message(MessageType type) {
    super();
    this.type = type;
  }

  public MessageType getType() {
    return this.type;
  }
}
