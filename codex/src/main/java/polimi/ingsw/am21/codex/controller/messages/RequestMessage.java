package polimi.ingsw.am21.codex.controller.messages;

import java.util.UUID;

public abstract class RequestMessage extends ClientMessage {

  public RequestMessage(MessageType type, UUID connectionID) {
    super(type, connectionID);
  }
}
