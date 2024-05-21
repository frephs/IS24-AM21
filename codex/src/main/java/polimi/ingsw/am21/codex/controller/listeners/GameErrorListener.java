package polimi.ingsw.am21.codex.controller.listeners;

import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface GameErrorListener {
  void gameFull(String gameId);
  void gameNotFound(String gameId);

  void tokenTaken(TokenColor token);
  void nicknameTaken(String nickname);

  void actionNotAllowed();
  void unknownResponse();
  void invalidCardPlacement(String reason);
}
