package polimi.ingsw.am21.codex.connection.server.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.UUID;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.IncompletePlayerBuilderException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

public class RMIConnectionHandlerImpl
  extends UnicastRemoteObject
  implements RMIConnectionHandler {

  GameController controller;

  public RMIConnectionHandlerImpl(GameController controller)
    throws RemoteException {
    super();
    this.controller = controller;
  }

  public void setClient(GameEventListener eventListener)
    throws RemoteException {
    this.controller.addListener(eventListener);
  }

  public void joinLobby(String gameId, UUID connectionID)
    throws RemoteException, GameNotFoundException, LobbyFullException, GameAlreadyStartedException {
    this.controller.joinLobby(gameId, connectionID);
  }

  public void lobbySetNickname(
    String gameId,
    UUID connectionID,
    String username
  )
    throws RemoteException, NicknameAlreadyTakenException, GameNotFoundException, GameAlreadyStartedException {
    this.controller.lobbySetNickname(gameId, connectionID, username);
  }

  @Override
  public void lobbyChooseObjective(String gameId, UUID socketID, Boolean first)
    throws RemoteException, GameNotFoundException, GameAlreadyStartedException {
    this.controller.lobbyChooseObjective(gameId, socketID, first);
  }

  @Override
  public void startGame(String gameId)
    throws RemoteException, GameNotFoundException, GameNotReadyException, GameAlreadyStartedException {
    this.controller.startGame(gameId);
  }

  @Override
  public void joinGame(String gameId, UUID socketID, CardSideType sideType)
    throws RemoteException, GameNotFoundException, IncompletePlayerBuilderException, EmptyDeckException, GameNotReadyException, IllegalCardSideChoiceException, IllegalPlacingPositionException, GameAlreadyStartedException {}

  @Override
  public void leaveLobby(String gameId, UUID connectionID)
    throws RemoteException, GameNotFoundException {
    this.controller.removePlayerFromLobby(gameId, connectionID);
  }

  @Override
  public Set<TokenColor> getAvailableTokens(String gameId)
    throws RemoteException, GameNotFoundException {
    return this.controller.getAvailableTokens(gameId);
  }

  @Override
  public void registerClient(RemoteGameEventListener listener)
    throws RemoteException {
    this.controller.addListener(listener);
  }

  public void lobbySetTokenColor(
    String gameId,
    UUID connectionID,
    TokenColor tokenColor
  )
    throws RemoteException, TokenAlreadyTakenException, GameNotFoundException, GameAlreadyStartedException {
    this.controller.lobbySetTokenColor(gameId, connectionID, tokenColor);
  }

  public void lobbySetObjectiveCard(
    String gameId,
    UUID connectionID,
    Boolean isFirst
  ) throws RemoteException, GameNotFoundException, GameAlreadyStartedException {
    this.controller.lobbyChooseObjective(gameId, connectionID, isFirst);
  }

  public void lobbyJoinGame(
    String gameId,
    UUID connectionID,
    CardSideType starterCardSize
  )
    throws RemoteException, GameNotReadyException, EmptyDeckException, IllegalCardSideChoiceException, IllegalPlacingPositionException, GameNotFoundException, GameAlreadyStartedException {
    this.controller.joinGame(gameId, connectionID, starterCardSize);
  }

  public void createGame(String gameId, UUID socketID, Integer players)
    throws RemoteException, EmptyDeckException {
    this.controller.createGame(gameId, socketID, players);
  }

  @Override
  public void deleteGame(String gameId) throws RemoteException {
    this.controller.deleteGame(gameId);
  }

  public void placeCard(
    String gameId,
    String playerNickname,
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  )
    throws RemoteException, GameNotFoundException, PlayerNotActive, IllegalCardSideChoiceException, IllegalPlacingPositionException {
    this.controller.placeCard(
        gameId,
        playerNickname,
        playerHandCardNumber,
        side,
        position
      );
  }

  public void nextTurn(String gameId, String playerNickname)
    throws RemoteException, GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive, GameOverException {
    this.controller.nextTurn(gameId, playerNickname);
  }

  public void nextTurn(
    String gameId,
    String playerNickname,
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  )
    throws RemoteException, GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive, GameOverException, EmptyDeckException {
    this.controller.nextTurn(gameId, playerNickname, drawingSource, deckType);
  }

  public Set<String> getGames() throws RemoteException {
    return this.controller.getGames();
  }

  @Override
  public void removePlayerFromLobby(String gameId, UUID socketID)
    throws RemoteException, GameNotFoundException {
    this.controller.removePlayerFromLobby(gameId, socketID);
  }
}
