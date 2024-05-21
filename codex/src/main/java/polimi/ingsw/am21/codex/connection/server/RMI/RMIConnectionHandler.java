package polimi.ingsw.am21.codex.connection.server.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Game;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.GameManager;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Lobby.Lobby;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.IncompletePlayerBuilderException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.Player;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

public interface RMIConnectionHandler extends Remote {
  public Set<String> getGames() throws RemoteException;

  public void removePlayerFromLobby(String gameId, UUID socketID)
    throws RemoteException, GameNotFoundException;

  public void joinLobby(String gameId, UUID socketID)
    throws RemoteException, GameNotFoundException, LobbyFullException, GameAlreadyStartedException;

  public void lobbySetTokenColor(
    String gameId,
    UUID socketID,
    TokenColor color
  )
    throws RemoteException, GameNotFoundException, TokenAlreadyTakenException, GameAlreadyStartedException;

  public void lobbySetNickname(String gameId, UUID socketID, String nickname)
    throws RemoteException, GameNotFoundException, NicknameAlreadyTakenException, GameAlreadyStartedException;

  public void lobbyChooseObjective(String gameId, UUID socketID, Boolean first)
    throws RemoteException, GameNotFoundException, GameAlreadyStartedException;

  public void startGame(String gameId)
    throws RemoteException, GameNotFoundException, GameNotReadyException, GameAlreadyStartedException;

  public void joinGame(String gameId, UUID socketID, CardSideType sideType)
    throws RemoteException, GameNotFoundException, IncompletePlayerBuilderException, EmptyDeckException, GameNotReadyException, IllegalCardSideChoiceException, IllegalPlacingPositionException, GameAlreadyStartedException;

  public void createGame(String gameId, UUID socketID, Integer players)
    throws RemoteException, EmptyDeckException;

  public void deleteGame(String gameId) throws RemoteException;

  public void nextTurn(String gameId, String playerNickname)
    throws RemoteException, GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive, GameOverException;

  public void nextTurn(
    String gameId,
    String playerNickname,
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  )
    throws RemoteException, GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive, GameOverException, EmptyDeckException;

  public void placeCard(
    String gameId,
    String playerNickname,
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  )
    throws RemoteException, GameNotFoundException, PlayerNotActive, IllegalCardSideChoiceException, IllegalPlacingPositionException;

  public void leaveLobby(String gameId, UUID connectionID)
    throws RemoteException, GameNotFoundException;

  public Set<TokenColor> getAvailableTokens(String gameId)
    throws RemoteException, GameNotFoundException;
}
