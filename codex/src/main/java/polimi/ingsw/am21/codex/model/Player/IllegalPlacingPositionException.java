package polimi.ingsw.am21.codex.model.Player;

public class IllegalPlacingPositionException extends IllegalArgumentException {
  public IllegalPlacingPositionException() {
    super("Illegal placing position: you tried placing a card which is either forbidden or not reachable");
  }
}
