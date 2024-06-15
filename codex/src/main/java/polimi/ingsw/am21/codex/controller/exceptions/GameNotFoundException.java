package polimi.ingsw.am21.codex.controller.exceptions;

import java.util.List;

public class GameNotFoundException extends InvalidActionException {

  public GameNotFoundException(String gameID) {
    super(InvalidActionCode.GAME_NOT_FOUND, List.of(gameID));
  }

  public String getGameID() {
    return this.getNotes().get(0);
  }

  public static GameNotFoundException fromExceptionNotes(List<String> notes) {
    return new GameNotFoundException(notes.get(0));
  }
}
