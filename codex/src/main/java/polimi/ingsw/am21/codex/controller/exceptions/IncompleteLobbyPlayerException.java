package polimi.ingsw.am21.codex.controller.exceptions;

import java.util.List;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.IncompletePlayerBuilderException;

public class IncompleteLobbyPlayerException extends InvalidActionException {

  public IncompleteLobbyPlayerException(String message) {
    super(InvalidActionCode.INCOMPLETE_LOBBY_PLAYER, List.of(message));
  }

  public IncompleteLobbyPlayerException(IncompletePlayerBuilderException e) {
    this(e.getMessage());
  }

  public String getMessage() {
    return this.notes.get(0);
  }

  public static IncompleteLobbyPlayerException fromExceptionNotes(
    List<String> notes
  ) {
    return new IncompleteLobbyPlayerException(notes.get(0));
  }
}
