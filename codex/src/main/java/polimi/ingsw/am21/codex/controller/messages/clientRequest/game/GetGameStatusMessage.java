package polimi.ingsw.am21.codex.controller.messages.clientRequest.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.RequestMessage;

public class GetGameStatusMessage extends RequestMessage {

  public GetGameStatusMessage() {
    super(MessageType.GET_GAME_STATUS);
  }
}
