package polimi.ingsw.am21.codex.controller.exceptions;

public class GameAlreadyStartedException extends Exception {

  public GameAlreadyStartedException() {
    super("The game has already started.");
  }
}
