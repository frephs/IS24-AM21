package polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class GameFullMessage extends ErrorMessage {

  private final String gameId;

  public GameFullMessage(String gameId) {
    super(MessageType.GAME_FULL);
    this.gameId = gameId;
  }

  public String getGameId() {
    return gameId;
  }

  @Override
  public String toString() {
    return getType().toString() + "{ gameId=" + gameId + " }";
  }
}
