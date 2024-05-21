package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import java.util.Set;
import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class PlayerJoinedLobbyMessage extends ViewUpdatingMessage {

  private final String lobbyId;
  private final UUID socketId;
  private final Set<TokenColor> availableTokenColors;

  public PlayerJoinedLobbyMessage(
    String lobbyId,
    UUID socketId,
    Set<TokenColor> availableTokenColors
  ) {
    super(MessageType.PLAYER_JOINED_LOBBY);
    this.lobbyId = lobbyId;
    this.socketId = socketId;
    this.availableTokenColors = availableTokenColors;
  }

  public String getLobbyId() {
    return lobbyId;
  }

  public UUID getSocketId() {
    return socketId;
  }

  public Set<TokenColor> getAvailableTokenColors() {
    return availableTokenColors;
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
      ", availableTokenColors=" +
      availableTokenColors +
      '}'
    );
  }
}
