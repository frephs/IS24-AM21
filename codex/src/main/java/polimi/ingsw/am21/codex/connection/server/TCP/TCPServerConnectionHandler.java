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
import polimi.ingsw.am21.codex.connection.server.NotAClientMessageException;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;
import polimi.ingsw.am21.codex.controller.messages.Message;
import polimi.ingsw.am21.codex.controller.messages.clientActions.game.*;
import polimi.ingsw.am21.codex.controller.messages.clientActions.lobby.*;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.game.*;
import polimi.ingsw.am21.codex.controller.messages.clientRequest.lobby.*;
import polimi.ingsw.am21.codex.controller.messages.server.game.GameStatusMessage;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.AvailableGameLobbiesMessage;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.ObjectiveCardsMessage;
import polimi.ingsw.am21.codex.controller.messages.server.lobby.StarterCardSidesMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.*;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.SocketIdMessage;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Game;

/** Runnable that handles a TCP connection */
public class TCPServerConnectionHandler implements Runnable {

  /**
   * The socket handling the TCP connection
   */
  private final Socket socket;
  /**
   * A UUID associated with the socket
   */
  private final UUID socketId;
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

  /**
   * The map of active connection handlers, used to handle message broadcasting
   */
  private final Map<UUID, TCPServerConnectionHandler> activeHandlers;

  public TCPServerConnectionHandler(
    Socket socket,
    GameController controller,
    UUID socketId,
    Map<UUID, TCPServerConnectionHandler> activeHandlers
  ) {
    this.socket = socket;
    try {
      this.socket.setKeepAlive(true);
    } catch (SocketException e) {
      throw new RuntimeException("Failed to enable TCP/IP Keep-Alive", e);
    }
    this.controller = controller;
    this.incomingMessages = new ArrayDeque<>();
    this.localExecutor = Executors.newCachedThreadPool();
    this.socketId = socketId;
    this.activeHandlers = activeHandlers;

    try {
      this.outputStream = new ObjectOutputStream(socket.getOutputStream());
      this.inputStream = new ObjectInputStream(socket.getInputStream());
    } catch (IOException e) {
      throw new RuntimeException(
        "TCP connection handler initialization failed: can't get IO streams " +
        "from socket."
      );
    }
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
    send(new SocketIdMessage(socketId));
  }

  /**
   * Closes the connection socket and removes the handler from the active map
   */
  private void closeConnection() {
    try {
      socket.close();
    } catch (IOException ignored) {}
    activeHandlers.remove(socketId);
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
            "IOException caught when parsing message from " +
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
    switch (message.getType()) {
      case NEXT_TURN_ACTION -> handleMessage((NextTurnActionMessage) message);
      case PLACE_CARD -> handleMessage((PlaceCardMessage) message);
      case CREATE_GAME -> handleMessage((CreateGameMessage) message);
      case JOIN_LOBBY -> handleMessage((JoinLobbyMessage) message);
      case SELECT_CARD_SIDE -> handleMessage((SelectCardSideMessage) message);
      case SELECT_OBJECTIVE -> handleMessage((SelectObjectiveMessage) message);
      case SET_NICKNAME -> handleMessage((SetNicknameMessage) message);
      case SET_TOKEN_COLOR -> handleMessage((SetTokenColorMessage) message);
      case GET_GAME_STATUS -> handleMessage((GetGameStatusMessage) message);
      case GET_AVAILABLE_GAME_LOBBIES -> handleMessage(
        (GetAvailableGameLobbiesMessage) message
      );
      case GET_OBJECTIVE_CARDS -> handleMessage(
        (GetObjectiveCardsMessage) message
      );
      case GET_STARTER_CARD_SIDE -> handleMessage(
        (GetStarterCardSideMessage) message
      );
      default -> throw new NotAClientMessageException();
    }
  }

  private void handleMessage(NextTurnActionMessage message) {
    try {
      // isLastRound() is present both in the message and the controller, we can use either
      if (controller.isLastRound(message.getGameId())) {
        controller.nextTurn(socketId);
      } else {
        controller.nextTurn(
          socketId,
          message.getCardSource(),
          message.getDeck()
        );
      }
    } catch (InvalidActionException e) {
      send(new InvalidActionMessage(e.getCode(), e.getNotes()));
    }
  }

  private void handleMessage(PlaceCardMessage message) {
    try {
      controller.placeCard(
        socketId,
        message.getPlayerHandCardNumber(),
        message.getSide(),
        message.getPosition()
      );
    } catch (InvalidActionException e) {
      send(new InvalidActionMessage(e.getCode(), e.getNotes()));
    }
  }

  private void handleMessage(CreateGameMessage message) {
    try {
      controller.createGame(
        socketId,
        message.getGameId(),
        message.getPlayers()
      );
    } catch (InvalidActionException e) {
      send(new InvalidActionMessage(e.getCode(), e.getNotes()));
    }
  }

  private void handleMessage(JoinLobbyMessage message) {
    try {
      controller.joinLobby(socketId, message.getLobbyId());
    } catch (InvalidActionException e) {
      send(new InvalidActionMessage(e.getCode(), e.getNotes()));
    }
  }

  private void handleMessage(SelectCardSideMessage message) {
    try {
      controller.joinGame(
        socketId,
        message.getLobbyId(),
        message.getCardSideType()
      );
    } catch (InvalidActionException e) {
      send(new InvalidActionMessage(e.getCode(), e.getNotes()));
    }
  }

  private void handleMessage(SelectObjectiveMessage message) {
    try {
      controller.lobbyChooseObjective(socketId, message.isFirst());
    } catch (InvalidActionException e) {
      send(new InvalidActionMessage(e.getCode(), e.getNotes()));
    }
  }

  private void handleMessage(SetNicknameMessage message) {
    try {
      controller.lobbySetNickname(socketId, message.getNickname());
    } catch (InvalidActionException e) {
      send(new InvalidActionMessage(e.getCode(), e.getNotes()));
    }
  }

  private void handleMessage(SetTokenColorMessage message) {
    try {
      controller.lobbySetTokenColor(socketId, message.getColor());
    } catch (InvalidActionException e) {
      send(new InvalidActionMessage(e.getCode(), e.getNotes()));
    }
  }

  private void handleMessage(GetGameStatusMessage message) {
    try {
      Game game = controller.getGame(message.getGameId());
      send(new GameStatusMessage(game.getState()));
    } catch (InvalidActionException e) {
      send(new InvalidActionMessage(e.getCode(), e.getNotes()));
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
        .getPlayerObjectiveCards(socketId);

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
      send(new InvalidActionMessage(e.getCode(), e.getNotes()));
    }
  }

  private void handleMessage(GetStarterCardSideMessage message) {
    try {
      Game game = controller.getGame(message.getGameId());
      PlayableCard starterCard = game.getLobby().getStarterCard(socketId);

      send(new StarterCardSidesMessage(starterCard.getId()));
    } catch (InvalidActionException e) {
      send(new InvalidActionMessage(e.getCode(), e.getNotes()));
    }
  }

  /** Sends a message synchronously to the client socket */
  public void send(Message message) {
    try {
      if (socket.isConnected() && !socket.isClosed()) {
        System.out.println("Sending " + message.getType() + " to " + socketId);
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
   * @param excludeCurrent Whether to exclude the current thread from being targeted,
   *                       defaults to false
   */
  public void broadcast(Message message, boolean excludeCurrent) {
    activeHandlers.forEach((socketId, handler) -> {
      if (socketId != this.socketId || !excludeCurrent) {
        handler.send(message);
      }
    });
  }

  public void broadcast(Message message) {
    broadcast(message, false);
  }
}
