package polimi.ingsw.am21.codex.controller.messages;

import java.util.UUID;

public abstract class ClientMessage extends Message {

  private final UUID connectionID;

  ClientMessage(MessageType type, UUID connectionID) {
    super(type);
    this.connectionID = connectionID;
  }

  public UUID getConnectionID() {
    return connectionID;
  }
}
