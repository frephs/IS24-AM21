package polimi.ingsw.am21.codex.connection.client.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import polimi.ingsw.am21.codex.client.ClientGameEventHandler;
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
import polimi.ingsw.am21.codex.view.View;

public class RMIClientConnectionHandler
  extends ClientConnectionHandler
  implements Remote {

  /**
   * The server connection handler this instance is connected to
   */
  private RMIServerConnectionHandler rmiConnectionHandler;

  public RMIClientConnectionHandler(
    String host,
    Integer port,
    View view,
    ClientGameEventHandler gameEventHandler
  ) {
    super(host, port, view, gameEventHandler);
    this.connectionType = ConnectionType.RMI;
  }

  @Override
  public void connect() {
    try {
      Registry registry = LocateRegistry.getRegistry(this.host, this.port);
      this.rmiConnectionHandler = (RMIServerConnectionHandler) registry.lookup(
        "IS24-AM21-CODEX"
      );
      this.rmiConnectionHandler.connect(
          this.getConnectionID(),
          this.gameEventHandler.getRemoteListener()
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
    if (this.gameEventHandler.getLocalModel().getGameId().isEmpty()) {
      this.gameEventHandler.notInGame();
      return Optional.empty();
    }
    String gameID = this.gameEventHandler.getLocalModel().getGameId().get();
    return Optional.of(gameID);
  }

  @Override
  public void getGames() {
    try {
      Set<String> games = rmiConnectionHandler.getGames();
      Map<String, Integer> currentPlayers =
        rmiConnectionHandler.getGamesCurrentPlayers();
      Map<String, Integer> maxPlayers =
        rmiConnectionHandler.getGamesMaxPlayers();

      gameEventHandler.refreshLobbies(games, currentPlayers, maxPlayers);
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public void createGame(String gameId, int players) {
    try {
      rmiConnectionHandler.createGame(this.getConnectionID(), gameId, players);
    } catch (RemoteException | EmptyDeckException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  private void handleInvalidActionException(InvalidActionException e) {
    this.gameEventHandler.handleInvalidActionException(e);
  }

  @Override
  public void connectToGame(String gameId) {
    try {
      rmiConnectionHandler.joinLobby(this.getConnectionID(), gameId);

      this.getObjectiveCards();
      this.getStarterCard();
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void leaveGameLobby() {
    try {
      this.rmiConnectionHandler.leaveLobby(this.getConnectionID());
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
        this.getConnectionID(),
        gameId,
        numberPlayers
      );
      rmiConnectionHandler.joinLobby(this.getConnectionID(), gameId);

      this.getObjectiveCards();
      this.getStarterCard();
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void lobbySetToken(TokenColor color) {
    try {
      rmiConnectionHandler.lobbySetTokenColor(this.getConnectionID(), color);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void lobbySetNickname(String nickname) {
    try {
      rmiConnectionHandler.lobbySetNickname(this.getConnectionID(), nickname);
      this.getObjectiveCards();
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void getObjectiveCards() {
    try {
      gameEventHandler
        .getLocalModel()
        .getObjectiveCards(
          rmiConnectionHandler.getLobbyObjectiveCards(this.getConnectionID())
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
      rmiConnectionHandler.lobbyChooseObjective(this.getConnectionID(), first);
      this.gameEventHandler.getLocalModel().playerChoseObjectiveCard(first);
      //TODO change to clientGameEventHandler
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void getStarterCard() {
    try {
      gameEventHandler
        .getLocalModel()
        .getStarterCard(rmiConnectionHandler.getLobbyStarterCard(connectionID));
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
      rmiConnectionHandler.joinGame(this.getConnectionID(), gameID, cardSide);
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
          this.getConnectionID(),
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
      rmiConnectionHandler.leaveLobby(this.getConnectionID());
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
          this.getConnectionID(),
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
      rmiConnectionHandler.nextTurn(this.getConnectionID());
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void heartBeat(Runnable successful, Runnable failed) {
    try {
      rmiConnectionHandler.heartBeat(this.getConnectionID());
      successful.run();
    } catch (RemoteException e) {
      failed.run();
    }
  }

  @Override
  public void sendChatMessage(ChatMessage message) {
    try {
      rmiConnectionHandler.sendChatMessage(getConnectionID(), message);
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }
}
