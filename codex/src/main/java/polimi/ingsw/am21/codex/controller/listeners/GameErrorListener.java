package polimi.ingsw.am21.codex.controller.listeners;

import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface GameErrorListener {
  void unknownResponse();

  void gameNotFound(String gameId);

  void notInLobby();
  void lobbyFull(String gameId);
  void tokenTaken(TokenColor token);
  void nicknameTaken(String nickname);

  void gameNotStarted();
  void notInGame();
  void gameAlreadyStarted();
  void playerNotActive();
  void invalidCardPlacement(String reason);
  void invalidNextTurnCall();
  void invalidGetObjectiveCardsCall();
  void gameNotReady();
  void gameOver();
  void emptyDeck();
  void playerNotFound();
  void incompleteLobbyPlayer(String msg);
  void illegalCardSideChoice();
}
