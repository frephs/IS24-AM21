package polimi.ingsw.am21.codex.controller.exceptions;

public class InvalidTokenColorException extends InvalidActionException {

  public InvalidTokenColorException() {
    super(InvalidActionCode.INVALID_TOKEN_COLOR);
  }
}
