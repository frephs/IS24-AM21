package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerLeftLobbyMessage extends ViewUpdatingMessage {

  private final String lobbyId;
  private final UUID connectionID;

  public PlayerLeftLobbyMessage(String lobbyId, UUID connectionID) {
    super(MessageType.PLAYER_LEFT_LOBBY);
    this.lobbyId = lobbyId;
    this.connectionID = connectionID;
  }

  public String getLobbyId() {
    return lobbyId;
  }

  public UUID getConnectionID() {
    return connectionID;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "lobbyId='" +
      lobbyId +
      '\'' +
      ", connectionID=" +
      connectionID +
      '}'
    );
  }
}
