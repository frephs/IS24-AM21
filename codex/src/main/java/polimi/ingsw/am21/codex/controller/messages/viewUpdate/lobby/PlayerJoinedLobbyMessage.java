package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerJoinedLobbyMessage extends ViewUpdatingMessage {

  private final int lobbyId;

  public PlayerJoinedLobbyMessage(int lobbyId) {
    super(MessageType.PLAYER_JOINED_LOBBY);
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
