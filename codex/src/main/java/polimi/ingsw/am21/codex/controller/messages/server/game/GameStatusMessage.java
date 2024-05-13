package polimi.ingsw.am21.codex.controller.messages.server.game;

import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.ResponseMessage;
import polimi.ingsw.am21.codex.model.GameState;

public class GameStatusMessage extends ResponseMessage {

  private final GameState state;

  public GameStatusMessage(GameState state) {
    super(MessageType.GAME_STATUS);
    this.state = state;
  }

  public GameState getState() {
    return state;
  }
}
