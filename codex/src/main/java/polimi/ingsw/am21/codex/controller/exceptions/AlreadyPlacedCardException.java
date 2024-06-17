package polimi.ingsw.am21.codex.controller.exceptions;

public class AlreadyPlacedCardException extends InvalidActionException {

  public AlreadyPlacedCardException() {
    super(InvalidActionCode.ALREADY_PLACED_CARD);
  }
}
