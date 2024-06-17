package polimi.ingsw.am21.codex.model.GameBoard.exceptions;

import java.util.List;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class TokenAlreadyTakenException extends InvalidActionException {

  public TokenAlreadyTakenException(TokenColor color) {
    super(InvalidActionCode.TOKEN_ALREADY_TAKEN, List.of(color.name()));
  }

  public String getTokenColor() {
    return this.getNotes().get(0);
  }

  public static TokenAlreadyTakenException fromExceptionNotes(
    List<String> notes
  ) {
    return new TokenAlreadyTakenException(TokenColor.valueOf(notes.get(0)));
  }
}
