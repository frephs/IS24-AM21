package polimi.ingsw.am21.codex.controller.exceptions;

public class GameNotFoundException extends Exception {
  public GameNotFoundException() {
    super("Game not found");
  }
}
