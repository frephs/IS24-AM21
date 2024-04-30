package polimi.ingsw.am21.codex.controller.exceptions;

public class PlayerNotActive extends Exception {
  public PlayerNotActive() {
    super("Player not currently in turn");
  }
}
