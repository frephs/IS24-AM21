package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class PlayerSetTokenColorMessage extends ViewUpdatingMessage {

  private final String gameId;
  private final UUID socketId;
  private final TokenColor color;

  public PlayerSetTokenColorMessage(
    String gamedId,
    UUID socketId,
    TokenColor color
  ) {
    super(MessageType.PLAYER_SET_TOKEN_COLOR);
    this.gameId = gamedId;
    this.socketId = socketId;
    this.color = color;
  }

  public String getGameId() {
    return gameId;
  }

  public UUID getSocketId() {
    return socketId;
  }

  public TokenColor getColor() {
    return color;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "gameId='" +
      gameId +
      "', socketId=" +
      socketId +
      ", color=" +
      color +
      '}'
    );
  }
}
