package polimi.ingsw.am21.codex.controller.listeners;

import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.Player;

import java.util.List;
import java.util.UUID;

public interface ControllerEventListener {
  void gameCreated(String gameId);

  void gameDeleted(String gameId);

  void gameStarted(String gameId);

  void playerJoinedLobby(String gameId, UUID socketID);

  void playerLeftLobby(String gameId, UUID socketID);

  void gameStarted(List<Player> players);

  void changeTurn(Integer nextPlayer, Boolean isLastRound);

  /* current player placed a card */
  void cardPlaced(int cardId);

  void nextTurn(DrawingCardSource drawingCardSource,
                DrawingDeckType drawingDeckType);
  void nextTurn();

}
