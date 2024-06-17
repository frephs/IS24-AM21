package polimi.ingsw.am21.codex.model.Lobby.exceptions;

import java.util.List;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;

public class LobbyFullException extends InvalidActionException {

  public static class LobbyFullInternalException extends Exception {

    public LobbyFullInternalException() {
      super("Lobby is full");
    }
  }

  public LobbyFullException(String gameID) {
    super(InvalidActionCode.LOBBY_FULL, List.of(gameID));
  }

  public String getGameID() {
    return this.getNotes().get(0);
  }

  public static LobbyFullException fromExceptionNotes(List<String> notes) {
    return new LobbyFullException(notes.get(0));
  }
}
