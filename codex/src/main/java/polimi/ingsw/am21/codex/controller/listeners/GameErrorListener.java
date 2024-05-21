package polimi.ingsw.am21.codex.controller.listeners;

import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface GameErrorListener {
  void lobbyFull();
  void gameFull(String gameId);
  void tokenTaken(TokenColor token);
  void nicknameTaken(String nickname);

  void actionNotAllowed();
  void unknownResponse();

  void gameAlreadyStarted();
  void gameNotStarted();
  void gameNotFound(String gameId);
  void notInGame();
  void playerNotActive();
  void invalidNextTurnCall();
  void gameOver();
  void emptyDeck();
  void invalidCardPlacement(String reason);
}
