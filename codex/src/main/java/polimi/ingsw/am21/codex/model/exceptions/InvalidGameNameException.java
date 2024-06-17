package polimi.ingsw.am21.codex.model.exceptions;

import java.util.List;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;

public class InvalidGameNameException extends InvalidActionException {

  public InvalidGameNameException(String gameName) {
    super(InvalidActionCode.INVALID_GAME_NAME, List.of(gameName));
  }

  public String getGameName() {
    return getNotes().get(0);
  }

  public static InvalidGameNameException fromExceptionNotes(
    List<String> notes
  ) {
    return new InvalidGameNameException(notes.get(0));
  }
}
