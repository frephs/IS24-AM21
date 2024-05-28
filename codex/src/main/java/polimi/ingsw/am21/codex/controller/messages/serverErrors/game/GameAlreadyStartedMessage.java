package polimi.ingsw.am21.codex.controller.messages.serverErrors.game;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class GameAlreadyStartedMessage extends ErrorMessage {

  public GameAlreadyStartedMessage() {
    super(MessageType.GAME_ALREADY_STARTED);
  }

  @Override
  public String toString() {
    return getType().toString();
  }
}
