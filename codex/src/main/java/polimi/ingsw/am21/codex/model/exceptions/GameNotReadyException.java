package polimi.ingsw.am21.codex.model.exceptions;

import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;

public class GameNotReadyException extends InvalidActionException {

  public GameNotReadyException() {
    super(InvalidActionCode.GAME_NOT_READY);
  }
}
