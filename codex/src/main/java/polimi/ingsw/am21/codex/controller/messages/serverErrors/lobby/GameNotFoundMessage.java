package polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class GameNotFoundMessage extends ErrorMessage {

  private final String gameId;

  public GameNotFoundMessage(String gameId) {
    super(MessageType.GAME_NOT_FOUND);
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
