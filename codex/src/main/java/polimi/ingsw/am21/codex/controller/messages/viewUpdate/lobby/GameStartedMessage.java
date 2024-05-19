package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import java.util.ArrayList;
import java.util.List;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class GameStartedMessage extends ViewUpdatingMessage {

  private final String gameId;
  private final List<String> playerIds;

  public GameStartedMessage(String gameId, List<String> playerIds) {
    super(MessageType.GAME_STARTED);
    this.gameId = gameId;
    this.playerIds = playerIds != null ? new ArrayList<>(playerIds) : List.of();
  }

  public String getGameId() {
    return gameId;
  }

  public List<String> getPlayerIds() {
    return playerIds;
  }

  @Override
  public String toString() {
    return (
      getType() + "{" + "gameId='" + gameId + "', players=" + playerIds + '}'
    );
  }
}
