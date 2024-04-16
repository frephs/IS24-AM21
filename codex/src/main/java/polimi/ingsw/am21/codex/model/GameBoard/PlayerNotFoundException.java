package polimi.ingsw.am21.codex.model.GameBoard;

import java.util.UUID;

public class PlayerNotFoundException extends RuntimeException {
  public PlayerNotFoundException(UUID playerID) {
    super("Player with ID " + playerID + " not found");
  }

  public PlayerNotFoundException(String username) {
    super("Player with username " + username + " not found");
  }

}
