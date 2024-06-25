package polimi.ingsw.am21.codex.connection.server.RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class RMIServerConnectionHandlerImpl
  extends UnicastRemoteObject
  implements RMIServerConnectionHandler {

  /**
   * The controller of the game
   */
  GameController controller;

  public RMIServerConnectionHandlerImpl(GameController controller)
    throws RemoteException {
    super();
    this.controller = controller;
  }

  @Override
  public Set<String> getGames() throws RemoteException {
    return this.controller.getGames();
  }

  @Override
  public Map<String, Integer> getGamesCurrentPlayers() throws RemoteException {
    return this.controller.getGamesCurrentPlayers();
  }

  @Override
  public Map<String, Integer> getGamesMaxPlayers() throws RemoteException {
    return this.controller.getGamesMaxPlayers();
  }

  @Override
  public Pair<Integer, Integer> getLobbyObjectiveCards(UUID connectionID)
    throws RemoteException, InvalidActionException {
    return this.controller.getLobbyObjectiveCards(connectionID);
  }

  @Override
  public Integer getLobbyStarterCard(UUID connectionID)
    throws RemoteException, InvalidActionException {
    return this.controller.getLobbyStarterCard(connectionID);
  }

  @Override
  public void joinLobby(UUID connectionID, String gameID)
    throws RemoteException, InvalidActionException {
    this.controller.joinLobby(connectionID, gameID);
  }

  @Override
  public void lobbySetTokenColor(UUID connectionID, TokenColor color)
    throws RemoteException, InvalidActionException {
    this.controller.lobbySetTokenColor(connectionID, color);
  }

  @Override
  public void lobbySetNickname(UUID connectionID, String nickname)
    throws RemoteException, InvalidActionException {
    this.controller.lobbySetNickname(connectionID, nickname);
  }

  @Override
  public void lobbyChooseObjective(UUID connectionID, Boolean first)
    throws RemoteException, InvalidActionException {
    this.controller.lobbyChooseObjective(connectionID, first);
  }

  @Override
  public void joinGame(UUID connectionID, String gameID, CardSideType sideType)
    throws RemoteException, InvalidActionException {
    this.controller.joinGame(connectionID, gameID, sideType);
  }

  @Override
  public void createGame(UUID connectionID, String gameId, Integer players)
    throws RemoteException, InvalidActionException {
    this.controller.createGame(connectionID, gameId, players);
  }

  @Override
  public void deleteGame(UUID connectionID, String gameId)
    throws RemoteException, InvalidActionException {
    this.controller.deleteGame(connectionID, gameId);
  }

  @Override
  public void nextTurn(UUID connectionID)
    throws RemoteException, InvalidActionException {
    this.controller.nextTurn(connectionID);
  }

  @Override
  public void nextTurn(
    UUID connectionID,
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  ) throws RemoteException, InvalidActionException {
    this.controller.nextTurn(connectionID, drawingSource, deckType);
  }

  @Override
  public void placeCard(
    UUID connectionID,
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  ) throws RemoteException, InvalidActionException {
    this.controller.placeCard(
        connectionID,
        playerHandCardNumber,
        side,
        position
      );
  }

  @Override
  public void leaveLobby(UUID connectionID)
    throws RemoteException, InvalidActionException {
    this.controller.quitFromLobby(connectionID);
  }

  @Override
  public void sendChatMessage(UUID connectionID, ChatMessage message)
    throws RemoteException, InvalidActionException {
    this.controller.sendChatMessage(connectionID, message);
  }

  @Override
  public void connect(UUID connectionID, RemoteGameEventListener listener)
    throws RemoteException {
    this.controller.connect(connectionID, listener);
  }

  @Override
  public void heartBeat(UUID connectionID) throws RemoteException {
    this.controller.heartBeat(connectionID);
  }
}
