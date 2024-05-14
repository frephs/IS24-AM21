package polimi.ingsw.am21.codex.controller.listeners;

import java.util.List;
import java.util.UUID;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Player.Player;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface GameEventListener {
  void gameCreated(String gameId);

  void gameDeleted(String gameId);

  void gameStarted(String gameId);

  void playerJoinedLobby(String gameId, UUID socketID);

  void playerLeftLobby(String gameId, UUID socketID);

  void playerSetToken(String gameId, UUID socketID, TokenColor token);

  void playerSetNickname(String gameId, UUID socketID, String nickname);

  void playerChoseObjectiveCard(String gameId, UUID socketID, Boolean isFirst);

  void playerJoinedGame(String gameId, UUID socketID, String nickname);

  void gameStarted(String gameId, List<Player> players);

  void changeTurn(String gameId, Integer nextPlayer, Boolean isLastRound);

  /* current player placed a card */
  void cardPlaced(
    String gameId,
    Integer playerHandCardNumber,
    Integer cardId,
    CardSideType side,
    Position position
  );
}
