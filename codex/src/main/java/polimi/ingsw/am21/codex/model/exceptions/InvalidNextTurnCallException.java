package polimi.ingsw.am21.codex.model.exceptions;

import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;

public class InvalidNextTurnCallException extends InvalidActionException {

  public InvalidNextTurnCallException() {
    super(InvalidActionCode.INVALID_NEXT_TURN_CALL);
  }
}
