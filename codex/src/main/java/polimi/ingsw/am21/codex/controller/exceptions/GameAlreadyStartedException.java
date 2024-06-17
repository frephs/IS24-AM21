package polimi.ingsw.am21.codex.controller.exceptions;

public class GameAlreadyStartedException extends InvalidActionException {

  public GameAlreadyStartedException() {
    super(InvalidActionCode.GAME_ALREADY_STARTED);
  }
}
