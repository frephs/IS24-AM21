package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class GameCreatedMessage extends ViewUpdatingMessage {

  private final String gameId;
  private final int players;

  public GameCreatedMessage(String gameId, int players) {
    super(MessageType.GAME_CREATED);
    this.gameId = gameId;
    this.players = players;
  }

  public String getGameId() {
    return gameId;
  }

  public int getPlayers() {
    return players;
  }

  @Override
  public String toString() {
    return (
      getType() + "{" + "gameId='" + gameId + "', players=" + players + '}'
    );
  }
}
