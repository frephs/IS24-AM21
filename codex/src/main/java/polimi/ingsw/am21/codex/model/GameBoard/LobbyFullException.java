package polimi.ingsw.am21.codex.model.GameBoard;

public class LobbyFullException extends Exception {
  public LobbyFullException() {
    super("Lobby is full");
  }
}
