package polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby;

import polimi.ingsw.am21.codex.controller.messages.Message;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class GameAlreadyExistsMessage extends Message {

  private String gameId;

  public GameAlreadyExistsMessage(String gameId) {
    super(MessageType.GAME_ALREADY_EXISTS);
    this.gameId = gameId;
  }

  public String getGameId() {
    return gameId;
  }

  @Override
  public String toString() {
    return getType() + "{" + "gameId=" + gameId + '}';
  }
}
