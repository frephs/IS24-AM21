package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import polimi.ingsw.am21.codex.controller.listeners.GameInfo;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class GameStartedMessage extends ViewUpdatingMessage {

  private final GameInfo gameInfo;

  public GameStartedMessage(GameInfo gameInfo) {
    super(MessageType.GAME_STARTED);
    this.gameInfo = gameInfo;
  }

  public GameInfo getGameInfo() {
    return gameInfo;
  }

  public String getGameId() {
    return gameInfo.getGameId();
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "gameId='" +
      gameInfo +
      "', players=" +
      Arrays.toString(gameInfo.getUsers().toArray()) +
      '}'
    );
  }
}
