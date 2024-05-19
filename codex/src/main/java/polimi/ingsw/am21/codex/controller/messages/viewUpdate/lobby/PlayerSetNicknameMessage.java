package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerSetNicknameMessage extends ViewUpdatingMessage {

  private final String gameId;
  private final UUID socketId;
  private final String nickname;

  public PlayerSetNicknameMessage(
    String gameId,
    UUID socketId,
    String nickname
  ) {
    super(MessageType.PLAYER_SET_NICKNAME);
    this.gameId = gameId;
    this.socketId = socketId;
    this.nickname = nickname;
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

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "gameId='" +
      gameId +
      '\'' +
      ", nickname='" +
      nickname +
      '\'' +
      '}'
    );
  }
}
