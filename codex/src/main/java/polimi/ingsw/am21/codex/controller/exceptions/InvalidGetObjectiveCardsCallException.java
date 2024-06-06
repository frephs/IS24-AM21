package polimi.ingsw.am21.codex.controller.exceptions;

public class InvalidGetObjectiveCardsCallException
  extends InvalidActionException {

  public InvalidGetObjectiveCardsCallException() {
    super(InvalidActionCode.INVALID_GET_OBJECTIVE_CARDS_CALL);
  }
}
