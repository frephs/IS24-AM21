package polimi.ingsw.am21.codex.controller.messages.clientActions.game;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class GameActionMessage extends ActionMessage {

  private final String gameId;
  private final String nickname;

  public GameActionMessage(
    MessageType type,
    UUID connectionID,
    String gameId,
    String nickname
  ) {
    super(type, connectionID);
    this.gameId = gameId;
    this.nickname = nickname;
  }

  public String getGameId() {
    return gameId;
  }

  public String getPlayerNickname() {
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
