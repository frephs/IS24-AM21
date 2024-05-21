package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import java.util.List;
import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class PlayerJoinedGameMessage extends ViewUpdatingMessage {

  private final String gameId;
  private final UUID socketId;
  private final String nickname;
  private final TokenColor color;
  private final List<Integer> handIDs;

  public PlayerJoinedGameMessage(
    String gameId,
    UUID socketId,
    String nickname,
    TokenColor color,
    List<Integer> handIDs
  ) {
    super(MessageType.PLAYER_JOINED_GAME);
    this.gameId = gameId;
    this.socketId = socketId;
    this.nickname = nickname;
    this.color = color;
    this.handIDs = handIDs;
  }

  public String getGameId() {
    return gameId;
  }

  public UUID getSocketId() {
    return socketId;
  }

  public String getNickname() {
    return nickname;
  }

  public TokenColor getColor() {
    return color;
  }

  public List<Integer> getHandIDs() {
    return handIDs;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "gameId=" +
      gameId +
      ", socketId=" +
      socketId +
      ", nickname='" +
      nickname +
      '\'' +
      ", color=" +
      color +
      ", handIDs=" +
      handIDs +
      '}'
    );
  }
}
