package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerGameJoinMessage extends ViewUpdatingMessage {

  public int lobbyId;

  public PlayerGameJoinMessage() {
    super(MessageType.PLAYER_GAME_JOIN);
  }
}
