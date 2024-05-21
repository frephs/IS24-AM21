package polimi.ingsw.am21.codex.connection.client.TCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.controller.messages.ClientMessage;
import polimi.ingsw.am21.codex.controller.messages.Message;
import polimi.ingsw.am21.codex.controller.messages.clientActions.game.NextTurnActionMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.game.PlaceCardMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.*;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.game.GetGameStatusMessage;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby.GetAvailableGameLobbiesMessage;
import polimi.ingsw.am21.codex.controller.messages.server.game.GameStatusMessage;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.AvailableGameLobbiesMessage;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.View;

public class TCPConnectionHandler implements ClientConnectionHandler {

  private final String host;
  private final int port;

  private Socket connection;
  private boolean connected = false;

  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;

  private final ExecutorService threadManager = Executors.newCachedThreadPool();

  private UUID socketID;

  private final LocalModelContainer localModel;

  private Consumer<Message> callbackFunction;
  private Boolean waiting = false;

  public TCPConnectionHandler(View view, String host, int port) {
    this.socketID = UUID.randomUUID();
    this.localModel = new LocalModelContainer(socketID, view);

    this.host = host;
    this.port = port;
    this.connect();
  }

  private void consumeCallback(Message message) {
    this.callbackFunction.accept(message);
    this.callbackFunction = null;
  }

  private void send(ClientMessage message) {
    synchronized (outputStream) {
      if (waiting) {
        try {
          outputStream.writeObject(message);
          outputStream.flush();
          outputStream.reset();
          this.waiting = true;
        } catch (IOException ignored) {
          this.getView().postNotification(Notification.CONNECTION_FAILED);
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
        this.connection = new Socket(host, port);
        connected = true;
        this.getView().postNotification(Notification.CONNECTION_ESTABLISHED);
      } catch (IOException e) {
        connected = false;
        throw new RuntimeException(e);
      }
    }

    try {
      assert connection != null;
      this.outputStream = new ObjectOutputStream(connection.getOutputStream());
      this.inputStream = new ObjectInputStream(connection.getInputStream());
    } catch (IOException e) {
      this.getView().postNotification(Notification.CONNECTION_FAILED);
      throw new RuntimeException("Connection Failed! Please restart the game");
    }
  }

  @Override
  public void disconnect() {
    // TODO
  }

  @Override
  public void getGames() {
    this.send(new GetAvailableGameLobbiesMessage());
    this.callbackFunction = message -> {
      switch (message.getType()) {
        case AVAILABLE_GAME_LOBBIES -> {
          AvailableGameLobbiesMessage availableGameLobbiesMessage =
            (AvailableGameLobbiesMessage) message;
          this.getView()
            .drawAvailableGames(
              availableGameLobbiesMessage.getLobbyIds(),
              availableGameLobbiesMessage.getCurrentPlayers(),
              availableGameLobbiesMessage.getMaxPlayers()
            );
        }
        default -> localModel.unknownResponse();
      }
    };
  }

  @Override
  public void connectToGame(String gameId) {
    this.send(new JoinLobbyMessage(gameId));
    this.callbackFunction = message -> {
      switch (message.getType()) {
        case GAME_NOT_FOUND -> localModel.gameDeleted(gameId);
        case GAME_FULL -> localModel.gameFull(gameId);
        case CONFIRM -> localModel.playerJoinedLobby(gameId, this.socketID);
        default -> this.getView()
          .postNotification(Notification.UNKNOWN_RESPONSE);
      }
    };
  }

  @Override
  public void leaveGameLobby() {
    this.send(new LeaveLobbyMessage());
    this.callbackFunction = message -> {
      switch (message.getType()) {
        case CONFIRM -> localModel.playerLeftLobby();
        default -> localModel.unknownResponse();
      }
    };
  }

  @Override
  public void createAndConnectToGame(String gameId, int players) {
    this.send(new CreateGameMessage(gameId, players));
    this.callbackFunction = message -> {
      switch (message.getType()) {
        case CONFIRM -> localModel.gameCreated(gameId, 1, players);
        default -> localModel.unknownResponse();
      }
    };
  }

  @Override
  public void lobbySetToken(TokenColor color) {
    this.send(
        new SetTokenColorMessage(
          color,
          localModel.getLocalGameBoard().getGameId()
        )
      );
    this.callbackFunction = message -> {
      switch (message.getType()) {
        case TOKEN_COLOR_ALREADY_TAKEN -> localModel.tokenTaken(color);
        case CONFIRM -> localModel.playerSetToken(color);
        default -> localModel.unknownResponse();
      }
    };
  }

  @Override
  public Set<TokenColor> getAvailableTokens() {
    // TODO
    return null;
  }

  @Override
  public void lobbySetNickname(String nickname) {
    this.send(
        new SetNicknameMessage(
          nickname,
          localModel.getLocalGameBoard().getGameId()
        )
      );
    this.callbackFunction = message -> {
      switch (message.getType()) {
        case NICKNAME_ALREADY_TAKEN -> localModel.nicknameTaken(nickname);
        case CONFIRM -> localModel.playerSetNickname(nickname);
      }
    };
  }

  @Override
  public void lobbyChooseObjectiveCard(Boolean first) {
    this.send(
        new SelectObjectiveMessage(
          first,
          localModel.getLocalGameBoard().getGameId()
        )
      );
    this.callbackFunction = message -> {
      switch (message.getType()) {
        case CONFIRM -> localModel.playerChoseObjectiveCard(first);
        default -> this.getView()
          .postNotification(Notification.UNKNOWN_RESPONSE);
      }
    };
  }

  @Override
  public void lobbyJoinGame(CardSideType cardSide) {
    this.send(
        new SelectCardSideMessage(
          cardSide,
          localModel.getLocalGameBoard().getGameId()
        )
      );
    //    this.callbackFunction = (message) -> {
    //      switch (message.getType()){
    //        case CONFIRM -> localModel.
    //      }
    //
    //    };
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
          localModel.getLocalGameBoard().getGameId(),
          localModel.getLocalGameBoard().getPlayerNickname(),
          playerHandCardNumber,
          side,
          position
        )
      );

    this.callbackFunction = message -> {
      //      switch (message.getType()){
      //        case
      //        case CONFIRM ->
      //      }
    };
  }

  @Override
  public void leaveLobby() {
    this.send(new LeaveLobbyMessage());
    this.callbackFunction = message -> {
      switch (message.getType()) {
        case CONFIRM -> localModel.playerLeftLobby();
        default -> localModel.unknownResponse();
      }
    };
  }

  @Override
  public void nextTurn(
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  ) {
    this.send(
        new NextTurnActionMessage(
          this.localModel.getLocalGameBoard().getGameId(),
          this.localModel.getLocalGameBoard().getPlayerNickname(),
          drawingSource,
          deckType
        )
      );
  }

  @Override
  public void nextTurn() {}

  public void handleMessage() {}
}
