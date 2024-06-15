package polimi.ingsw.am21.codex.controller.exceptions;

import java.util.List;
import java.util.UUID;
import polimi.ingsw.am21.codex.model.exceptions.PlayerNotFoundGameException;

public class PlayerNotFoundException extends InvalidActionException {

  public PlayerNotFoundException(String nickname) {
    super(InvalidActionCode.PLAYER_NOT_FOUND, List.of(nickname));
  }

  public PlayerNotFoundException(UUID playerID) {
    super(InvalidActionCode.PLAYER_NOT_FOUND, List.of(playerID.toString()));
  }

  public PlayerNotFoundException(PlayerNotFoundGameException e) {
    super(InvalidActionCode.PLAYER_NOT_FOUND, List.of(e.getIdentifier()));
  }

  public String getIdentifier() {
    return this.notes.get(0);
  }

  public static PlayerNotFoundException fromExceptionNotes(List<String> notes) {
    return new PlayerNotFoundException(notes.get(0));
  }
}
