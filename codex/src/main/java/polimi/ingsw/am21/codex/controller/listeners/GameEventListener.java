package polimi.ingsw.am21.codex.controller.listeners;

import java.rmi.RemoteException;
import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface GameEventListener extends RemoteGameEventListener {
  @Override
  void gameCreated(String gameId, int currentPlayers, int maxPlayers);

  @Override
  void gameDeleted(String gameId);

  @Override
  void playerJoinedLobby(String gameId, UUID socketID);

  @Override
  void playerLeftLobby(String gameId, UUID socketID);

  @Override
  void playerSetToken(String gameId, UUID socketID, TokenColor token);

  @Override
  void playerSetNickname(String gameId, UUID socketID, String nickname);

  @Override
  void playerChoseObjectiveCard(String gameId, UUID socketID, String nickname);

  @Override
  void playerJoinedGame(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCardID,
    CardSideType starterSide
  );

  @Override
  void gameStarted(
    String gameId,
    List<String> players,
    Pair<Integer, Integer> goldCardPairIds,
    Pair<Integer, Integer> resourceCardPairIds,
    Pair<Integer, Integer> commonObjectivesIds
  );

  /**
   * @param playerId The player that has just finished their turn
   */
  @Override
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
  @Override
  void changeTurn(String gameId, String playerId, Boolean isLastRound);

  /* current player placed a card */
  @Override
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

  @Override
  void gameOver() throws RemoteException;

  @Override
  void playerScoresUpdate(Map<String, Integer> newScores);

  @Override
  void remainingTurns(int remainingTurns);

  @Override
  void winningPlayer(String nickname);

  @Override
  void chatMessageSent(String gameId, ChatMessage chatMessage);
}
