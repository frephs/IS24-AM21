package polimi.ingsw.am21.codex.model.Lobby;

public class LobbyFullException extends Exception {
  public LobbyFullException() {
    super("Lobby is full");
  }
}
