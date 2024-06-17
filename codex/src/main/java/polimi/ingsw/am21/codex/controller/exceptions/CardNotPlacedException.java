package polimi.ingsw.am21.codex.controller.exceptions;

public class CardNotPlacedException extends InvalidActionException {

  public CardNotPlacedException() {
    super(InvalidActionCode.CARD_NOT_PLACED);
  }
}
