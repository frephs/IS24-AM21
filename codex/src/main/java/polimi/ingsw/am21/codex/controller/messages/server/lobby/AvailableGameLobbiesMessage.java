package polimi.ingsw.am21.codex.controller.messages.server.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ResponseMessage;

public class AvailableGameLobbiesMessage extends ResponseMessage {

  public AvailableGameLobbiesMessage() {
    super(MessageType.AVAILABLE_GAME_LOBBIES);
  }
}
