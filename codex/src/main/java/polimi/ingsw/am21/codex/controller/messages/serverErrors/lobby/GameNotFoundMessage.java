package polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class GameNotFoundMessage extends ErrorMessage {

  public GameNotFoundMessage() {
    super(MessageType.GAME_NOT_FOUND);
  }

  @Override
  public String toString() {
    return getType().toString();
  }
}
