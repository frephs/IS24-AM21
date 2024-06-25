package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class PlayerSetTokenColorMessage extends ViewUpdatingMessage {

  private final String gameId;
  private final UUID connectionID;
  private final TokenColor color;
  private final String nickname;

  public PlayerSetTokenColorMessage(
    String gamedId,
    UUID connectionID,
    String nickname,
    TokenColor color
  ) {
    super(MessageType.PLAYER_SET_TOKEN_COLOR);
    this.gameId = gamedId;
    this.connectionID = connectionID;
    this.color = color;
    this.nickname = nickname;
  }

  public String getGameId() {
    return gameId;
  }

  public UUID getConnectionID() {
    return connectionID;
  }

  public TokenColor getColor() {
    return color;
  }

  public String getNickname() {
    return nickname;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "gameId='" +
      gameId +
      "', connectionID=" +
      connectionID +
      ", color=" +
      color +
      ", nickname='" +
      nickname +
      '}'
    );
  }
}
