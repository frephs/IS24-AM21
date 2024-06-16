package polimi.ingsw.am21.codex.model.exceptions;

public class InvalidGameNameException extends Exception {

  public InvalidGameNameException(String gameName) {
    super(
      "Game name " +
      gameName +
      " is invalid. Game names can only contain letters and numbers."
    );
  }
}
