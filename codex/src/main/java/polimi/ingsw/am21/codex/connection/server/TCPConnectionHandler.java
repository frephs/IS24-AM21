package polimi.ingsw.am21.codex.connection.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.messages.Message;
import polimi.ingsw.am21.codex.controller.messages.clientActions.game.*;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.*;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.game.*;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby.*;
import polimi.ingsw.am21.codex.controller.messages.server.game.*;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.*;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.*;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.game.*;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby.*;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.game.*;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby.*;

/** Runnable that handles a TCP connection */
public class TCPConnectionHandler implements Runnable {

  /**
   * The socket handling the TCP connection
   */
  private final Socket socket;
  /**
   * The Game controller
   */
  private final GameController controller;
  /**
   * The queue of incoming messages to handle
   */
  private final Queue<Message> incomingMessages;

  /**
   * Object input stream from the socket
   */
  private final ObjectInputStream inputStream;
  /**
   * Object output stream from the socket
   */
  private final ObjectOutputStream outputStream;

  /**
   * The executor that handles the threads for parsing and handling messages
   */
  private final ExecutorService executor;

  public TCPConnectionHandler(Socket socket, GameController controller) {
    this.socket = socket;
    this.controller = controller;
    this.incomingMessages = new ArrayDeque<>();
    this.executor = Executors.newCachedThreadPool();

    try {
      this.inputStream = new ObjectInputStream(socket.getInputStream());
      this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    } catch (IOException error) {
      throw new RuntimeException(
        "TCP connection handler initialization failed: can't get IO streams " +
        "from socket."
      );
    }
  }

  /** Runs the parser and handler threads */
  public void run() {
    startMessageParser();
    startMessageHandler();
  }

  /**
   * Runs a thread that synchronously loads incoming messages from
   * inputStream to incomingMessages
   */
  private void startMessageParser() {
    executor.execute(() -> {
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
          incomingMessages.wait();
        } catch (ClassNotFoundException error) {
          send(new UnknownMessageTypeMessage());
          // TODO break?
        } catch (IOException error) {
          System.err.println(
            "IOException caught when parsing message from " +
            socket.getInetAddress() +
            ". Parser is exiting.\n" +
            error
          );
          break;
        } catch (InterruptedException error) {
          System.err.println(
            "Parser thread for " +
            socket.getInetAddress() +
            "interrupted, exiting."
          );
          break;
        }
      }
    });
  }

  /**
   * Runs a thread that synchronously gets messages from incomingMessages and
   * handles them
   */
  private void startMessageHandler() {
    executor.execute(() -> {
      while (true) synchronized (incomingMessages) {
        try {
          while (incomingMessages.isEmpty()) incomingMessages.wait();

          handleMessage(incomingMessages.poll());
        } catch (InterruptedException error) {
          System.err.println(
            "Message handler thread for " +
            socket.getInetAddress() +
            " interrupted, exiting."
          );
          break;
        }
      }
    });
  }

  /** Determines the message type and calls the appropriate method based on that */
  private void handleMessage(Message message) {
    // TODO switch based on message type

    switch (message.getType()) {
      case DECK_DRAW_CARD -> handleMessage((DeckDrawCardMessage) message);
      case PLACE_CARD -> handleMessage((PlaceCardMessage) message);
      case CARD_PAIR_DRAW -> handleMessage((CardPairDrawMessage) message);
      case JOIN_LOBBY -> handleMessage((JoinLobbyMessage) message);
      case SELECT_FROM_PAIR -> handleMessage((SelectFromPairMessage) message);
      case SET_NICKNAME -> handleMessage((SetNicknameMessage) message);
      case SET_TOKEN_COLOR -> handleMessage((SetTokenColorMessage) message);
      case GET_GAME_STATUS -> handleMessage((GetGameStatusMessage) message);
      case GET_AVAILABLE_GAME_LOBBIES -> handleMessage(
        (GetAvailableGameLobbiesMessage) message
      );
      case GET_AVAILABLE_TOKEN_COLORS -> handleMessage(
        (GetAvailableTokenColorsMessage) message
      );
      case GET_OBJECTIVE_CARDS -> handleMessage(
        (GetObjectiveCardsMessage) message
      );
      case GET_STARTER_CARD_SIDE -> handleMessage(
        (GetStarterCardSideMessage) message
      );
      case GAME_STATUS -> handleMessage((GameStatusMessage) message);
      case AVAILABLE_GAME_LOBBIES -> handleMessage(
        (AvailableGameLobbiesMessage) message
      );
      case AVAILABLE_TOKEN_COLORS -> handleMessage(
        (AvailableTokenColorsMessage) message
      );
      case OBJECTIVE_CARDS -> handleMessage((ObjectiveCardsMessage) message);
      case STARTER_CARD_SIDES -> handleMessage(
        (StarterCardSidesMessage) message
      );
      case INVALID_CARD_PLACEMENT -> handleMessage(
        (InvalidCardPlacementMessage) message
      );
      case GAME_FULL -> handleMessage((GameFullMessage) message);
      case NICKNAME_ALREADY_TAKEN -> handleMessage(
        (NicknameAlreadyTakenMessage) message
      );
      case TOKEN_COLOR_ALREADY_TAKEN -> handleMessage(
        (TokenColorAlreadyTakenMessage) message
      );
      case ACTION_NOT_ALLOWED -> handleMessage(
        (ActionNotAllowedMessage) message
      );
      case UNKNOWN_MESSAGE_TYPE -> handleMessage(
        (UnknownMessageTypeMessage) message
      );
      case CARD_PLACED -> handleMessage((CardPlacedMessage) message);
      case DECK_CARD_DRAWN -> handleMessage((DeckCardDrawnMessage) message);
      case GAME_OVER -> handleMessage((GameOverMessage) message);
      case PLAYER_SCORE_UPDATE -> handleMessage(
        (PlayerScoreUpdateMessage) message
      );
      case REMAINING_TURNS -> handleMessage((RemainingTurnsMessage) message);
      case WINNING_PLAYER -> handleMessage((WinningPlayerMessage) message);
      case PLAYER_GAME_JOIN -> handleMessage((PlayerGameJoinMessage) message);
      case PLAYER_NICKNAME_SET -> handleMessage(
        (PlayerNicknameSetMessage) message
      );
      case TOKEN_COLOR_SET -> handleMessage((TokenColorSetMessage) message);
    }

    System.out.println(message);
  }

  private void handleMessage(DeckDrawCardMessage message) {
    // TODO
  }

  private void handleMessage(PlaceCardMessage message) {
    // TODO
  }

  private void handleMessage(CardPairDrawMessage message) {
    // TODO
  }

  private void handleMessage(JoinLobbyMessage message) {
    // TODO
  }

  private void handleMessage(SelectFromPairMessage message) {
    // TODO
  }

  private void handleMessage(SetNicknameMessage message) {
    // TODO
  }

  private void handleMessage(SetTokenColorMessage message) {
    // TODO
  }

  private void handleMessage(GetGameStatusMessage message) {
    // TODO
  }

  private void handleMessage(GetAvailableGameLobbiesMessage message) {
    // TODO
  }

  private void handleMessage(GetAvailableTokenColorsMessage message) {
    // TODO
  }

  private void handleMessage(GetObjectiveCardsMessage message) {
    // TODO
  }

  private void handleMessage(GetStarterCardSideMessage message) {
    // TODO
  }

  private void handleMessage(GameStatusMessage message) {
    // TODO
  }

  private void handleMessage(AvailableGameLobbiesMessage message) {
    // TODO
  }

  private void handleMessage(AvailableTokenColorsMessage message) {
    // TODO
  }

  private void handleMessage(ObjectiveCardsMessage message) {
    // TODO
  }

  private void handleMessage(StarterCardSidesMessage message) {
    // TODO
  }

  private void handleMessage(InvalidCardPlacementMessage message) {
    // TODO
  }

  private void handleMessage(GameFullMessage message) {
    // TODO
  }

  private void handleMessage(NicknameAlreadyTakenMessage message) {
    // TODO
  }

  private void handleMessage(TokenColorAlreadyTakenMessage message) {
    // TODO
  }

  private void handleMessage(ActionNotAllowedMessage message) {
    // TODO
  }

  private void handleMessage(UnknownMessageTypeMessage message) {
    // TODO
  }

  private void handleMessage(CardPlacedMessage message) {
    // TODO
  }

  private void handleMessage(DeckCardDrawnMessage message) {
    // TODO
  }

  private void handleMessage(GameOverMessage message) {
    // TODO
  }

  private void handleMessage(PlayerScoreUpdateMessage message) {
    // TODO
  }

  private void handleMessage(RemainingTurnsMessage message) {
    // TODO
  }

  private void handleMessage(WinningPlayerMessage message) {
    // TODO
  }

  private void handleMessage(PlayerGameJoinMessage message) {
    // TODO
  }

  private void handleMessage(PlayerNicknameSetMessage message) {
    // TODO
  }

  private void handleMessage(TokenColorSetMessage message) {
    // TODO
  }

  /** Sends a message synchronously to the client socket */
  public void send(Message message) {
    try {
      // This thread lock is needed because the send method could be called
      // by both the message handler thread and the controller (in case of a
      // server push)
      synchronized (outputStream) {
        outputStream.writeObject(message);
        outputStream.flush();
        outputStream.reset();
      }
    } catch (IOException error) {
      System.err.println(
        "Could not send " +
        message.getType() +
        " to " +
        socket.getInetAddress() +
        ", closing socket."
      );
      try {
        socket.close();
      } catch (IOException ignore) {}
    }
  }
}
