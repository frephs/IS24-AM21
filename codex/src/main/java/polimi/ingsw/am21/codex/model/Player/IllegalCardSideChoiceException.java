package polimi.ingsw.am21.codex.model.Player;

import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;

public class IllegalCardSideChoiceException extends InvalidActionException {

  public IllegalCardSideChoiceException() {
    super(InvalidActionCode.ILLEGAL_CARD_SIDE_CHOICE);
  }
}
