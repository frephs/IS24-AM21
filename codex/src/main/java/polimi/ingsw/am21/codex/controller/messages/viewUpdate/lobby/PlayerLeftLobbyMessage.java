package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerLeftLobbyMessage extends ViewUpdatingMessage {

  private final String lobbyId;
  private final UUID socketId;

  public PlayerLeftLobbyMessage(String lobbyId, UUID socketId) {
    super(MessageType.PLAYER_LEFT_LOBBY);
    this.lobbyId = lobbyId;
    this.socketId = socketId;
  }

  public String getLobbyId() {
    return lobbyId;
  }

  public UUID getSocketId() {
    return socketId;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "lobbyId='" +
      lobbyId +
      '\'' +
      ", socketId=" +
      socketId +
      '}'
    );
  }
}
