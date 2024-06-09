package polimi.ingsw.am21.codex.controller.listeners;

import java.rmi.RemoteException;
import java.util.*;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
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
  void playerSetToken(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor token
  );

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
  void gameStarted(String gameId, GameInfo gameInfo);

  /**
   * @param playerId The player that has just finished their turn
   */
  @Override
  void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    DrawingCardSource source,
    DrawingDeckType deck,
    Integer cardId,
    Integer newPairCardId,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots
  );

  /**
   * @param playerId The player that has just finished their turn
   */
  @Override
  void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots
  );

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
  void gameOver();

  @Override
  void playerScoresUpdate(Map<String, Integer> newScores);

  @Override
  void remainingRounds(String gameID, int remainingRounds);

  @Override
  void winningPlayer(String nickname);

  @Override
  void playerConnectionChanged(
    UUID socketID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  );

  @Override
  void lobbyInfo(LobbyUsersInfo usersInfo);
}
