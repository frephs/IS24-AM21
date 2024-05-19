package polimi.ingsw.am21.codex.controller.listeners;

import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface GameErrorListener {
  void gameFull(String gameId);
  void nicknameTaken(String nickname);
  void tokenTaken(TokenColor token);
  void unknownResponse();
}
