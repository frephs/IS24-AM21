package polimi.ingsw.am21.codex.controller.messages.server.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ResponseMessage;

public class GameStatusMessage extends ResponseMessage {

  public GameStatusMessage() {
    super(MessageType.GAME_STATUS);
  }
}
