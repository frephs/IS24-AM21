package polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby;

import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;

public class GameFullMessage extends ErrorMessage {

  public GameFullMessage() {
    super(MessageType.GAME_FULL);
  }
}
