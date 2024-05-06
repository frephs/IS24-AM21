package polimi.ingsw.am21.codex.controller.messages.server.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ResponseMessage;

public class AvailableGameLobbieMessage extends ResponseMessage {

  public AvailableGameLobbieMessage() {
    super(MessageType.AVAILABLE_GAME_LOBBIES);
  }
}
