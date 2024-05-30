package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class GameCreatedMessage extends ViewUpdatingMessage {

  private final String gameId;
  private final int players;
  private final int maxPlayers;

  public GameCreatedMessage(String gameId, int players, int maxPlayers) {
    super(MessageType.GAME_CREATED);
    this.gameId = gameId;
    this.players = players;
    this.maxPlayers = maxPlayers;
  }

  public String getGameId() {
    return gameId;
  }

  public int getPlayers() {
    return players;
  }

  public int getMaxPlayers() {
    return maxPlayers;
  }

  @Override
  public String toString() {
    return (
      getType() + "{" + "gameId='" + gameId + "', players=" + players + '}'
    );
  }
}
