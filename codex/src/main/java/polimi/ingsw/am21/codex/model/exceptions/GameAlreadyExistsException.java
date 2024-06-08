package polimi.ingsw.am21.codex.model.exceptions;

public class GameAlreadyExistsException extends Exception {

  public GameAlreadyExistsException(String gameId) {
    super("Game " + gameId + " already exists.");
  }
}
