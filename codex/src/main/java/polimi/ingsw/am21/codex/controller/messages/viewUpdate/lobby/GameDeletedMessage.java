package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class GameDeletedMessage extends ViewUpdatingMessage {

  private final String gameId;

  public GameDeletedMessage(String gameId) {
    super(MessageType.GAME_DELETED);
    this.gameId = gameId;
  }

  public String getGameId() {
    return gameId;
  }

  @Override
  public String toString() {
    return (getType() + "{" + "gameId='" + gameId + "'}");
  }
}
