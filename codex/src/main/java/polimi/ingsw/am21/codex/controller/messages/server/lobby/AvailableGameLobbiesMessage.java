package polimi.ingsw.am21.codex.controller.messages.server.lobby;

import java.util.HashSet;
import java.util.Set;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ResponseMessage;

public class AvailableGameLobbiesMessage extends ResponseMessage {

  private final Set<String> lobbyIds;

  public AvailableGameLobbiesMessage(Set<String> lobbyIds) {
    super(MessageType.AVAILABLE_GAME_LOBBIES);
    this.lobbyIds = new HashSet<>(lobbyIds);
  }

  public Set<String> getLobbyIds() {
    return lobbyIds;
  }

  @Override
  public String toString() {
    return getType() + "{" + "lobbyIds=" + lobbyIds + '}';
  }
}
