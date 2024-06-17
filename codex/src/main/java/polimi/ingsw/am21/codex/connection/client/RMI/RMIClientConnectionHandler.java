package polimi.ingsw.am21.codex.connection.client.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.server.RMI.RMIServerConnectionHandler;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class RMIClientConnectionHandler
  extends ClientConnectionHandler
  implements Remote {

  private RMIServerConnectionHandler rmiConnectionHandler;

  public RMIClientConnectionHandler(
    String host,
    Integer port,
    LocalModelContainer localModel
  ) {
    super(host, port, localModel);
    this.connectionType = ConnectionType.RMI;
  }

  @Override
  public void connect() {
    try {
      Registry registry = LocateRegistry.getRegistry(this.port);
      this.rmiConnectionHandler = (RMIServerConnectionHandler) registry.lookup(
        "//" + this.host + ":" + this.port + "/IS24-AM21-CODEX"
      );
      this.rmiConnectionHandler.connect(
          this.getSocketID(),
          this.localModel.getRemoteListener()
        );
      this.connectionEstablished();
    } catch (Exception e) {
      this.connectionFailed(e);
    }
  }

  @Override
  public void disconnect() {
    // TODO
  }

  private Optional<String> getGameIDWithMessage() {
    if (this.localModel.getGameId().isEmpty()) {
      this.localModel.notInGame();
      return Optional.empty();
    }
    String gameID = this.localModel.getGameId().get();
    return Optional.of(gameID);
  }

  @Override
  public void listGames() {
    try {
      Set<String> games = rmiConnectionHandler.getGames();
      Map<String, Integer> currentPlayers =
        rmiConnectionHandler.getGamesCurrentPlayers();
      Map<String, Integer> maxPlayers =
        rmiConnectionHandler.getGamesMaxPlayers();
      localModel.createGames(games, currentPlayers, maxPlayers);
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public void createGame(String gameId, int players) {
    try {
      rmiConnectionHandler.createGame(this.getSocketID(), gameId, players);
      this.localModel.gameCreated(gameId, 0, players);
    } catch (RemoteException | EmptyDeckException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  private void handleInvalidActionException(InvalidActionException e) {
    this.localModel.handleInvalidActionException(e);
  }

  @Override
  public void connectToGame(String gameId) {
    try {
      rmiConnectionHandler.joinLobby(this.getSocketID(), gameId);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void leaveGameLobby() {
    try {
      this.rmiConnectionHandler.leaveLobby(this.getSocketID());
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void createAndConnectToGame(String gameId, int numberPlayers) {
    try {
      rmiConnectionHandler.createGame(
        this.getSocketID(),
        gameId,
        numberPlayers
      );
      rmiConnectionHandler.joinLobby(this.getSocketID(), gameId);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void lobbySetToken(TokenColor color) {
    try {
      rmiConnectionHandler.lobbySetTokenColor(this.getSocketID(), color);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void showAvailableTokens() {
    this.localModel.showAvailableTokens();
  }

  @Override
  public void lobbySetNickname(String nickname) {
    try {
      rmiConnectionHandler.lobbySetNickname(this.getSocketID(), nickname);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void getObjectiveCards() {
    try {
      localModel.listObjectiveCards(
        rmiConnectionHandler.getLobbyObjectiveCards(this.getSocketID())
      );
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void lobbyChooseObjectiveCard(Boolean first) {
    try {
      rmiConnectionHandler.lobbyChooseObjective(this.getSocketID(), first);
      this.localModel.playerChoseObjectiveCard(first);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void getStarterCard() {
    try {
      localModel.playerGetStarterCardSides(
        rmiConnectionHandler.getLobbyStarterCard(socketID)
      );
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void lobbyJoinGame(CardSideType cardSide) {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    try {
      rmiConnectionHandler.joinGame(this.getSocketID(), gameID, cardSide);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void placeCard(
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  ) {
    try {
      this.rmiConnectionHandler.placeCard(
          this.getSocketID(),
          playerHandCardNumber,
          side,
          position
        );
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void leaveLobby() {
    try {
      rmiConnectionHandler.leaveLobby(this.getSocketID());
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void nextTurn(
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  ) {
    try {
      this.rmiConnectionHandler.nextTurn(
          this.getSocketID(),
          drawingSource,
          deckType
        );
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void nextTurn() {
    try {
      rmiConnectionHandler.nextTurn(this.getSocketID());
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void heartBeat(Runnable successful, Runnable failed) {
    try {
      rmiConnectionHandler.heartBeat(this.getSocketID());
      successful.run();
    } catch (RemoteException e) {
      failed.run();
    }
  }

  @Override
  public void sendChatMessage(ChatMessage message) {
    try {
      rmiConnectionHandler.sendChatMessage(getSocketID(), message);
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }
}
