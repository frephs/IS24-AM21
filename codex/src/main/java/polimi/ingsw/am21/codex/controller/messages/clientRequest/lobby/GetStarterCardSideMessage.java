package polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.RequestMessage;

public class GetStarterCardSideMessage extends RequestMessage {

  public GetStarterCardSideMessage() {
    super(MessageType.GET_STARTER_CARD_SIDE);
  }
}
