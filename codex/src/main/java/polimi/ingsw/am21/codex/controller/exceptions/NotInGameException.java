package polimi.ingsw.am21.codex.controller.exceptions;

public class NotInGameException extends InvalidActionException {

  public NotInGameException() {
    super(InvalidActionCode.NOT_IN_GAME);
  }
}
