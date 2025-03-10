package polimi.ingsw.am21.codex.controller.listeners;

import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface GameEventListener extends RemoteGameEventListener {
  @Override
  void gameCreated(String gameId, int currentPlayers, int maxPlayers);

  @Override
  void refreshLobbies(
    Set<String> lobbyIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  );

  @Override
  void gameDeleted(String gameId);

  @Override
  void playerJoinedLobby(String gameId, UUID connectionID);

  @Override
  void playerLeftLobby(String gameId, UUID connectionID);

  @Override
  void playerSetToken(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor token
  );

  @Override
  void playerSetNickname(String gameId, UUID connectionID, String nickname);

  @Override
  void playerChoseObjectiveCard(
    String gameId,
    UUID connectionID,
    String nickname
  );

  @Override
  void playerJoinedGame(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCardID,
    CardSideType starterSide
  );

  @Override
  void gameStarted(String gameId, GameInfo gameInfo);

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
    Set<Position> forbiddenSpots,
    Integer resourceDeckTopCardId,
    Integer goldDeckTopCardId
  );

  /**
   * @param playerNickname The player that has just finished their turn
   */
  @Override
  void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots,
    Integer resourceDeckTopCardId,
    Integer goldDeckTopCardId
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
    UUID connectionID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  );

  @Override
  void lobbyInfo(LobbyUsersInfo usersInfo);

  @Override
  void chatMessage(String gameID, ChatMessage message);

  @Override
  void getObjectiveCards(Pair<Integer, Integer> objectiveCards);

  @Override
  void getStarterCard(Integer cardId);

  @Override
  void gameHalted(String gameID);

  @Override
  void gameResumed(String gameID);

  @Override
  void userContext(FullUserGameContext context);
}
