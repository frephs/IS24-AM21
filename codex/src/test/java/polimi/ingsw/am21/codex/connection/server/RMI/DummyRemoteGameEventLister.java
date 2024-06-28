package polimi.ingsw.am21.codex.connection.server.RMI;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.listeners.FullUserGameContext;
import polimi.ingsw.am21.codex.controller.listeners.GameInfo;
import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class DummyRemoteGameEventLister
  implements RemoteGameEventListener, Serializable {

  @Override
  public void gameCreated(String gameId, int currentPlayers, int maxPlayers)
    throws RemoteException {}

  @Override
  public void refreshLobbies(
    Set<String> lobbyIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) throws RemoteException {}

  @Override
  public void gameDeleted(String gameId) throws RemoteException {}

  @Override
  public void playerJoinedLobby(String gameId, UUID connectionID)
    throws RemoteException {}

  @Override
  public void playerLeftLobby(String gameId, UUID connectionID)
    throws RemoteException {}

  @Override
  public void playerSetToken(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor token
  ) throws RemoteException {}

  @Override
  public void playerSetNickname(
    String gameId,
    UUID connectionID,
    String nickname
  ) throws RemoteException {}

  @Override
  public void playerChoseObjectiveCard(
    String gameId,
    UUID connectionID,
    String nickname
  ) throws RemoteException {}

  @Override
  public void playerJoinedGame(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCard,
    CardSideType starterSide
  ) throws RemoteException {}

  @Override
  public void gameStarted(String gameId, GameInfo gameInfo)
    throws RemoteException {}

  @Override
  public void changeTurn(
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
  ) throws RemoteException {}

  @Override
  public void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots,
    Integer resourceDeckTopCardId,
    Integer goldDeckTopCardId
  ) throws RemoteException {}

  @Override
  public void cardPlaced(
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
  ) throws RemoteException {}

  @Override
  public void gameOver() throws RemoteException {}

  @Override
  public void playerScoresUpdate(Map<String, Integer> newScores)
    throws RemoteException {}

  @Override
  public void remainingRounds(String gameID, int remainingRounds)
    throws RemoteException {}

  @Override
  public void winningPlayer(String nickname) throws RemoteException {}

  @Override
  public void playerConnectionChanged(
    UUID connectionID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) throws RemoteException {}

  @Override
  public void lobbyInfo(LobbyUsersInfo usersInfo) throws RemoteException {}

  @Override
  public void chatMessage(String gameID, ChatMessage message)
    throws RemoteException {}

  @Override
  public void getObjectiveCards(Pair<Integer, Integer> objectiveCards)
    throws RemoteException {}

  @Override
  public void getStarterCard(Integer cardId) throws RemoteException {}

  @Override
  public void gameHalted(String gameID) throws RemoteException {}

  @Override
  public void gameResumed(String gameID) throws RemoteException {}

  @Override
  public void userContext(FullUserGameContext context) throws RemoteException {}
}
