package polimi.ingsw.am21.codex.model.exceptions;

import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;

public class GameOverException extends InvalidActionException {

  public GameOverException() {
    super(InvalidActionCode.GAME_OVER);
  }
}
