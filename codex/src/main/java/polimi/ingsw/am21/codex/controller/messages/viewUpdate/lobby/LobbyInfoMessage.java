package polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby;

import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ViewUpdatingMessage;

public class LobbyInfoMessage extends ViewUpdatingMessage {

  private final LobbyUsersInfo lobbyUsersInfo;

  public LobbyInfoMessage(LobbyUsersInfo lobbyUsersInfo) {
    super(MessageType.LOBBY_INFO);
    this.lobbyUsersInfo = lobbyUsersInfo;
  }

  public LobbyUsersInfo getLobbyUsersInfo() {
    return lobbyUsersInfo;
  }

  @Override
  public String toString() {
    return (
      getType() +
      "{" +
      "gameId='" +
      lobbyUsersInfo.getGameID() +
      "', users=" +
      lobbyUsersInfo.getUsers() +
      '}'
    );
  }
}
