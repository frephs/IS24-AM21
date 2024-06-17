package polimi.ingsw.am21.codex.model.Cards.Commons;

import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;

public class EmptyDeckException extends InvalidActionException {

  public EmptyDeckException() {
    super(InvalidActionCode.EMPTY_DECK);
  }
}
