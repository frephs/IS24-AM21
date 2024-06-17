package polimi.ingsw.am21.codex.model.exceptions;

public class AlreadyPlacedCardGameException extends Exception {

  public AlreadyPlacedCardGameException() {
    super("Card already placed in game");
  }
}
