package polimi.ingsw.am21.codex.connection.client.RMI;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.server.RMI.RMIServerConnectionHandler;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
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
    this.connect();
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

      this.localModel.setSocketId(this.socketID);
      this.rmiConnectionHandler.registerListener(
          this.getSocketID(),
          this.localModel.getRemoteListener()
        );
      this.connectionEstablished();
      this.listGames();
    } catch (RemoteException | NotBoundException e) {
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
      rmiConnectionHandler.createGame(this.socketID, gameId, players);
      this.localModel.gameCreated(gameId, 0, players);
    } catch (RemoteException | EmptyDeckException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  private void handleInvalidActionException(InvalidActionException e) {
    switch (e.getCode()) {
      case PLAYER_NOT_ACTIVE -> this.localModel.playerNotActive();
      case NOT_IN_GAME -> this.localModel.notInGame();
      case GAME_ALREADY_STARTED -> this.localModel.gameAlreadyStarted();
      case INVALID_NEXT_TURN_CALL -> this.localModel.invalidNextTurnCall();
      case INVALID_GET_OBJECTIVE_CARDS_CALL -> this.localModel.invalidGetObjectiveCardsCall();
      case GAME_NOT_READY -> this.localModel.gameNotReady();
      case GAME_NOT_FOUND -> this.localModel.gameNotFound(
          ((GameNotFoundException) e).getGameID()
        );
      case PLAYER_NOT_FOUND -> this.localModel.playerNotFound();
      case INCOMPLETE_LOBBY_PLAYER -> this.localModel.incompleteLobbyPlayer(
          e.getNotes().get(0)
        );
      case EMPTY_DECK -> this.localModel.emptyDeck();
      case ILLEGAL_PLACING_POSITION -> this.localModel.invalidCardPlacement(
          ((IllegalPlacingPositionException) e).getReason()
        );
      case ILLEGAL_CARD_SIDE_CHOICE -> this.localModel.invalidCardPlacement(
          ((IllegalCardSideChoiceException) e).getMessage()
        );
      case LOBBY_FULL -> localModel.lobbyFull(
        ((LobbyFullException) e).getGameID()
      );
      case NICKNAME_ALREADY_TAKEN -> localModel.nicknameTaken(
        ((NicknameAlreadyTakenException) e).getNickname()
      );
      case TOKEN_ALREADY_TAKEN -> localModel.tokenTaken(
        TokenColor.fromString(((TokenAlreadyTakenException) e).getTokenColor())
      );
      case GAME_OVER -> localModel.gameOver();
    }
  }

  @Override
  public void connectToGame(String gameId) {
    try {
      rmiConnectionHandler.joinLobby(this.getSocketID(), gameId);
      this.localModel.playerJoinedLobby(gameId, this.getSocketID());
    } catch (GameAlreadyStartedException e) {
      this.localModel.gameAlreadyStarted();
    } catch (LobbyFullException e) {
      this.localModel.lobbyFull(gameId);
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(gameId);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void leaveGameLobby() {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    try {
      this.rmiConnectionHandler.leaveLobby(this.getSocketID());

      this.localModel.playerLeftLobby(gameID, this.getSocketID());
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(gameID);
      this.localModel.playerLeftLobby(gameID, this.getSocketID());
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
      rmiConnectionHandler.joinLobby(this.socketID, gameId);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void lobbySetToken(TokenColor color) {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    try {
      rmiConnectionHandler.lobbySetTokenColor(this.getSocketID(), color);
    } catch (GameAlreadyStartedException e) {
      localModel.gameAlreadyStarted();
    } catch (GameNotFoundException e) {
      localModel.gameNotFound(gameID);
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
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    try {
      rmiConnectionHandler.lobbySetNickname(this.getSocketID(), nickname);
      //      this.localModel.getLocalGameBoard().getPlayer().setNickname(nickname);
    } catch (NicknameAlreadyTakenException e) {
      this.localModel.nicknameTaken(nickname);
    } catch (GameAlreadyStartedException e) {
      this.localModel.gameAlreadyStarted();
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(gameID);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void getObjectiveCards() {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    try {
      localModel.listObjectiveCards(
        rmiConnectionHandler.getLobbyObjectiveCards(this.socketID)
      );
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(gameID);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void lobbyChooseObjectiveCard(Boolean first) {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    try {
      rmiConnectionHandler.lobbyChooseObjective(this.getSocketID(), first);
      this.localModel.playerChoseObjectiveCard(first);
    } catch (GameAlreadyStartedException e) {
      this.localModel.gameAlreadyStarted();
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(gameID);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void getStarterCard() {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    try {
      localModel.playerGetStarterCardSides(
        rmiConnectionHandler.getLobbyStarterCard(socketID)
      );
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(gameID);
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
    } catch (GameNotReadyException e) {
      this.localModel.gameNotStarted();
    } catch (GameAlreadyStartedException e) {
      this.localModel.playerLeftLobby(gameID, this.getSocketID());
    } catch (EmptyDeckException e) {
      localModel.emptyDeck();
    } catch (IllegalCardSideChoiceException e) {
      this.localModel.illegalCardSideChoice();
    } catch (IllegalPlacingPositionException e) {
      this.localModel.invalidCardPlacement(e.getReason());
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(e.getGameID());
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
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    try {
      this.rmiConnectionHandler.placeCard(
          this.socketID,
          playerHandCardNumber,
          side,
          position
        );
    } catch (GameNotFoundException e) {
      localModel.gameNotFound(gameID);
    } catch (PlayerNotActive e) {
      localModel.playerNotActive();
    } catch (IllegalCardSideChoiceException e) {
      localModel.invalidCardPlacement("Illegal Card Side Choice");
    } catch (IllegalPlacingPositionException e) {
      localModel.invalidCardPlacement("Illegal Placing Position");
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void leaveLobby() {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    try {
      rmiConnectionHandler.leaveLobby(this.getSocketID());
      this.localModel.playerLeftLobby(gameID, this.getSocketID());
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(gameID);
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (InvalidActionException e) {
      // TODO: handle this
    }
  }

  @Override
  public void nextTurn(
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  ) {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    try {
      this.rmiConnectionHandler.nextTurn(
          this.getSocketID(),
          drawingSource,
          deckType
        );
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(gameID);
    } catch (InvalidNextTurnCallException e) {
      this.localModel.invalidNextTurnCall();
    } catch (PlayerNotActive e) {
      this.localModel.playerNotActive();
    } catch (GameOverException e) {
      this.localModel.gameOver();
    } catch (EmptyDeckException e) {
      this.localModel.emptyDeck();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }

  @Override
  public void nextTurn() {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    try {
      rmiConnectionHandler.nextTurn(this.getSocketID());
    } catch (RemoteException e) {
      this.messageNotSent();
    } catch (GameNotFoundException e) {
      this.localModel.gameNotFound(gameID);
    } catch (InvalidNextTurnCallException e) {
      this.localModel.invalidNextTurnCall();
    } catch (PlayerNotActive e) {
      this.localModel.playerNotActive();
    } catch (GameOverException e) {
      this.localModel.gameOver();
    } catch (InvalidActionException e) {
      this.handleInvalidActionException(e);
    }
  }
}
