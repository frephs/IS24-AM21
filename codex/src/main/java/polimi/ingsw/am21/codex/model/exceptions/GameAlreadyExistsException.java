package polimi.ingsw.am21.codex.model.exceptions;

import java.util.List;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;

public class GameAlreadyExistsException extends InvalidActionException {

  public GameAlreadyExistsException(String gameID) {
    super(InvalidActionCode.GAME_ALREADY_EXISTS, List.of(gameID));
  }

  public String getGameID() {
    return getNotes().get(0);
  }

  public static GameAlreadyExistsException fromExceptionNotes(
    List<String> notes
  ) {
    return new GameAlreadyExistsException(notes.get(0));
  }
}
