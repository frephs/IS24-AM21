package polimi.ingsw.am21.codex.controller.listeners;

import java.util.*;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface GameEventListener extends RemoteGameEventListener {
  void gameCreated(String gameId, int currentPlayers, int maxPlayers);

  void gameDeleted(String gameId);

  void playerJoinedLobby(String gameId, UUID socketID);

  void playerLeftLobby(String gameId, UUID socketID);

  void playerSetToken(String gameId, UUID socketID, TokenColor token);

  void playerSetNickname(String gameId, UUID socketID, String nickname);

  void playerChoseObjectiveCard(Boolean isFirst);

  void playerJoinedGame(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCardID,
    CardSideType starterSide
  );

  void gameStarted(String gameId, List<String> players);

  /**
   * @param playerId The player that has just finished their turn
   */
  void changeTurn(
    String gameId,
    String playerId,
    Boolean isLastRound,
    DrawingCardSource source,
    DrawingDeckType deck,
    Integer cardId,
    Integer newPairCardId
  );

  /**
   * @param playerId The player that has just finished their turn
   */
  void changeTurn(String gameId, String playerId, Boolean isLastRound);

  /* current player placed a card */
  void cardPlaced(
    String gameId,
    String playerId,
    Integer playerHandCardNumber,
    Integer cardId,
    CardSideType side,
    Position position,
    int newPlayerScore,
    Map<ResourceType, Integer> updatedResources,
    Map<ObjectType, Integer> updatedObjects,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots
  );

  void gameOver();

  void playerScoresUpdate(Map<String, Integer> newScores);

  void remainingTurns(int remainingTurns);

  void winningPlayer(String nickname);
}
