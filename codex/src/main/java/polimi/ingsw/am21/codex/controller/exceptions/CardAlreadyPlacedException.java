package polimi.ingsw.am21.codex.controller.exceptions;

public class CardAlreadyPlacedException extends Exception {

  public CardAlreadyPlacedException() {
    super("You already placed a card this turn");
  }
}
