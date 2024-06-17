package polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.RequestMessage;

public class GetAvailableGameLobbiesMessage extends RequestMessage {

  public GetAvailableGameLobbiesMessage(UUID connectionID) {
    super(MessageType.GET_AVAILABLE_GAME_LOBBIES, connectionID);
  }

  @Override
  public String toString() {
    return getType().toString();
  }
}
