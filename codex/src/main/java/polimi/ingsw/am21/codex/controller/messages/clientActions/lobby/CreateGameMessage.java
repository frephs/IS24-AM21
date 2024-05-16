package polimi.ingsw.am21.codex.controller.messages.clientActions.lobby;

import polimi.ingsw.am21.codex.controller.messages.ActionMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class CreateGameMessage extends ActionMessage {

  private final String gameId;
  private final int players;

  public CreateGameMessage(String gameId, int players) {
    super(MessageType.CREATE_GAME);
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
      getType() +
      "{" +
      "gameId='" +
      gameId +
      '\'' +
      ", players=" +
      players +
      '}'
    );
  }
}
