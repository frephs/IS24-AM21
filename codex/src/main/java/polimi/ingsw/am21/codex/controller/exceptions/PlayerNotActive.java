package polimi.ingsw.am21.codex.controller.exceptions;

public class PlayerNotActive extends InvalidActionException {

  public PlayerNotActive() {
    super(InvalidActionCode.PLAYER_NOT_ACTIVE);
  }
}
