package polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.RequestMessage;

public class GetObjectiveCardsMessage extends RequestMessage {

  public GetObjectiveCardsMessage() {
    super(MessageType.GET_OBJECTIVE_CARDS);
  }
}
