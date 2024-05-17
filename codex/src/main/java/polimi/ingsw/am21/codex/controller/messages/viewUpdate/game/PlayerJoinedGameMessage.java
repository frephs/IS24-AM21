package polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class PlayerJoinedGameMessage extends ViewUpdatingMessage {

  private final int gameId;

  public PlayerJoinedGameMessage(int gameId) {
    super(MessageType.PLAYER_JOINED_GAME);
    this.gameId = gameId;
  }

  public int getGameId() {
    return gameId;
  }

  @Override
  public String toString() {
    return getType() + "{" + "gameId=" + gameId + '}';
  }
}
