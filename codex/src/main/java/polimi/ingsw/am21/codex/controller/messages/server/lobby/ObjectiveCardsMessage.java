package polimi.ingsw.am21.codex.controller.messages.server.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ResponseMessage;

public class ObjectiveCardsMessage extends ResponseMessage {

  public ObjectiveCardsMessage() {
    super(MessageType.OBJECTIVE_CARDS);
  }
}
