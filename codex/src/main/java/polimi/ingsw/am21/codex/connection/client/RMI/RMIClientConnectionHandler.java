package polimi.ingsw.am21.codex.connection.client.RMI;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Set;
import java.util.UUID;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.server.RMI.RMIConnectionHandler;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.View;

public class RMIClientConnectionHandler
  implements Remote, ClientConnectionHandler {

  private LocalPlayer localPlayer;
  private LocalModelContainer localModel;
  private final UUID socketID;

  private Boolean connected;

  private final String host;
  private final Integer port;

  private RMIConnectionHandler rmiConnectionHandler;

  public RMIClientConnectionHandler(View view, String host, Integer port) {
    this.host = host;
    this.port = port;
    this.socketID = UUID.randomUUID();
    this.localModel = new LocalModelContainer(socketID, view);
  }

  private View getView() {
    return this.localModel.getView();
  }

  private void messageNotSent() {
    this.getView().postNotification(Notification.MESSAGE_NOT_SENT);
  }

  private void connectionFailed() {
    this.getView().postNotification(Notification.CONNECTION_FAILED);
  }

  private void connectionEstablished() {
    this.getView().postNotification(Notification.CONNECTION_ESTABLISHED);
  }

  @Override
  public void connect() {
    try {
      Registry registry = LocateRegistry.getRegistry(this.port);
      this.rmiConnectionHandler = (RMIConnectionHandler) registry.lookup(
        "//" + this.host + ":" + this.port + "/IS24-AM21-CODEX"
      );
      this.connectionEstablished();
    } catch (RemoteException | NotBoundException e) {
      this.connectionFailed();
    }
  }

  @Override
  public void disconnect() {
    // TODO
  }

  @Override
  public void getGames() {
    try {
      Set<String> games = rmiConnectionHandler.getGames();
      //TODO: update view
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public void connectToGame(String gameId) {
    try {
      rmiConnectionHandler.joinLobby(gameId, this.socketID);
      this.localModel.playerJoinedLobby(
          gameId,
          this.socketID,
          this.localModel.getAvailableTokens()
        );
    } catch (GameAlreadyStartedException e) {
      this.localModel.gameAlreadyStarted();
    } catch (LobbyFullException e) {
      this.localModel.lobbyFull();
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(gameId);
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public void leaveGameLobby() {
    try {
      this.rmiConnectionHandler.leaveLobby(
          this.localModel.getGameId(),
          this.socketID
        );
      this.localModel.playerLeftLobby(
          this.localModel.getGameId(),
          this.socketID
        );
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(localModel.getGameId());
      this.localModel.playerLeftLobby(
          this.localModel.getGameId(),
          this.socketID
        );
    }
  }

  @Override
  public void createAndConnectToGame(String gameId, int numberPlayers) {
    try {
      try {
        rmiConnectionHandler.createGame(gameId, this.socketID, numberPlayers);
        // TODO
        //      this.localModel.gameCreated(gameId, numberPlayers);
        this.localModel.playerJoinedLobby(
            gameId,
            this.socketID,
            rmiConnectionHandler.getAvailableTokens(gameId)
          );
      } catch (GameNotFoundException ignored) {}
    } catch (EmptyDeckException e) { //    } //      System.err.println("A remote exception occurred" + e.getMessage()); //    } catch (EmptyDeckException e) { //      System.err.println("The game was not found" + e.getMessage()); //    } catch (GameNotFoundException e) { //      System.err.println("The lobby is full" + e.getMessage()); //    } catch (LobbyFullException e) { //      System.err.println("The game already started" + e.getMessage()); //    catch (GameAlreadyStartedException e) {
      throw new RuntimeException(e);
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public void lobbySetToken(TokenColor color) {
    try {
      rmiConnectionHandler.lobbySetTokenColor(
        this.localModel.getGameId(),
        this.socketID,
        color
      );
      this.localModel.playerSetToken(
          this.localModel.getGameId(),
          this.socketID,
          color
        );
      // TODO: implement
      //      localPlayer = new LocalPlayer(color);
    } catch (GameAlreadyStartedException e) {
      // TODO: update view
    } catch (GameNotFoundException e) {
      // TODO: update view
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public Set<TokenColor> getAvailableTokens() {
    return this.localModel.getAvailableTokens();
  }

  @Override
  public void lobbySetNickname(String nickname) {
    try {
      rmiConnectionHandler.lobbySetNickname(
        this.localModel.getGameId(),
        this.socketID,
        nickname
      );
      this.localPlayer.setNickname(nickname);
    } catch (NicknameAlreadyTakenException e) {
      this.localModel.nicknameTaken(nickname);
    } catch (GameAlreadyStartedException e) {
      this.localModel.gameAlreadyStarted();
    } catch (GameNotFoundException e) {} catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public void lobbyChooseObjectiveCard(Boolean first) {
    try {
      rmiConnectionHandler.lobbyChooseObjective(
        this.localModel.getGameId(),
        this.socketID,
        first
      );
      this.localModel.playerChoseObjectiveCard(first);
    } catch (GameAlreadyStartedException e) {
      this.localModel.gameAlreadyStarted();
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(localModel.getGameId());
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public void lobbyJoinGame(CardSideType cardSide) {
    try {
      rmiConnectionHandler.joinGame(
        this.localModel.getGameId(),
        this.socketID,
        cardSide
      );
      // TODO
      //      this.localModel.playerJoinedGame(this.localModel.getGameId(), this.socketID, localPlayer.getNickname(), localPlayer.getToken(), );
    } catch (GameNotReadyException e) {} catch (GameAlreadyStartedException e) {
      this.localModel.playerLeftLobby(
          this.localModel.getGameId(),
          this.socketID
        );
    } catch (EmptyDeckException e) {
      // TODO:
    } catch (IllegalCardSideChoiceException e) {
      throw new RuntimeException(e);
    } catch (IllegalPlacingPositionException e) {
      throw new RuntimeException(e);
    } catch (GameNotFoundException e) {
      throw new RuntimeException(e);
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public void placeCard(
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  ) {
    if (this.localModel.getGameId() == null) {
      this.localModel.notInGame();
    }

    try {
      this.rmiConnectionHandler.placeCard(
          this.localModel.getGameId(),
          this.localPlayer.getNickname(),
          playerHandCardNumber,
          side,
          position
        );
    } catch (GameNotFoundException e) {
      localModel.gameNotFound(this.localModel.getGameId());
    } catch (PlayerNotActive e) {
      localModel.playerNotActive();
    } catch (IllegalCardSideChoiceException e) {
      // TODO: add IllegalCardSideChoiceException handling
    } catch (IllegalPlacingPositionException e) {
      // TODO: add IllegalPlacingPositionException handling
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public void leaveLobby() {}

  @Override
  public void nextTurn(
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  ) {
    try {
      this.rmiConnectionHandler.nextTurn(
          localModel.getGameId(),
          localPlayer.getNickname(),
          drawingSource,
          deckType
        );
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(this.localModel.getGameId());
    } catch (InvalidNextTurnCallException e) {
      this.localModel.invalidNextTurnCall();
    } catch (PlayerNotActive e) {
      this.localModel.playerNotActive();
    } catch (GameOverException e) {
      this.localModel.gameOver();
    } catch (EmptyDeckException e) {
      this.localModel.emptyDeck();
    }
  }

  @Override
  public void nextTurn() {
    try {
      rmiConnectionHandler.nextTurn(
        localModel.getGameId(),
        localPlayer.getNickname()
      );
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(localModel.getGameId());
    } catch (InvalidNextTurnCallException e) {
      this.localModel.invalidNextTurnCall();
    } catch (PlayerNotActive e) {
      this.localModel.playerNotActive();
    } catch (GameOverException e) {
      this.localModel.gameOver();
    }
  }
}
