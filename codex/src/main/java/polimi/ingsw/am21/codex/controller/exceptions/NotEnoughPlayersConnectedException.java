package polimi.ingsw.am21.codex.controller.exceptions;

public class NotEnoughPlayersConnectedException extends InvalidActionException {

  public NotEnoughPlayersConnectedException() {
    super(InvalidActionCode.NOT_ENOUGH_PLAYERS_CONNECTED);
  }
}
