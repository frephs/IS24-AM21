package polimi.ingsw.am21.codex.model.Player;

public class IllegalPlacingPositionException extends Exception {

  public IllegalPlacingPositionException() {
    super(
      "Illegal placing position: you tried placing a card in a position which is either forbidden, occupied or not reachable"
    );
  }

  public IllegalPlacingPositionException(String message) {
    super(message);
  }
}
