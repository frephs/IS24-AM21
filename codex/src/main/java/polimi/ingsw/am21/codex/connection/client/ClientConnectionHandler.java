package polimi.ingsw.am21.codex.connection.client;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import polimi.ingsw.am21.codex.client.ClientContext;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.CliClient;
import polimi.ingsw.am21.codex.view.View;

public abstract class ClientConnectionHandler {

  protected LocalModelContainer localModel;
  protected UUID socketID;

  protected final String host;
  protected final Integer port;
  private GameController.UserGameContext.ConnectionStatus connectionStatus =
    GameController.UserGameContext.ConnectionStatus.DISCONNECTED;
  private Integer consecutiveFailedHeartBeats = 0;

  public ClientConnectionHandler(
    String host,
    Integer port,
    LocalModelContainer localModel
  ) {
    this.host = host;
    this.port = port;
    this.localModel = localModel;
    this.socketID = UUID.randomUUID();
    this.localModel.setSocketId(socketID);
  }

  protected UUID getSocketID() {
    return localModel.getSocketID();
  }

  View getView() {
    return localModel.getView();
  }

  /*
   * -----------------
   * COMMAND SENDERS
   * -----------------
   * */

  /**
   * Retrieves the list of available games and displays them in the view
   */
  public abstract void listGames();

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
   * Shows the available tokens in the view
   */
  public abstract void showAvailableTokens();

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
  public abstract void heartBeat(Runnable successful, Runnable failed);

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
      GameController.UserGameContext.ConnectionStatus.CONNECTED;
    this.getView().postNotification(Notification.CONNECTION_FAILED);
    this.getView().displayException(e);
    this.getView()
      .postNotification(
        NotificationType.WARNING,
        "Try reconnecting using: reconnect [host port]"
      );
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
    this.listGames();
    this.connectionStatus =
      GameController.UserGameContext.ConnectionStatus.CONNECTED;
    this.getView().postNotification(Notification.CONNECTION_ESTABLISHED);
    System.out.println("Your ID is: " + this.getSocketID());
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
    if (localModel.getAvailableObjectives() == null) {
      getObjectiveCards();
    }
  }

  /**
   * @return local model for testing purposes
   * */
  LocalModelContainer getLocalModel() {
    return localModel;
  }
}
