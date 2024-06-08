package polimi.ingsw.am21.codex.connection.client.RMI;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.server.RMI.RMIServerConnectionHandler;
import polimi.ingsw.am21.codex.controller.exceptions.CardAlreadyPlacedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameAlreadyExistsException;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

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
  }

  private UUID getSocketID() {
    return this.localModel.getSocketID();
  }

  @Override
  public void connect() {
    try {
      Registry registry = LocateRegistry.getRegistry(this.port);
      this.rmiConnectionHandler = (RMIServerConnectionHandler) registry.lookup(
        "//" + this.host + ":" + this.port + "/IS24-AM21-CODEX"
      );
      this.localModel.setSocketId(UUID.randomUUID());
      this.rmiConnectionHandler.registerListener(
          this.getSocketID(),
          this.localModel.getRemoteListener()
        );
      this.connectionEstablished();
    } catch (RemoteException | NotBoundException e) {
      this.connectionFailed(e);
    }
  }

  @Override
  public void disconnect() {
    // TODO
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
      rmiConnectionHandler.createGame(gameId, this.socketID, players);
      this.localModel.gameCreated(gameId, 0, players);
    } catch (RemoteException | EmptyDeckException e) {
      throw new RuntimeException(e);
    } catch (GameAlreadyExistsException e) {
      this.localModel.gameAlreadyExists(gameId);
    }
  }

  @Override
  public void connectToGame(String gameId) {
    try {
      rmiConnectionHandler.joinLobby(gameId, this.getSocketID());
      this.localModel.playerJoinedLobby(gameId, this.getSocketID());
    } catch (GameAlreadyStartedException e) {
      this.localModel.gameAlreadyStarted();
    } catch (LobbyFullException e) {
      this.localModel.lobbyFull(gameId);
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
          this.getSocketID()
        );
      this.localModel.playerLeftLobby(
          this.localModel.getGameId(),
          this.getSocketID()
        );
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(localModel.getGameId());
      this.localModel.playerLeftLobby(
          this.localModel.getGameId(),
          this.getSocketID()
        );
    }
  }

  @Override
  public void createAndConnectToGame(String gameId, int numberPlayers) {
    try {
      rmiConnectionHandler.createGame(
        gameId,
        this.getSocketID(),
        numberPlayers
      );
      // TODO
      this.localModel.gameCreated(gameId, 0, numberPlayers);
      this.localModel.playerJoinedLobby(gameId, this.getSocketID());
    } catch (EmptyDeckException e) {
      throw new RuntimeException(e);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (GameAlreadyExistsException e) {
      this.localModel.gameAlreadyExists(gameId);
    }
  }

  @Override
  public void lobbySetToken(TokenColor color) {
    try {
      rmiConnectionHandler.lobbySetTokenColor(
        this.localModel.getGameId(),
        this.getSocketID(),
        color
      );
      this.localModel.playerSetToken(
          this.localModel.getGameId(),
          this.getSocketID(),
          color
        );
      // TODO: implement
      //      localPlayer = new LocalPlayer(color);
    } catch (GameAlreadyStartedException e) {
      localModel.gameAlreadyStarted();
    } catch (GameNotFoundException e) {
      localModel.gameNotFound(this.localModel.getGameId());
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public void showAvailableTokens() {
    this.localModel.showAvailableTokens();
  }

  @Override
  public void lobbySetNickname(String nickname) {
    try {
      rmiConnectionHandler.lobbySetNickname(
        this.localModel.getGameId(),
        this.getSocketID(),
        nickname
      );
      //      this.localModel.getLocalGameBoard().getPlayer().setNickname(nickname);
    } catch (NicknameAlreadyTakenException e) {
      this.localModel.nicknameTaken(nickname);
    } catch (GameAlreadyStartedException e) {
      this.localModel.gameAlreadyStarted();
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(localModel.getGameId());
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public void getObjectiveCards() {
    try {
      localModel.listObjectiveCards(
        rmiConnectionHandler.getLobbyObjectiveCards(
          this.localModel.getGameId(),
          this.socketID
        )
      );
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(localModel.getGameId());
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public void lobbyChooseObjectiveCard(Boolean first) {
    try {
      rmiConnectionHandler.lobbyChooseObjective(
        this.localModel.getGameId(),
        this.getSocketID(),
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
  public void getStarterCard() {
    try {
      localModel.playerGetStarterCardSides(
        rmiConnectionHandler.getLobbyStarterCard(
          localModel.getGameId(),
          socketID
        )
      );
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
        this.getSocketID(),
        cardSide
      );
      // TODO
      //      this.localModel.playerJoinedGame(this.localModel.getGameId(), this.getSocketID(), localPlayer.getNickname(), localPlayer.getToken(), );
    } catch (GameNotReadyException e) {
      this.localModel.gameNotStarted();
    } catch (GameAlreadyStartedException e) {
      this.localModel.playerLeftLobby(
          this.localModel.getGameId(),
          this.getSocketID()
        );
    } catch (EmptyDeckException e) {
      localModel.emptyDeck();
    } catch (
      IllegalCardSideChoiceException
      | IllegalPlacingPositionException
      | GameNotFoundException e
    ) {
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
          this.localModel.getLocalGameBoard().getPlayer().getNickname(),
          playerHandCardNumber,
          side,
          position
        );
    } catch (GameNotFoundException e) {
      localModel.gameNotFound(this.localModel.getGameId());
    } catch (PlayerNotActive e) {
      localModel.playerNotActive();
    } catch (IllegalCardSideChoiceException e) {
      localModel.invalidCardPlacement("Illegal Card Side Choice");
      throw new RuntimeException(e);
    } catch (IllegalPlacingPositionException e) {
      localModel.invalidCardPlacement("Illegal Placing Position");
      throw new RuntimeException(e);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (CardAlreadyPlacedException e) {
      localModel.actionNotAllowed(e.getMessage());
    }
  }

  @Override
  public void leaveLobby() {
    try {
      rmiConnectionHandler.leaveLobby(
        this.localModel.getGameId(),
        this.getSocketID()
      );
      this.localModel.playerLeftLobby(
          this.localModel.getGameId(),
          this.getSocketID()
        );
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(this.localModel.getGameId());
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }

  @Override
  public void nextTurn(
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  ) {
    try {
      this.rmiConnectionHandler.nextTurn(
          localModel.getGameId(),
          localModel.getLocalGameBoard().getPlayer().getNickname(),
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
        localModel.getLocalGameBoard().getPlayer().getNickname()
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

  @Override
  public void sendChatMessage(ChatMessage message) {
    try {
      rmiConnectionHandler.sendChatMessage(localModel.getGameId(), message);
    } catch (GameNotFoundException e) {
      throw new RuntimeException(e);
    } catch (RemoteException e) {
      this.messageNotSent();
    }
  }
}
