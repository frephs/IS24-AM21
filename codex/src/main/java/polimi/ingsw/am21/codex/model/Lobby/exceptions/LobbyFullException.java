package polimi.ingsw.am21.codex.model.Lobby.exceptions;

public class LobbyFullException extends Exception {
  public LobbyFullException() {
    super("Lobby is full");
  }
}
