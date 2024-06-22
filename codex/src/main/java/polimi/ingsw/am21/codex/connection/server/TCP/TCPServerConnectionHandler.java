package polimi.ingsw.am21.codex.connection.server.TCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.Main;
import polimi.ingsw.am21.codex.connection.server.NotAClientMessageException;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotFoundException;
import polimi.ingsw.am21.codex.controller.messages.Message;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.controller.messages.clientActions.ConnectMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.HeartBeatMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.SendChatMessage;
import polimi.ingsw.am21.codex.controller.messages.clientActions.game.*;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.*;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby.*;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.AvailableGameLobbiesMessage;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.ObjectiveCardsMessage;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.StarterCardSidesMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Game;
import polimi.ingsw.am21.codex.model.exceptions.PlayerNotFoundGameException;

/** Runnable that handles a TCP connection */
public class TCPServerConnectionHandler implements Runnable {

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
  private final ExecutorService localExecutor;
  private final TCPServerControllerListener listener;

  public TCPServerConnectionHandler(Socket socket, GameController controller) {
    this.socket = socket;
    try {
      this.socket.setKeepAlive(true);
      this.socket.setTcpNoDelay(true);
    } catch (SocketException e) {
      throw new RuntimeException("Failed to enable TCP/IP Keep-Alive", e);
    }

    this.controller = controller;
    this.incomingMessages = new ArrayDeque<>();
    this.localExecutor = Executors.newCachedThreadPool();

    try {
      this.outputStream = new ObjectOutputStream(socket.getOutputStream());
      this.inputStream = new ObjectInputStream(socket.getInputStream());
    } catch (IOException e) {
      throw new RuntimeException(
        "TCP connection handler initialization failed: can't get IO streams " +
        "from socket."
      );
    }
    this.listener = new TCPServerControllerListener(this::broadcast);
  }

  /** Runs the parser and handler threads */
  public void run() {
    localExecutor.execute(() -> {
      while (true) {
        if (!socket.isConnected()) {
          System.out.println("Connection closed. server");
          break;
        }
      }
    });
    startMessageParser();
    startMessageHandler();
  }

  /**
   * Closes the connection socket and removes the handler from the active map
   */
  private void closeConnection() {
    try {
      socket.close();
    } catch (IOException ignored) {}
  }

  /**
   * Runs a thread that synchronously loads incoming messages from
   * inputStream to incomingMessages
   */
  private void startMessageParser() {
    localExecutor.execute(() -> {
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
          send(new UnknownMessageTypeMessage());
        } catch (IOException e) {
          System.err.println(
            "IOException caught when parsing message from client at " +
            socket.getInetAddress() +
            ". Parser is exiting.\n"
          );
          e.printStackTrace();
          break;
        } catch (InterruptedException e) {
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
    localExecutor.execute(() -> {
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
        } catch (NotAClientMessageException e) {
          System.err.println(
            "Non-client message received by " +
            socket.getInetAddress() +
            ". Replying and closing connection."
          );

          send(new NotAClientMessageMessage());

          closeConnection();
          break;
        } catch (Exception e) {
          System.err.println(
            "Caught controller exception while handling message from " +
            socket.getInetAddress() +
            ". Closing connection."
          );
          e.printStackTrace();

          closeConnection();
          break;
        }
      }
    });
  }

  /** Determines the message type and calls the appropriate method based on that */
  private void handleMessage(Message message)
    throws NotAClientMessageException {
    if (
      Main.Options.isDebug() && message.getType() != MessageType.HEART_BEAT
    ) System.out.println(
      "Received " + message.getType() + " from " + socket.getInetAddress()
    );
    switch (message.getType()) {
      case CONNECT -> handleMessage((ConnectMessage) message);
      case HEART_BEAT -> handleMessage((HeartBeatMessage) message);
      case NEXT_TURN_ACTION -> handleMessage((NextTurnActionMessage) message);
      case PLACE_CARD -> handleMessage((PlaceCardMessage) message);
      case CREATE_GAME -> handleMessage((CreateGameMessage) message);
      case JOIN_LOBBY -> handleMessage((JoinLobbyMessage) message);
      case SELECT_CARD_SIDE -> handleMessage((SelectCardSideMessage) message);
      case SELECT_OBJECTIVE -> handleMessage((SelectObjectiveMessage) message);
      case SET_NICKNAME -> handleMessage((SetNicknameMessage) message);
      case SET_TOKEN_COLOR -> handleMessage((SetTokenColorMessage) message);
      case GET_AVAILABLE_GAME_LOBBIES -> handleMessage(
        (GetAvailableGameLobbiesMessage) message
      );
      case GET_OBJECTIVE_CARDS -> handleMessage(
        (GetObjectiveCardsMessage) message
      );
      case GET_STARTER_CARD_SIDE -> handleMessage(
        (GetStarterCardSideMessage) message
      );
      case SEND_CHAT_MESSAGE -> handleMessage((SendChatMessage) message);
      case LEAVE_LOBBY -> handleMessage((LeaveLobbyMessage) message);
      default -> throw new NotAClientMessageException();
    }
  }

  private void handleMessage(ConnectMessage message) {
    controller.connect(message.getConnectionID(), listener);
  }

  private void handleMessage(NextTurnActionMessage message) {
    try {
      // isLastRound() is present both in the message and the controller, we can use either
      if (controller.isLastRound(message.getGameId())) {
        controller.nextTurn(message.getConnectionID());
      } else {
        controller.nextTurn(
          message.getConnectionID(),
          message.getCardSource(),
          message.getDeck()
        );
      }
    } catch (InvalidActionException e) {
      send(InvalidActionMessage.fromException(e));
    }
  }

  private void handleMessage(PlaceCardMessage message) {
    try {
      controller.placeCard(
        message.getConnectionID(),
        message.getPlayerHandCardNumber(),
        message.getSide(),
        message.getPosition()
      );
    } catch (InvalidActionException e) {
      send(InvalidActionMessage.fromException(e));
    }
  }

  private void handleMessage(CreateGameMessage message) {
    try {
      controller.createGame(
        message.getConnectionID(),
        message.getGameId(),
        message.getPlayers()
      );
    } catch (InvalidActionException e) {
      send(InvalidActionMessage.fromException(e));
    }
  }

  private void handleMessage(JoinLobbyMessage message) {
    try {
      controller.joinLobby(message.getConnectionID(), message.getLobbyId());
    } catch (InvalidActionException e) {
      send(InvalidActionMessage.fromException(e));
    }
  }

  private void handleMessage(SelectCardSideMessage message) {
    try {
      controller.joinGame(
        message.getConnectionID(),
        message.getLobbyId(),
        message.getCardSideType()
      );
    } catch (InvalidActionException e) {
      send(InvalidActionMessage.fromException(e));
    }
  }

  private void handleMessage(SelectObjectiveMessage message) {
    try {
      controller.lobbyChooseObjective(
        message.getConnectionID(),
        message.isFirst()
      );
    } catch (InvalidActionException e) {
      send(InvalidActionMessage.fromException(e));
    }
  }

  private void handleMessage(SetNicknameMessage message) {
    try {
      controller.lobbySetNickname(
        message.getConnectionID(),
        message.getNickname()
      );
    } catch (InvalidActionException e) {
      send(InvalidActionMessage.fromException(e));
    }
  }

  private void handleMessage(SetTokenColorMessage message) {
    try {
      controller.lobbySetTokenColor(
        message.getConnectionID(),
        message.getColor()
      );
    } catch (InvalidActionException e) {
      send(InvalidActionMessage.fromException(e));
    }
  }

  private void handleMessage(GetAvailableGameLobbiesMessage ignored) {
    send(
      new AvailableGameLobbiesMessage(
        controller.getGames(),
        controller.getCurrentSlots(),
        controller.getMaxSlots()
      )
    );
  }

  private void handleMessage(GetObjectiveCardsMessage message) {
    try {
      Game game = controller.getGame(message.getGameId());
      Optional<CardPair<ObjectiveCard>> pair = game
        .getLobby()
        .getPlayerObjectiveCards(message.getConnectionID());

      send(
        new ObjectiveCardsMessage(
          pair
            .map(p -> new Pair<>(p.getFirst().getId(), p.getSecond().getId()))
            .orElseThrow(
              () -> new RuntimeException("No player objective cards found.")
            )
        )
      );
    } catch (InvalidActionException e) {
      send(InvalidActionMessage.fromException(e));
    }
  }

  private void handleMessage(GetStarterCardSideMessage message) {
    try {
      Game game = controller.getGame(message.getGameId());
      PlayableCard starterCard = game
        .getLobby()
        .getStarterCard(message.getConnectionID());

      send(new StarterCardSidesMessage(starterCard.getId()));
    } catch (PlayerNotFoundGameException e) {
      send(InvalidActionMessage.fromException(new PlayerNotFoundException(e)));
    } catch (InvalidActionException e) {
      send(InvalidActionMessage.fromException(e));
    }
  }

  private void handleMessage(HeartBeatMessage message) {
    controller.heartBeat(message.getConnectionID());
  }

  public void handleMessage(SendChatMessage message) {
    try {
      controller.sendChatMessage(
        message.getConnectionID(),
        message.getMessage()
      );
    } catch (InvalidActionException e) {
      send(InvalidActionMessage.fromException(e));
    }
  }

  public void handleMessage(LeaveLobbyMessage message) {
    try {
      controller.quitFromLobby(message.getConnectionID());
    } catch (InvalidActionException e) {
      send(InvalidActionMessage.fromException(e));
    }
  }

  /** Sends a message synchronously to the client socket */
  public void send(Message message) {
    try {
      if (socket.isConnected() && !socket.isClosed()) {
        if (Main.Options.isDebug()) {
          System.out.println("Sending " + message.getType());
        }
        synchronized (outputStream) {
          outputStream.writeObject(message);
          outputStream.flush();
          outputStream.reset();
        }
      } else {
        System.err.println("Socket is not connected, cannot send message.");
      }
    } catch (IOException e) {
      System.err.println(
        "Could not send " +
        message.getType() +
        " to " +
        socket.getInetAddress() +
        ", closing socket. " +
        e
      );
      closeConnection();
    }
  }

  /**
   * Invokes .send() on all TCPConnectionHandler threads in the pool
   * @param message The message to broadcast to all clients
   */
  public void broadcast(Message message) {
    this.send(message);
  }
}
