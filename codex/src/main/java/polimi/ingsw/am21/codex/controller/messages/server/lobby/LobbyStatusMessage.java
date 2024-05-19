package polimi.ingsw.am21.codex.controller.messages.server.lobby;

import java.util.Map;
import java.util.UUID;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ResponseMessage;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class LobbyStatusMessage extends ResponseMessage {

  private final Map<UUID, Pair<String, TokenColor>> players;

  public LobbyStatusMessage(Map<UUID, Pair<String, TokenColor>> players) {
    super(MessageType.LOBBY_STATUS);
    this.players = players == null ? Map.of() : Map.copyOf(players);
  }

  public Map<UUID, Pair<String, TokenColor>> getPlayers() {
    return players;
  }

  @Override
  public String toString() {
    return getType() + "{" + "players=" + players + '}';
  }
}
