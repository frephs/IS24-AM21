package polimi.ingsw.am21.codex.connection.server.TCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.connection.server.NotAClientMessageException;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.controller.messages.ConfirmMessage;
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
import polimi.ingsw.am21.codex.controller.messages.serverErrors.game.InvalidCardPlacementMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby.GameFullMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby.GameNotFoundMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby.NicknameAlreadyTakenMessage;
import polimi.ingsw.am21.codex.controller.messages.serverErrors.lobby.TokenColorAlreadyTakenMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.game.NextTurnUpdateMessage;
import polimi.ingsw.am21.codex.controller.messages.viewUpdate.lobby.AvailableTokenColorsMessage;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Game;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.PlayerNotFoundException;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

/** Runnable that handles a TCP connection */
public class TCPConnectionHandler implements Runnable {

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
   * The executor associated to the connections thread pool, that can be used to
   * broadcast messages to all clients
   */
  private final ExecutorService serverExecutor;

  public TCPConnectionHandler(
    Socket socket,
    GameController controller,
    ExecutorService serverExecutor
  ) {
    this.socket = socket;
    this.controller = controller;
    this.incomingMessages = new ArrayDeque<>();
    this.localExecutor = Executors.newCachedThreadPool();
    this.serverExecutor = serverExecutor;
    this.socketId = UUID.randomUUID();

    try {
      this.inputStream = new ObjectInputStream(socket.getInputStream());
      this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    } catch (IOException e) {
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
          incomingMessages.wait();
        } catch (ClassNotFoundException e) {
          send(new UnknownMessageTypeMessage());
          // TODO break?
        } catch (IOException e) {
          System.err.println(
            "IOException caught when parsing message from " +
            socket.getInetAddress() +
            ". Parser is exiting.\n" +
            e
          );
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

          try {
            socket.close();
          } catch (IOException ignored) {}
          break;
        } catch (Exception e) {
          System.err.println(
            "Caught controller exception while handling message from " +
            socket.getInetAddress() +
            ". Closing connection."
          );
          System.err.println(e.getMessage());

          try {
            socket.close();
          } catch (IOException ignored) {}
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
      case CONFIRM,
        GAME_STATUS,
        TOKEN_COLOR_SET,
        PLAYER_NICKNAME_SET,
        AVAILABLE_GAME_LOBBIES,
        AVAILABLE_TOKEN_COLORS,
        OBJECTIVE_CARDS,
        STARTER_CARD_SIDES,
        INVALID_CARD_PLACEMENT,
        GAME_FULL,
        GAME_NOT_FOUND,
        NICKNAME_ALREADY_TAKEN,
        TOKEN_COLOR_ALREADY_TAKEN,
        ACTION_NOT_ALLOWED,
        NOT_A_CLIENT_MESSAGE,
        UNKNOWN_MESSAGE_TYPE,
        CARD_PLACED,
        GAME_OVER,
        NEXT_TURN_UPDATE,
        PLAYER_SCORE_UPDATE,
        REMAINING_TURNS,
        WINNING_PLAYER,
        PLAYER_GAME_JOIN -> throw new NotAClientMessageException();
    }

    System.out.println(message);
  }

  private void handleMessage(NextTurnActionMessage message) {
    try {
      // isLastRound() is present both in the message and the controller, we can use either
      if (controller.isLastRound(message.getGameId())) {
        controller.nextTurn(message.getGameId(), message.getPlayerNickname());
      } else {
        controller.nextTurn(
          message.getGameId(),
          message.getPlayerNickname(),
          message.getCardSource(),
          message.getDeck()
        );
      }

      PlayableCard drawnCard = controller
        .getGame(message.getGameId())
        .getPlayer(message.getPlayerNickname())
        .getBoard()
        .getHand()
        .getLast();

      broadcast(
        new NextTurnUpdateMessage(
          message.getGameId(),
          message.getPlayerNickname(),
          message.getCardSource(),
          message.getDeck(),
          drawnCard.getId()
        )
      );
    } catch (GameNotFoundException e) {
      send(new GameNotFoundMessage());
    } catch (
      PlayerNotActive
      | GameOverException
      | EmptyDeckException
      | InvalidNextTurnCallException
      | PlayerNotFoundException e
    ) {
      send(new ActionNotAllowedMessage());
    }
  }

  private void handleMessage(PlaceCardMessage message) {
    try {
      controller.placeCard(
        message.getGameId(),
        message.getPlayerNickname(),
        message.getPlayerHandCardNumber(),
        message.getSide(),
        message.getPosition()
      );
      send(new ConfirmMessage());
    } catch (PlayerNotActive e) {
      send(new ActionNotAllowedMessage());
    } catch (IllegalCardSideChoiceException e) {
      // TODO differentiate for illegal side choice?
      send(new InvalidCardPlacementMessage());
    } catch (IllegalPlacingPositionException e) {
      send(new InvalidCardPlacementMessage());
    } catch (GameNotFoundException e) {
      send(new GameNotFoundMessage());
    }
  }

  private void handleMessage(CreateGameMessage message) {
    try {
      controller.createGame(
        message.getGameId(),
        socketId,
        message.getPlayers()
      );
    } catch (EmptyDeckException e) {
      throw new RuntimeException(e);
    }
  }

  /*
   * TODO also, in other messages, make sure that we are not in the lobby state;
   *  otherwise, use ActionNotAllowedMessage
   */

  private void handleMessage(JoinLobbyMessage message) {
    try {
      controller.joinLobby(message.getLobbyId(), socketId);
      send(new ConfirmMessage());
    } catch (GameNotFoundException e) {
      send(new GameNotFoundMessage());
    } catch (LobbyFullException e) {
      send(new GameFullMessage());
    } catch (GameAlreadyStartedException e) {
      send(new ActionNotAllowedMessage());
    }
  }

  private void handleMessage(SelectCardSideMessage message) {
    try {
      controller.joinGame(
        message.getLobbyId(),
        socketId,
        message.getCardSideType()
      );
      send(new ConfirmMessage());
    } catch (
      GameNotReadyException | GameAlreadyStartedException | EmptyDeckException e
    ) {
      send(new ActionNotAllowedMessage());
    } catch (
      IllegalCardSideChoiceException | IllegalPlacingPositionException e
    ) {
      send(new InvalidCardPlacementMessage());
    } catch (GameNotFoundException e) {
      send(new GameNotFoundMessage());
    }
  }

  private void handleMessage(SelectObjectiveMessage message) {
    try {
      controller.lobbyChooseObjective(
        message.getLobbyId(),
        socketId,
        message.isFirst()
      );
      send(new ConfirmMessage());
    } catch (GameNotFoundException e) {
      send(new GameNotFoundMessage());
    } catch (GameAlreadyStartedException e) {
      send(new ActionNotAllowedMessage());
    }
  }

  private void handleMessage(SetNicknameMessage message) {
    try {
      controller.lobbySetNickname(
        message.getLobbyId(),
        socketId,
        message.getNickname()
      );
      send(new ConfirmMessage());
    } catch (NicknameAlreadyTakenException e) {
      send(new NicknameAlreadyTakenMessage());
    } catch (GameNotFoundException e) {
      send(new GameNotFoundMessage());
    } catch (GameAlreadyStartedException e) {
      send(new ActionNotAllowedMessage());
    }
  }

  private void handleMessage(SetTokenColorMessage message) {
    try {
      controller.lobbySetTokenColor(
        message.getLobbyId(),
        socketId,
        message.getColor()
      );

      List<TokenColor> availableColors = controller
        .getGame(message.getLobbyId())
        .getLobby()
        .getAvailableColors();

      send(new ConfirmMessage());
      broadcast(new AvailableTokenColorsMessage(availableColors), true);
    } catch (GameNotFoundException e) {
      send(new GameNotFoundMessage());
    } catch (TokenAlreadyTakenException e) {
      send(new TokenColorAlreadyTakenMessage());
    } catch (GameAlreadyStartedException e) {
      send(new ActionNotAllowedMessage());
    }
  }

  private void handleMessage(GetGameStatusMessage message) {
    try {
      Game game = controller.getGame(message.getGameId());

      send(new GameStatusMessage(game.getState()));
    } catch (GameNotFoundException e) {
      send(new GameNotFoundMessage());
    }
  }

  private void handleMessage(GetAvailableGameLobbiesMessage message) {
    send(new AvailableGameLobbiesMessage(controller.getGames()));
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
    } catch (GameNotFoundException e) {
      send(new GameNotFoundMessage());
    }
  }

  private void handleMessage(GetStarterCardSideMessage message) {
    try {
      Game game = controller.getGame(message.getGameId());
      PlayableCard starterCard = game.getLobby().getStarterCard(socketId);

      send(new StarterCardSidesMessage(starterCard.getId()));
    } catch (GameNotFoundException e) {
      send(new GameNotFoundMessage());
    }
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
    } catch (IOException e) {
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

  /**
   * Invokes .send() on all TCPConnectionHandler threads in the pool
   * @param message The message to broadcast to all clients
   * @param excludeCurrent Whether to exclude the current thread from being targeted,
   *                       defaults to false
   */
  public void broadcast(Message message, boolean excludeCurrent) {
    // TODO wait until the client "server" part is implemented
  }

  public void broadcast(Message message) {
    broadcast(message, false);
  }
}
