package polimi.ingsw.am21.codex.connection.client.TCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import polimi.ingsw.am21.codex.Main;
import polimi.ingsw.am21.codex.client.ClientGameEventHandler;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.controller.messages.ClientMessage;
import polimi.ingsw.am21.codex.controller.messages.Message;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.clientActions.ConnectMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.HeartBeatMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.SendChatMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.game.NextTurnActionMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.game.PlaceCardMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.*;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby.GetAvailableGameLobbiesMessage;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby.GetObjectiveCardsMessage;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby.GetStarterCardSideMessage;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.AvailableGameLobbiesMessage;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.ObjectiveCardsMessage;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.StarterCardSidesMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.InvalidActionMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.NotAClientMessageMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.UnknownMessageTypeMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.ChatMessageMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.PlayerConnectionChangedMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.UserContextMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.game.*;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby.*;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.View;

public class TCPClientConnectionHandler extends ClientConnectionHandler {

  /**
   * The socket that is handling the TCP connection with the server
   */
  private Socket socket;

  /**
   * A stream of objects that are automatically deserialized and parsed to local
   * class instances
   */
  private ObjectInputStream inputStream;
  /**
   * A stream of objects that need to be serialized and sent to the server
   */
  private ObjectOutputStream outputStream;

  /**
   * The executor service that is handling all client threads
   */
  private final ExecutorService threadManager = Executors.newCachedThreadPool();

  /**
   * A queue of messages that need to be handled
   */
  private final Queue<Message> incomingMessages;

  public TCPClientConnectionHandler(
    String host,
    int port,
    View view,
    ClientGameEventHandler gameEventHandler
  ) {
    super(host, port, view, gameEventHandler);
    this.connectionType = ConnectionType.TCP;
    this.incomingMessages = new ArrayDeque<>();
  }

  /**
   * Gets the current game ID, and calls {@link ClientGameEventHandler#notInGame()}
   * if the client is not currently in a game
   */
  private Optional<String> getGameIDWithMessage() {
    if (
      this.gameEventHandler.getLocalModel().getGameId().isEmpty()
    ) this.gameEventHandler.notInGame();

    return this.gameEventHandler.getLocalModel().getGameId();
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
        } catch (ClassNotFoundException ignored) {} catch (IOException e) {
          getView()
            .postNotification(
              NotificationType.ERROR,
              "IOException caught when parsing message from " +
              socket.getInetAddress() +
              ". Parser is exiting.\n"
            );
          if (Main.Options.isDebug()) {
            getView().displayException(e);
          }
          break;
        } catch (InterruptedException e) {
          getView()
            .postNotification(
              NotificationType.ERROR,
              "Parser thread for " +
              socket.getInetAddress() +
              "interrupted, exiting."
            );
          if (Main.Options.isDebug()) {
            getView().displayException(e);
          }
          break;
        }
      }
    });
  }

  /**
   * Runs a thread that synchronously handles incoming messages
   */
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

  /**
   * Sends a message to the server
   * @param message The message to send
   * @param successful A callback to execute if the action is successful
   * @param failed A callback to execute if the action is not successful
   */
  private void send(
    ClientMessage message,
    Runnable successful,
    Runnable failed
  ) {
    synchronized (outputStream) {
      try {
        if (socket.isConnected() && !socket.isClosed()) {
          if (
            message.getType() != MessageType.HEART_BEAT &&
            Main.Options.isDebug()
          ) System.out.println("Sending " + message.getType());
          outputStream.writeObject(message);
          outputStream.flush();
          outputStream.reset();
          if (message.getType().isClientRequest()) {
            if (Main.Options.isDebug()) {
              getView()
                .postNotification(
                  NotificationType.WARNING,
                  "Sending request. "
                );
            }
          }
        }
      } catch (IOException e) {
        connectionFailed(e);
        failed.run();
        return;
      }
    }
    successful.run();
  }

  /**
   * Sends a message to the server
   * @param message The message to send
   */
  private void send(ClientMessage message) {
    this.send(message, () -> {}, () -> {});
  }

  /**
   * Gets the view associated
   */
  private View getView() {
    return gameEventHandler.getView();
  }

  @Override
  public void connect() {
    boolean connected = false;
    int attempts = 0;
    while (!connected && attempts++ < 10) {
      try {
        this.socket = new Socket(host, port);
        this.socket.setTcpNoDelay(true);
        connected = true;
      } catch (IOException e) {
        if (Main.Options.isDebug()) {
          getView().displayException(e);
        }
        connectionFailed(e);
      }
    }
    if (!connected) return;
    try {
      assert socket != null;
      this.outputStream = new ObjectOutputStream(socket.getOutputStream());
      this.inputStream = new ObjectInputStream(socket.getInputStream());

      this.startMessageParser();
      this.startMessageHandler();
      this.gameEventHandler.getLocalModel()
        .setConnectionID(this.getConnectionID());
      this.send(new ConnectMessage(this.getConnectionID()));
      connectionEstablished();
    } catch (IOException e) {
      connectionFailed(e);
    }
  }

  @Override
  public void disconnect() {
    // TODO
  }

  @Override
  public void getGames() {
    this.send(new GetAvailableGameLobbiesMessage(this.getConnectionID()));
  }

  @Override
  public void createGame(String gameId, int players) {
    this.send(new CreateGameMessage(this.getConnectionID(), gameId, players));
  }

  @Override
  public void connectToGame(String gameId) {
    this.send(new JoinLobbyMessage(this.getConnectionID(), gameId));
  }

  @Override
  public void leaveGameLobby() {
    this.send(new LeaveLobbyMessage(this.getConnectionID()));
  }

  @Override
  public void createAndConnectToGame(String gameId, int players) {
    this.send(new CreateGameMessage(this.getConnectionID(), gameId, players));
    this.send(new JoinLobbyMessage(this.getConnectionID(), gameId));
  }

  @Override
  public void lobbySetToken(TokenColor color) {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    this.send(new SetTokenColorMessage(this.getConnectionID(), color, gameID));
  }

  @Override
  public void lobbySetNickname(String nickname) {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    this.send(new SetNicknameMessage(this.getConnectionID(), nickname, gameID));
  }

  @Override
  public void getObjectiveCards() {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    this.send(new GetObjectiveCardsMessage(this.getConnectionID(), gameID));
  }

  @Override
  public void lobbyChooseObjectiveCard(Boolean first) {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    this.send(
        new SelectObjectiveMessage(this.getConnectionID(), first, gameID)
      );
  }

  @Override
  public void getStarterCard() {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    this.send(new GetStarterCardSideMessage(this.getConnectionID(), gameID));
  }

  @Override
  public void lobbyJoinGame(CardSideType cardSide) {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    this.send(
        new SelectCardSideMessage(this.getConnectionID(), cardSide, gameID)
      );
  }

  @Override
  public void placeCard(
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  ) {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    this.send(
        new PlaceCardMessage(
          this.getConnectionID(),
          gameID,
          gameEventHandler
            .getLocalModel()
            .getLocalGameBoard()
            .orElseThrow()
            .getPlayerNickname(),
          playerHandCardNumber,
          side,
          position
        )
      );
  }

  @Override
  public void leaveLobby() {
    this.send(new LeaveLobbyMessage(this.getConnectionID()));
  }

  @Override
  public void nextTurn(
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  ) {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    this.send(
        new NextTurnActionMessage(
          this.getConnectionID(),
          gameID,
          this.gameEventHandler.getLocalModel()
            .getLocalGameBoard()
            .orElseThrow()
            .getPlayerNickname(),
          drawingSource,
          deckType
        )
      );
  }

  @Override
  public void nextTurn() {
    if (this.getGameIDWithMessage().isEmpty()) return;
    String gameID = this.getGameIDWithMessage().get();
    this.send(
        new NextTurnActionMessage(
          this.getConnectionID(),
          gameID,
          gameEventHandler
            .getLocalModel()
            .getLocalGameBoard()
            .orElseThrow()
            .getPlayerNickname()
        )
      );
  }

  @Override
  public void heartBeat(Runnable successful, Runnable failed) {
    send(new HeartBeatMessage(this.getConnectionID()), successful, failed);
  }

  @Override
  public void sendChatMessage(ChatMessage message) {
    this.send(new SendChatMessage(getConnectionID(), message));
  }

  public GameState getGameState() {
    //TODO
    return null;
  }

  // <editor-fold desc="Message handling">
  public void handleMessage(Message message) {
    if (message.getType().isServerResponse()) {
      // TODO?
    }

    if (Main.Options.isDebug()) {
      System.out.println("Received " + message.getType());
    }

    switch (message.getType()) {
      // Server Responses
      // Lobby
      case LOBBY_INFO -> handleMessage((LobbyInfoMessage) message);
      case AVAILABLE_GAME_LOBBIES -> handleMessage(
        (AvailableGameLobbiesMessage) message
      );
      case OBJECTIVE_CARDS -> handleMessage((ObjectiveCardsMessage) message);
      case STARTER_CARD_SIDES -> handleMessage(
        (StarterCardSidesMessage) message
      );
      // Server Errors

      case UNKNOWN_MESSAGE_TYPE -> handleMessage(
        (UnknownMessageTypeMessage) message
      );
      case NOT_A_CLIENT_MESSAGE -> handleMessage(
        (NotAClientMessageMessage) message
      );
      case INVALID_ACTION -> handleMessage((InvalidActionMessage) message);
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
      case PLAYER_CHOSE_OBJECTIVE -> handleMessage(
        (PlayerChoseObjectiveCardMessage) message
      );
      case SOCKET_ID -> {}
      case PLAYER_CONNECTION_CHANGED -> handleMessage(
        (PlayerConnectionChangedMessage) message
      );
      case GAME_HALTED_UPDATE -> handleMessage((GameHaltedMessage) message);
      case USER_CONTEXT -> handleMessage((UserContextMessage) message);
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
      case REMAINING_ROUNDS -> handleMessage((RemainingRoundsMessage) message);
      case CHAT_MESSAGE_MESSAGE -> handleMessage((ChatMessageMessage) message);
      case WINNING_PLAYER -> handleMessage((WinningPlayerMessage) message);
      // Init
      default -> getView().postNotification(Notification.UNKNOWN_MESSAGE);
    }
  }

  // <editor-fold desc="Server Responses">
  public void handleMessage(AvailableGameLobbiesMessage message) {
    gameEventHandler.refreshLobbies(
      message.getLobbyIds(),
      message.getCurrentPlayers(),
      message.getMaxPlayers()
    );
  }

  public void handleMessage(ObjectiveCardsMessage message) {
    gameEventHandler.getObjectiveCards(message.getIdPair());
  }

  public void handleMessage(StarterCardSidesMessage message) {
    gameEventHandler.getStarterCard(message.getCardId());
  }

  // </editor-fold>

  // <editor-fold desc="Server Errors">
  public void handleMessage(InvalidActionMessage message) {
    gameEventHandler.handleInvalidActionException(message.toException());
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

  // </editor-fold>

  // <editor-fold desc="View updates">
  // <editor-fold desc="Lobby">
  public void handleMessage(LobbyInfoMessage message) {
    gameEventHandler.lobbyInfo(message.getLobbyUsersInfo());
  }

  public void handleMessage(GameCreatedMessage message) {
    gameEventHandler.gameCreated(
      message.getGameId(),
      message.getPlayers(),
      message.getMaxPlayers()
    );
  }

  public void handleMessage(GameDeletedMessage message) {
    gameEventHandler.gameDeleted(message.getGameId());
  }

  public void handleMessage(GameStartedMessage message) {
    gameEventHandler.gameStarted(message.getGameId(), message.getGameInfo());
  }

  public void handleMessage(PlayerJoinedLobbyMessage message) {
    gameEventHandler.playerJoinedLobby(
      message.getLobbyId(),
      message.getConnectionID()
    );
    if (message.getConnectionID().equals(this.getConnectionID())) {
      this.getObjectiveCards();
      this.getStarterCard();
    }
  }

  public void handleMessage(PlayerLeftLobbyMessage message) {
    gameEventHandler.playerLeftLobby(
      message.getLobbyId(),
      message.getConnectionID()
    );
  }

  public void handleMessage(PlayerSetNicknameMessage message) {
    gameEventHandler.playerSetNickname(
      message.getGameId(),
      message.getConnectionID(),
      message.getNickname()
    );
    if (
      message.getConnectionID().equals(this.getConnectionID())
    ) getObjectiveCards();
  }

  public void handleMessage(PlayerSetTokenColorMessage message) {
    gameEventHandler.playerSetToken(
      message.getGameId(),
      message.getConnectionID(),
      message.getNickname(),
      message.getColor()
    );
  }

  public void handleMessage(PlayerChoseObjectiveCardMessage message) {
    gameEventHandler.playerChoseObjectiveCard(
      message.getGameId(),
      message.getConnectionID(),
      message.getNickname()
    );
  }

  public void handleMessage(PlayerConnectionChangedMessage message) {
    gameEventHandler.playerConnectionChanged(
      message.getConnectionID(),
      message.getNickname(),
      message.getStatus()
    );
  }

  // </editor-fold>

  // <editor-fold desc="Game">
  public void handleMessage(CardPlacedMessage message) {
    gameEventHandler.cardPlaced(
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
    gameEventHandler.gameOver();
  }

  public void handleMessage(NextTurnUpdateMessage message) {
    gameEventHandler.changeTurn(
      message.getGameId(),
      message.getNickname(),
      message.getPlayerIndex(),
      message.isLastRound(),
      message.getCardSource(),
      message.getDeck(),
      message.getDrawnCardId(),
      message.getNewPairCardId(),
      message.getAvailableSpots(),
      message.getForbiddenSpots(),
      message.getResourceDeckTopCardId(),
      message.getGoldDeckTopCardId()
    );
  }

  public void handleMessage(PlayerJoinedGameMessage message) {
    gameEventHandler.playerJoinedGame(
      message.getGameId(),
      message.getConnectionID(),
      message.getNickname(),
      message.getColor(),
      message.getHandIDs(),
      message.getStarterCardID(),
      message.getStarterSideType()
    );
  }

  public void handleMessage(PlayerScoresUpdateMessage message) {
    gameEventHandler.playerScoresUpdate(message.getNewScores());
  }

  public void handleMessage(RemainingRoundsMessage message) {
    gameEventHandler.remainingRounds(message.getGameID(), message.getRounds());
  }

  public void handleMessage(WinningPlayerMessage message) {
    gameEventHandler.winningPlayer(message.getWinnerNickname());
  }

  public void handleMessage(ChatMessageMessage message) {
    gameEventHandler.chatMessage(
      gameEventHandler.getLocalModel().getGameId().orElse(""),
      message.getMessage()
    );
  }

  public void handleMessage(GameHaltedMessage gameHaltedMessage) {
    if (gameHaltedMessage.getHalted()) {
      gameEventHandler.gameHalted(gameHaltedMessage.getGameID());
    } else {
      gameEventHandler.gameResumed(gameHaltedMessage.getGameID());
    }
  }

  public void handleMessage(UserContextMessage message) {
    gameEventHandler.userContext(message.getContext());
  }
  // </editor-fold>
}
