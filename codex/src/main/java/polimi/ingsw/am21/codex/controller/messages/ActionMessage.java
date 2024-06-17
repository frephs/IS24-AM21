package polimi.ingsw.am21.codex.controller.messages;

import java.util.UUID;

public abstract class ActionMessage extends ClientMessage {

  public ActionMessage(MessageType type, UUID connectionID) {
    super(type, connectionID);
  }
}
