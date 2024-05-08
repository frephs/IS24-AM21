package polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.RequestMessage;

public class GetAvailableGameLobbiesMessage extends RequestMessage {

  public GetAvailableGameLobbiesMessage() {
    super(MessageType.GET_AVAILABLE_GAME_LOBBIES);
  }
}
