package polimi.ingsw.am21.codex.connection.client.TCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.controller.messages.ClientMessage;
import polimi.ingsw.am21.codex.controller.messages.Message;
import polimi.ingsw.am21.codex.controller.messages.clientActions.game.NextTurnActionMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.game.PlaceCardMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.*;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby.GetAvailableGameLobbiesMessage;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby.GetObjectiveCardsMessage;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby.GetStarterCardSideMessage;
import polimi.ingsw.am21.codex.controller.messages.server.game.GameStatusMessage;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.AvailableGameLobbiesMessage;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.LobbyStatusMessage;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.ObjectiveCardsMessage;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.StarterCardSidesMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.ActionNotAllowedMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.NotAClientMessageMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.UnknownMessageTypeMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.game.GameAlreadyStartedMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.game.InvalidCardPlacementMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby.GameFullMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby.GameNotFoundMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby.NicknameAlreadyTakenMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby.TokenColorAlreadyTakenMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.SocketIdMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.game.*;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby.*;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.View;

public class TCPClientConnectionHandler extends ClientConnectionHandler {

  private Socket socket;

  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;

  private final ExecutorService threadManager = Executors.newCachedThreadPool();

  private Boolean waiting = false;

  private final Queue<Message> incomingMessages;

  public TCPClientConnectionHandler(
    String host,
    int port,
    LocalModelContainer localModel
  ) {
    super(host, port, localModel);
    this.incomingMessages = new ArrayDeque<>();

    this.connect();
  }

  /**
   * Runs a thread that synchronously loads incoming messages from
   * inputStream to incomingMessages
   */
  private void startMessageParser() {
    threadManager.execute(() -> {
      while (true) synchronized (incomingMessages) {
        try {
          Object receviedObject = inputStream.readObject();
          if (
            !(receviedObject instanceof Message)
          ) throw new ClassNotFoundException();

          // This casting to Message is safe since we're checking for the
          // parsed class above.
          incomingMessages.add((Message) receviedObject);

          incomingMessages.notifyAll();
          incomingMessages.wait(1);
        } catch (ClassNotFoundException e) {
          getView().postNotification(Notification.UNKNOWN_MESSAGE);
        } catch (IOException e) {
          getView()
            .postNotification(
              NotificationType.ERROR,
              "IOException caught when parsing message from " +
              socket.getInetAddress() +
              ". Parser is exiting.\n"
            );
          getView().displayException(e);
          break;
        } catch (InterruptedException e) {
          getView()
            .postNotification(
              NotificationType.ERROR,
              "Parser thread for " +
              socket.getInetAddress() +
              "interrupted, exiting."
            );
          getView().displayException(e);
          break;
        }
      }
    });
  }

  private void startMessageHandler() {
    threadManager.execute(() -> {
      while (true) synchronized (incomingMessages) {
        try {
          while (incomingMessages.isEmpty()) incomingMessages.wait();

          handleMessage(incomingMessages.poll());
        } catch (InterruptedException e) {
          System.err.println(
            "Message handler thread for " +
            socket.getInetAddress() +
            " interrupted, exiting."
          );
          break;
        } catch (Exception e) {
          getView().displayException(e);
          System.err.println(
            "Caught handler exception while handling message from " +
            socket.getInetAddress() +
            ". Closing connection."
          );

          this.disconnect();
          break;
        }
      }
    });
  }

  private void send(ClientMessage message) {
    synchronized (outputStream) {
      if (!waiting) {
        try {
          if (socket.isConnected() && !socket.isClosed()) {
            outputStream.writeObject(message);
            outputStream.flush();
            outputStream.reset();
            if (message.getType().isClientRequest()) {
              this.waiting = true;
              getView()
                .postNotification(
                  NotificationType.WARNING,
                  "Sending request. "
                );
            }
          }
        } catch (IOException e) {
          connectionFailed(e);
        }
      } else {
        this.getView().postNotification(Notification.ALREADY_WAITING);
      }
    }
  }

  private View getView() {
    return this.localModel.getView();
  }

  @Override
  public void connect() {
    while (!connected) {
      try {
        this.socket = new Socket(host, port);
        this.socket.setTcpNoDelay(true);
        connected = true;
        connectionEstablished();
      } catch (IOException e) {
        connectionFailed(e);
      }
    }
    try {
      assert socket != null;
      this.outputStream = new ObjectOutputStream(socket.getOutputStream());
      this.inputStream = new ObjectInputStream(socket.getInputStream());

      this.startMessageParser();
      this.startMessageHandler();
    } catch (IOException e) {
      connectionFailed(e);
    }
  }

  @Override
  public void disconnect() {
    // TODO
  }

  @Override
  public void listGames() {
    this.send(new GetAvailableGameLobbiesMessage());
  }

  @Override
  public void createGame(String gameId, int players) {
    this.send(new CreateGameMessage(gameId, players));
  }

  @Override
  public void connectToGame(String gameId) {
    this.send(new JoinLobbyMessage(gameId));
  }

  @Override
  public void leaveGameLobby() {
    this.send(new LeaveLobbyMessage());
  }

  @Override
  public void createAndConnectToGame(String gameId, int players) {
    this.send(new CreateGameMessage(gameId, players));
    this.send(new JoinLobbyMessage(gameId));
  }

  @Override
  public void lobbySetToken(TokenColor color) {
    this.send(new SetTokenColorMessage(color, localModel.getGameId()));
  }

  @Override
  public void showAvailableTokens() {
    localModel.showAvailableTokens();
  }

  @Override
  public void lobbySetNickname(String nickname) {
    this.send(new SetNicknameMessage(nickname, localModel.getGameId()));
  }

  @Override
  public void getObjectiveCards() {
    this.send(new GetObjectiveCardsMessage(localModel.getGameId()));
  }

  @Override
  public void lobbyChooseObjectiveCard(Boolean first) {
    this.send(new SelectObjectiveMessage(first, localModel.getGameId()));
  }

  @Override
  public void getStarterCard() {
    this.send(new GetStarterCardSideMessage(localModel.getGameId()));
  }

  @Override
  public void lobbyJoinGame(CardSideType cardSide) {
    this.send(new SelectCardSideMessage(cardSide, localModel.getGameId()));
  }

  @Override
  public void placeCard(
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  ) {
    this.send(
        //TODO: maybe change these messages so a basic authentication is implemented
        new PlaceCardMessage(
          localModel.getGameId(),
          localModel.getLocalGameBoard().getPlayerNickname(),
          playerHandCardNumber,
          side,
          position
        )
      );
  }

  @Override
  public void leaveLobby() {
    this.send(new LeaveLobbyMessage());
  }

  @Override
  public void nextTurn(
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  ) {
    this.send(
        new NextTurnActionMessage(
          this.localModel.getGameId(),
          this.localModel.getLocalGameBoard().getPlayerNickname(),
          drawingSource,
          deckType
        )
      );
  }

  @Override
  public void nextTurn() {
    this.send(
        new NextTurnActionMessage(
          localModel.getGameId(),
          localModel.getLocalGameBoard().getPlayerNickname()
        )
      );
  }

  public GameState getGameState() {
    //TODO
    return null;
  }

  /*
   * ----------------------
   * MESSAGE PARSING
   * ----------------------
   * */

  public void handleMessage(Message message) {
    if (message.getType().isServerResponse()) {
      this.waiting = false;
      getView()
        .postNotification(NotificationType.CONFIRM, "Response received. ");
    }

    switch (message.getType()) {
      // Server Responses
      // Lobby
      case AVAILABLE_GAME_LOBBIES -> handleMessage(
        (AvailableGameLobbiesMessage) message
      );
      case LOBBY_STATUS -> handleMessage((LobbyStatusMessage) message);
      case OBJECTIVE_CARDS -> handleMessage((ObjectiveCardsMessage) message);
      case STARTER_CARD_SIDES -> handleMessage(
        (StarterCardSidesMessage) message
      );
      // Game
      case GAME_STATUS -> handleMessage((GameStatusMessage) message);
      // Server Errors
      case GAME_ALREADY_STARTED -> handleMessage(
        (GameAlreadyStartedMessage) message
      );
      case ACTION_NOT_ALLOWED -> handleMessage(
        (ActionNotAllowedMessage) message
      );
      case UNKNOWN_MESSAGE_TYPE -> handleMessage(
        (UnknownMessageTypeMessage) message
      );
      case NOT_A_CLIENT_MESSAGE -> handleMessage(
        (NotAClientMessageMessage) message
      );
      // lobby
      case GAME_FULL -> handleMessage((GameFullMessage) message);
      case GAME_NOT_FOUND -> handleMessage((GameNotFoundMessage) message);
      case NICKNAME_ALREADY_TAKEN -> handleMessage(
        (NicknameAlreadyTakenMessage) message
      );
      case TOKEN_COLOR_ALREADY_TAKEN -> handleMessage(
        (TokenColorAlreadyTakenMessage) message
      );
      // game
      case INVALID_CARD_PLACEMENT -> handleMessage(
        (InvalidCardPlacementMessage) message
      );
      // View Updating Messages
      // Lobby
      case GAME_CREATED -> handleMessage((GameCreatedMessage) message);
      case GAME_DELETED -> handleMessage((GameDeletedMessage) message);
      case GAME_STARTED -> handleMessage((GameStartedMessage) message);
      case PLAYER_JOINED_LOBBY -> handleMessage(
        (PlayerJoinedLobbyMessage) message
      );
      case PLAYER_LEFT_LOBBY -> handleMessage((PlayerLeftLobbyMessage) message);
      case PLAYER_SET_NICKNAME -> handleMessage(
        (PlayerSetNicknameMessage) message
      );
      case PLAYER_SET_TOKEN_COLOR -> handleMessage(
        (PlayerSetTokenColorMessage) message
      );
      // Game
      case CARD_PLACED -> handleMessage((CardPlacedMessage) message);
      case GAME_OVER -> handleMessage((GameOverMessage) message);
      case NEXT_TURN_UPDATE -> handleMessage((NextTurnUpdateMessage) message);
      case PLAYER_JOINED_GAME -> handleMessage(
        (PlayerJoinedGameMessage) message
      );
      case PLAYER_SCORES_UPDATE -> handleMessage(
        (PlayerScoresUpdateMessage) message
      );
      case REMAINING_TURNS -> handleMessage((RemainingTurnsMessage) message);
      case WINNING_PLAYER -> handleMessage((WinningPlayerMessage) message);
      // Init
      case SOCKET_ID -> handleMessage((SocketIdMessage) message);
      default -> getView().postNotification(Notification.UNKNOWN_MESSAGE);
    }
  }

  /*
   * -------------------------
   * SERVER RESPONSES HANDLERS
   * -------------------------
   * */

  public void handleMessage(GameStatusMessage message) {
    localModel.gameStatusUpdate(message.getState());
  }

  public void handleMessage(LobbyStatusMessage message) {
    localModel.loadGameLobby(message.getPlayers());
  }

  public void handleMessage(AvailableGameLobbiesMessage message) {
    localModel.createGames(
      message.getLobbyIds(),
      message.getCurrentPlayers(),
      message.getMaxPlayers()
    );
  }

  public void handleMessage(ObjectiveCardsMessage message) {
    localModel.listObjectiveCards(message.getIdPair());
  }

  public void handleMessage(StarterCardSidesMessage message) {
    localModel.playerGetStarterCardSides(message.getCardId());
  }

  /*
   * ------------------------
   * SERVER ERRORS HANDLERS
   * ------------------------
   */

  //game

  public void handleMessage(InvalidCardPlacementMessage message) {
    localModel.invalidCardPlacement(message.getReason());
  }

  public void handleMessage(GameAlreadyStartedMessage ignored) {
    localModel.gameAlreadyStarted();
  }

  //lobby

  public void handleMessage(GameFullMessage message) {
    localModel.lobbyFull(message.getGameId());
  }

  public void handleMessage(GameNotFoundMessage message) {
    localModel.gameNotFound(message.getGameId());
  }

  public void handleMessage(NicknameAlreadyTakenMessage message) {
    localModel.nicknameTaken(message.getNickname());
  }

  public void handleMessage(TokenColorAlreadyTakenMessage message) {
    localModel.tokenTaken(message.getToken());
  }

  public void handleMessage(ActionNotAllowedMessage ignored) {
    localModel.actionNotAllowed();
  }

  public void handleMessage(UnknownMessageTypeMessage ignored) {
    getView()
      .postNotification(
        NotificationType.WARNING,
        "You sent a message unknown to the server "
      );
  }

  public void handleMessage(NotAClientMessageMessage ignored) {
    getView()
      .postNotification(
        NotificationType.WARNING,
        "You sent a message which is not a client's message"
      );
  }

  /*
   * ----------------------
   * VIEW UPDATES HANDLERS
   * ----------------------
   */

  // LOBBY
  public void handleMessage(GameCreatedMessage message) {
    localModel.gameCreated(
      message.getGameId(),
      message.getPlayers(),
      message.getMaxPlayers()
    );
    getView()
      .postNotification(
        NotificationType.RESPONSE,
        "New game created: " + message.getGameId()
      );
  }

  public void handleMessage(GameDeletedMessage message) {
    localModel.gameDeleted(message.getGameId());
  }

  public void handleMessage(GameStartedMessage message) {
    localModel.gameStarted(message.getGameId(), message.getPlayerIds());
  }

  public void handleMessage(PlayerJoinedLobbyMessage message) {
    localModel.playerJoinedLobby(message.getLobbyId(), message.getSocketId());
  }

  public void handleMessage(PlayerLeftLobbyMessage message) {
    localModel.playerLeftLobby(message.getLobbyId(), message.getSocketId());
  }

  public void handleMessage(PlayerSetNicknameMessage message) {
    localModel.playerSetNickname(
      message.getGameId(),
      message.getSocketId(),
      message.getNickname()
    );
  }

  public void handleMessage(PlayerSetTokenColorMessage message) {
    localModel.playerSetToken(
      message.getGameId(),
      message.getSocketId(),
      message.getColor()
    );
  }

  // GAME

  public void handleMessage(CardPlacedMessage message) {
    localModel.cardPlaced(
      message.getGameId(),
      message.getPlayerId(),
      message.getPlayerHandCardNumber(),
      message.getCardId(),
      message.getSide(),
      message.getPosition(),
      message.getNewPlayerScore(),
      message.getUpdatedResources(),
      message.getUpdatedObjects(),
      message.getAvailableSpots(),
      message.getForbiddenSpots()
    );
  }

  public void handleMessage(GameOverMessage ignored) {
    localModel.gameOver();
  }

  public void handleMessage(NextTurnUpdateMessage message) {
    localModel.changeTurn(
      message.getGameId(),
      message.getNickname(),
      message.isLastRound(),
      message.getCardSource(),
      message.getDeck(),
      message.getDrawnCardId(),
      message.getNewPairCardId()
    );
  }

  public void handleMessage(PlayerJoinedGameMessage message) {
    localModel.playerJoinedGame(
      message.getGameId(),
      message.getSocketId(),
      message.getNickname(),
      message.getColor(),
      message.getHandIDs(),
      message.getStarterCardID(),
      message.getStarterSideType()
    );
  }

  public void handleMessage(PlayerScoresUpdateMessage message) {
    localModel.playerScoresUpdate(message.getNewScores());
  }

  public void handleMessage(RemainingTurnsMessage message) {
    localModel.remainingTurns(message.getTurns());
  }

  public void handleMessage(WinningPlayerMessage message) {
    localModel.winningPlayer(message.getWinnerNickname());
  }

  public void handleMessage(SocketIdMessage message) {
    localModel.setSocketId(message.getSocketId());
  }
}
