package polimi.ingsw.am21.codex.model;

public class LobbyFullException extends Exception {
  public LobbyFullException() {
    super("Lobby is full");
  }
}
