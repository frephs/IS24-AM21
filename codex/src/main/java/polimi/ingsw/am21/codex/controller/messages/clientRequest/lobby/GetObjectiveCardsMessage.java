package polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby;

import java.util.UUID;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.RequestMessage;

public class GetObjectiveCardsMessage extends RequestMessage {

  private final String gameId;

  public GetObjectiveCardsMessage(UUID connectionID, String gameId) {
    super(MessageType.GET_OBJECTIVE_CARDS, connectionID);
    this.gameId = gameId;
  }

  public String getGameId() {
    return gameId;
  }

  @Override
  public String toString() {
    return getType() + "{" + "gameId='" + gameId + '\'' + '}';
  }
}
