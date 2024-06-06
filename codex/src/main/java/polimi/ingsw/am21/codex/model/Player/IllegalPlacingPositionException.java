package polimi.ingsw.am21.codex.model.Player;

import java.util.List;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;

public class IllegalPlacingPositionException extends InvalidActionException {

  public IllegalPlacingPositionException(String reason) {
    super(InvalidActionCode.ILLEGAL_PLACING_POSITION, List.of(reason));
  }

  public String getReason() {
    return this.getNotes().get(0);
  }
}
