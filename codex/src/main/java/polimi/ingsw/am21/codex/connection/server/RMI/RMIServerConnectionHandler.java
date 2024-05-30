package polimi.ingsw.am21.codex.connection.server.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.PlayerNotFoundException;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.IncompletePlayerBuilderException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

public interface RMIServerConnectionHandler extends Remote {
  Set<String> getGames() throws RemoteException;

  Map<String, Integer> getGamesCurrentPlayers() throws RemoteException;

  Map<String, Integer> getGamesMaxPlayers() throws RemoteException;

  Pair<Integer, Integer> getLobbyObjectiveCards(String gameId, UUID socketID)
    throws RemoteException, GameNotFoundException;

  Integer getLobbyStarterCard(String gameId, UUID socketID)
    throws RemoteException, GameNotFoundException, PlayerNotFoundException;

  void removePlayerFromLobby(String gameId, UUID socketID)
    throws RemoteException, GameNotFoundException;

  void joinLobby(String gameId, UUID socketID)
    throws RemoteException, GameNotFoundException, LobbyFullException, GameAlreadyStartedException;

  void lobbySetTokenColor(String gameId, UUID socketID, TokenColor color)
    throws RemoteException, GameNotFoundException, TokenAlreadyTakenException, GameAlreadyStartedException;

  void lobbySetNickname(String gameId, UUID socketID, String nickname)
    throws RemoteException, GameNotFoundException, NicknameAlreadyTakenException, GameAlreadyStartedException;

  void lobbyChooseObjective(String gameId, UUID socketID, Boolean first)
    throws RemoteException, GameNotFoundException, GameAlreadyStartedException;

  void startGame(String gameId)
    throws RemoteException, GameNotFoundException, GameNotReadyException, GameAlreadyStartedException;

  void joinGame(String gameId, UUID socketID, CardSideType sideType)
    throws RemoteException, GameNotFoundException, IncompletePlayerBuilderException, EmptyDeckException, GameNotReadyException, IllegalCardSideChoiceException, IllegalPlacingPositionException, GameAlreadyStartedException;

  void createGame(String gameId, UUID socketID, Integer players)
    throws RemoteException, EmptyDeckException;

  void deleteGame(String gameId) throws RemoteException;

  void nextTurn(String gameId, String playerNickname)
    throws RemoteException, GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive, GameOverException;

  void nextTurn(
    String gameId,
    String playerNickname,
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  )
    throws RemoteException, GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive, GameOverException, EmptyDeckException;

  void placeCard(
    String gameId,
    String playerNickname,
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  )
    throws RemoteException, GameNotFoundException, PlayerNotActive, IllegalCardSideChoiceException, IllegalPlacingPositionException;

  void leaveLobby(String gameId, UUID connectionID)
    throws RemoteException, GameNotFoundException;

  Set<TokenColor> getAvailableTokens(String gameId)
    throws RemoteException, GameNotFoundException;

  public void registerListener(
    UUID connectionID,
    RemoteGameEventListener listener
  ) throws RemoteException;
}
