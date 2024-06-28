package polimi.ingsw.am21.codex.connection.client;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import polimi.ingsw.am21.codex.Main;
import polimi.ingsw.am21.codex.client.ClientContext;
import polimi.ingsw.am21.codex.client.ClientGameEventHandler;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotFoundException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.View;

public abstract class ClientConnectionHandler {

  /**
   * The game event handler associated with the client
   */
  protected final ClientGameEventHandler gameEventHandler;
  /**
   * The ID associated with this connection
   */
  protected UUID connectionID;

  /**
   * The host where the server is running
   */
  protected final String host;
  /**
   * The port that should be used to access the server with the current {@link ClientConnectionHandler#connectionType}
   */
  protected final Integer port;
  /**
   * The status of the connection
   */
  private GameController.UserGameContext.ConnectionStatus connectionStatus =
    GameController.UserGameContext.ConnectionStatus.DISCONNECTED;
  /**
   * The number of consecutive heartbeats that the client will try to send before
   * determining that the server is unreachable
   */
  private Integer consecutiveFailedHeartBeats = 0;
  /**
   * The type of connection teh client is using to communicate with the server
   */
  protected ConnectionType connectionType;

  /**
   * The view the client is using
   */
  private final View view;

  public ClientConnectionHandler(
    String host,
    Integer port,
    View view,
    ClientGameEventHandler gameEventHandler
  ) {
    this(host, port, view, gameEventHandler, UUID.randomUUID());
  }

  public ClientConnectionHandler(
    String host,
    Integer port,
    View view,
    ClientGameEventHandler gameEventHandler,
    UUID connectionID
  ) {
    this.host = host;
    this.port = port;
    this.connectionID = connectionID;
    this.gameEventHandler = gameEventHandler;
    this.gameEventHandler.getLocalModel().setConnectionID(this.connectionID);
    this.view = view;
  }

  protected UUID getConnectionID() {
    return gameEventHandler.getLocalModel().getConnectionID();
  }

  private View getView() {
    return view;
  }

  public ConnectionType getConnectionType() {
    return connectionType;
  }

  /*
   * -----------------
   * COMMAND SENDERS
   * -----------------
   * */

  /**
   * Retrieves the list of available games and displays them in the view
   */
  public abstract void getGames();

  /**
   * @param gameId the id of the game to create
   * @param players the number of players in the game
   * */
  public abstract void createGame(String gameId, int players);

  /**
   * @param gameId the id of the game to connect to
   */
  public abstract void connectToGame(String gameId);

  /**
   * Leaves the game the player has joined, if any
   */
  public abstract void leaveGameLobby();

  /**
   * @param gameId the id of the game create and connect to
   */
  public abstract void createAndConnectToGame(String gameId, int numberPlayers);

  /**
   * @param color the color of the chosen token color
   */
  public abstract void lobbySetToken(TokenColor color);

  /**
   * @param nickname the nickname of the lobby player
   */
  public abstract void lobbySetNickname(String nickname);

  /**
   * Gets the objective cards the client should choose from
   */
  public abstract void getObjectiveCards();

  /**
   * @param first true if the player selects the first card in the pair
   *              otherwise false
   */
  public abstract void lobbyChooseObjectiveCard(Boolean first);

  /**
   * Gets the starter card the player has to place
   */
  public abstract void getStarterCard();

  /**
   * Sets the chosen starter card side and make player join game
   *
   * @param cardSide the starter card side chosen by the player
   */
  public abstract void lobbyJoinGame(CardSideType cardSide);

  /**
   * Places a card in the grid
   *
   * @param playerHandCardNumber the index of the card to place from the player hand
   * @param side which side the player wants to place it
   * @param position the target position
   */
  public abstract void placeCard(
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  );

  /**
   * Leaves lobby
   */
  public abstract void leaveLobby();

  /**
   *
   * draws a card from the game board and continues to the next turn
   *
   * @param drawingSource the source were we get the card
   * @param deckType the type of card that we draw
   */
  public abstract void nextTurn(
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  );

  /**
   * Goes to next turn ( called only when the game is in the last round, since you cannot draw a card in that case)
   */
  public abstract void nextTurn();

  /**
   * Sends a heart beat to the server
   */
  public abstract void heartBeat(Runnable successful, Runnable failed)
    throws PlayerNotFoundException;

  public abstract void sendChatMessage(ChatMessage message);

  /*
   * -----------------
   * CONNECTION HANDLING
   * -----------------
   * */

  public abstract void connect();

  public abstract void disconnect();

  private void disconnected() {
    connectionStatus =
      GameController.UserGameContext.ConnectionStatus.DISCONNECTED;
  }

  public Boolean isConnected() {
    return (
      this.connectionStatus ==
      GameController.UserGameContext.ConnectionStatus.CONNECTED
    );
  }

  public Boolean isLosing() {
    return (
      this.connectionStatus ==
      GameController.UserGameContext.ConnectionStatus.LOSING
    );
  }

  public Boolean isConnectedOrLosing() {
    return this.isConnected() || this.isLosing();
  }

  public void messageNotSent() {
    this.getView().postNotification(Notification.MESSAGE_NOT_SENT);
  }

  public void connectionFailed(Exception e) {
    this.connectionStatus =
      GameController.UserGameContext.ConnectionStatus.DISCONNECTED;
    this.getView().postNotification(Notification.CONNECTION_FAILED);
    if (Main.Options.isDebug()) {
      this.getView().displayException(e);
    }
  }

  private void failedHeartBeat() {
    this.consecutiveFailedHeartBeats++;
    if (this.consecutiveFailedHeartBeats >= 10) {
      this.disconnected();
      this.getView().postNotification(Notification.CONNECTION_FAILED);
    } else if (this.consecutiveFailedHeartBeats >= 2) {
      this.connectionStatus =
        GameController.UserGameContext.ConnectionStatus.LOSING;
      this.getView()
        .postNotification(
          NotificationType.WARNING,
          "Connection lost, trying to reconnect"
        );
    }
  }

  public void connectionEstablished() {
    this.getGames();
    if (
      getLocalModel()
        .getClientContextContainer()
        .get()
        .map(clientContext -> clientContext.equals(ClientContext.GAME))
        .orElse(false)
    ) this.view.drawGame();
    this.connectionStatus =
      GameController.UserGameContext.ConnectionStatus.CONNECTED;
    this.getView().postNotification(Notification.CONNECTION_ESTABLISHED);
    System.out.println("Your ID is: " + this.getConnectionID());
    Runnable heartBeatRunnable = new Runnable() {
      @Override
      public void run() {
        try {
          if (isConnectedOrLosing()) heartBeat(
            () -> {
              connectionStatus =
                GameController.UserGameContext.ConnectionStatus.CONNECTED;
            },
            () -> failedHeartBeat()
          );
        } catch (Exception e) {
          failedHeartBeat();
        }
      }
    };

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    executor.scheduleAtFixedRate(
      heartBeatRunnable,
      0,
      1,
      java.util.concurrent.TimeUnit.SECONDS
    );
  }

  public void getObjectivesIfNull() {
    if (gameEventHandler.getLocalModel().getAvailableObjectives() == null) {
      getObjectiveCards();
    }
  }

  /**
   * @return local model for testing purposes
   * */
  LocalModelContainer getLocalModel() {
    return gameEventHandler.getLocalModel();
  }
}
