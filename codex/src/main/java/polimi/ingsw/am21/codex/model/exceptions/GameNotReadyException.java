package polimi.ingsw.am21.codex.model.exceptions;

public class GameNotReadyException extends Exception {

  public GameNotReadyException() {
    super("The game cannot start yet");
  }
}
