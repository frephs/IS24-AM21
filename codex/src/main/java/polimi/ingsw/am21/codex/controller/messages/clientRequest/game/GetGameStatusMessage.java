package polimi.ingsw.am21.codex.controller.messages.clientRequest.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.RequestMessage;

public class GetGameStatusMessage extends RequestMessage {

  private final String gameId;

  public GetGameStatusMessage(String gameId) {
    super(MessageType.GET_GAME_STATUS);
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
