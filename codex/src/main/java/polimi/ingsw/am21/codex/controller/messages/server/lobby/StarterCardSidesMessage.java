package polimi.ingsw.am21.codex.controller.messages.server.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ResponseMessage;

public class StarterCardSidesMessage extends ResponseMessage {

  public StarterCardSidesMessage() {
    super(MessageType.STARTER_CARD_SIDES);
  }
}
