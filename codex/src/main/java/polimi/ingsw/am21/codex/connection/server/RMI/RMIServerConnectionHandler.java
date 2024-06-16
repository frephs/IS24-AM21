package polimi.ingsw.am21.codex.connection.server.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface RMIServerConnectionHandler extends Remote {
  Set<String> getGames() throws RemoteException;

  Map<String, Integer> getGamesCurrentPlayers() throws RemoteException;

  Map<String, Integer> getGamesMaxPlayers() throws RemoteException;

  Pair<Integer, Integer> getLobbyObjectiveCards(UUID connectionID)
    throws RemoteException, InvalidActionException;

  Integer getLobbyStarterCard(UUID connectionID)
    throws RemoteException, InvalidActionException;

  void joinLobby(UUID connectionID, String gameID)
    throws RemoteException, InvalidActionException;

  void lobbySetTokenColor(UUID connectionID, TokenColor color)
    throws RemoteException, InvalidActionException;

  void lobbySetNickname(UUID connectionID, String nickname)
    throws RemoteException, InvalidActionException;

  void lobbyChooseObjective(UUID connectionID, Boolean first)
    throws RemoteException, InvalidActionException;

  void startGame(UUID connectionID)
    throws RemoteException, InvalidActionException;

  void joinGame(UUID connectionID, String gameID, CardSideType sideType)
    throws RemoteException, InvalidActionException;

  void createGame(UUID connectionID, String gameId, Integer players)
    throws RemoteException, InvalidActionException;

  void deleteGame(UUID connectionID, String gameId)
    throws RemoteException, InvalidActionException;

  void nextTurn(UUID connectionID)
    throws RemoteException, InvalidActionException;

  void nextTurn(
    UUID connectionID,
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  ) throws RemoteException, InvalidActionException;

  void placeCard(
    UUID connectionID,
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  ) throws RemoteException, InvalidActionException;

  void leaveLobby(UUID connectionID)
    throws RemoteException, InvalidActionException;

  Set<TokenColor> getAvailableTokens(String gameId)
    throws RemoteException, InvalidActionException;

  public void sendChatMessage(UUID connectionID, ChatMessage message)
    throws RemoteException, InvalidActionException;

  public void connect(UUID connectionID, RemoteGameEventListener listener)
    throws RemoteException;

  public void heartBeat(UUID connectionID) throws RemoteException;
}
