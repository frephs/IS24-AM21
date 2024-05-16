package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerGameJoinMessage extends ViewUpdatingMessage {

  private final int lobbyId;

  public PlayerGameJoinMessage(int lobbyId) {
    super(MessageType.PLAYER_GAME_JOIN);
    this.lobbyId = lobbyId;
  }

  public int getLobbyId() {
    return lobbyId;
  }

  @Override
  public String toString() {
    return getType() + "{" + "lobbyId=" + lobbyId + '}';
  }
}
