package polimi.ingsw.am21.codex.controller.messages.server.lobby;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ResponseMessage;

public class AvailableGameLobbiesMessage extends ResponseMessage {

  private final Set<String> lobbyIds;
  private final Map<String, Integer> currentPlayers;
  private final Map<String, Integer> maxPlayers;

  public AvailableGameLobbiesMessage(
    Set<String> lobbyIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) {
    super(MessageType.AVAILABLE_GAME_LOBBIES);
    this.lobbyIds = new HashSet<>(lobbyIds);
    this.currentPlayers = new HashMap<>(currentPlayers);
    this.maxPlayers = new HashMap<>(maxPlayers);
  }

  public Set<String> getLobbyIds() {
    return lobbyIds;
  }

  public Map<String, Integer> getCurrentPlayers() {
    return currentPlayers;
  }

  public Map<String, Integer> getMaxPlayers() {
    return maxPlayers;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "lobbyIds=" +
      lobbyIds +
      ", currentPlayers= " +
      currentPlayers +
      ", maxPlayers=" +
      maxPlayers +
      '}'
    );
  }
}
