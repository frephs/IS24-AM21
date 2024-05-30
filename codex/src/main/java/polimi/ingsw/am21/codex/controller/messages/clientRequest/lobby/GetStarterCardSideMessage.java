package polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.RequestMessage;

public class GetStarterCardSideMessage extends RequestMessage {

  private final String gameId;

  public GetStarterCardSideMessage(String gameId) {
    super(MessageType.GET_STARTER_CARD_SIDE);
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
