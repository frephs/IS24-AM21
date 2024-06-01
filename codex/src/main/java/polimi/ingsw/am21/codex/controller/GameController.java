package polimi.ingsw.am21.codex.controller;

import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.connection.client.RMI.common.GamePlayerInfo;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Game;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.GameManager;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Lobby.Lobby;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.IncompletePlayerBuilderException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.Player;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

public class GameController {

  public enum UserGameContextStatus {
    MENU,
    IN_LOBBY,
    IN_GAME,
  }

  public class UserGameContext {

    public enum ConnectionStatus {
      CONNECTED,
      LOSING,
      DISCONNECTED,
    }

    private Date lastHeartBeat;

    private String gameId;
    private UserGameContextStatus status;

    private RemoteGameEventListener listener;
    private String username;

    private ConnectionStatus connectionStatus;

    public UserGameContext() {
      this.gameId = null;
      this.status = UserGameContextStatus.MENU;
      this.connectionStatus = ConnectionStatus.CONNECTED;
    }

    public UserGameContext(RemoteGameEventListener listener) {
      this();
      this.listener = listener;
    }

    private UserGameContext(
      String gameId,
      UserGameContextStatus status,
      String username,
      RemoteGameEventListener listener
    ) {
      this.gameId = null;
      this.status = status;
      this.username = username;
      this.connectionStatus = ConnectionStatus.CONNECTED;
    }

    /* in game constructor */
    public UserGameContext(String gameId, String username) {
      this(gameId, UserGameContextStatus.IN_GAME, username, null);
    }

    /* in game constructor */
    public UserGameContext(
      String gameId,
      String username,
      RemoteGameEventListener listener
    ) {
      this(gameId, UserGameContextStatus.IN_GAME, username, listener);
    }

    /* in lobby constructor */
    public UserGameContext(String gameId) {
      this(gameId, UserGameContextStatus.IN_LOBBY, null, null);
    }

    /* in lobby constructor */
    public UserGameContext(String gameId, RemoteGameEventListener listener) {
      this(gameId, UserGameContextStatus.IN_LOBBY, null, listener);
    }

    public RemoteGameEventListener getListener() {
      return listener;
    }

    public void setListener(RemoteGameEventListener listener) {
      this.listener = listener;
    }

    public void setLobbyGameId(String gameId) {
      this.gameId = gameId;
      this.status = UserGameContextStatus.IN_LOBBY;
      this.username = null;
    }

    public void setGameId(String gameId, String username) {
      this.gameId = gameId;
      this.status = UserGameContextStatus.IN_GAME;
      this.username = username;
    }

    public void removeGameId() {
      this.gameId = null;
      this.status = UserGameContextStatus.MENU;
      this.username = null;
    }

    /**
     * @return true if the connection has been restored
     */
    public Boolean heartBeat() {
      lastHeartBeat = new Date();
      if (this.connectionStatus == ConnectionStatus.CONNECTED) return false;
      this.connectionStatus = ConnectionStatus.CONNECTED;
      return true;
    }

    public Boolean disconnected() {
      if (this.connectionStatus == ConnectionStatus.DISCONNECTED) return false;
      this.connectionStatus = ConnectionStatus.DISCONNECTED;
      return true;
    }

    /**
     * @return the ConnectionStatus has changed or empty if it hasn't
     */
    public Optional<ConnectionStatus> checkConnection() {
      if (lastHeartBeat != null) {
        long elapsedTime = new Date().getTime() - lastHeartBeat.getTime();
        if (elapsedTime > 10000) {
          if (this.connectionStatus != ConnectionStatus.DISCONNECTED) {
            this.connectionStatus = ConnectionStatus.DISCONNECTED;
            return Optional.of(ConnectionStatus.DISCONNECTED);
          }
        } else if (elapsedTime > 2000) {
          if (this.connectionStatus != ConnectionStatus.LOSING) {
            this.connectionStatus = ConnectionStatus.LOSING;
            return Optional.of(ConnectionStatus.LOSING);
          }
        }
      }
      return Optional.empty();
    }

    public Optional<String> getGameId() {
      return Optional.ofNullable(gameId);
    }

    public Optional<String> getUsername() {
      return Optional.ofNullable(username);
    }

    public UserGameContextStatus getStatus() {
      return status;
    }

    public ConnectionStatus getConnectionStatus() {
      return connectionStatus;
    }
  }

  GameManager manager;

  Map<UUID, UserGameContext> userContexts = new HashMap<>();
  List<RemoteGameEventListener> listeners = new ArrayList<>();

  public GameController() {
    manager = new GameManager();
  }

  public Set<String> getGames() {
    return manager.getGames();
  }

  public Map<String, Integer> getCurrentSlots() {
    return manager.getCurrentSlots();
  }

  public Map<String, Integer> getMaxSlots() {
    return manager.getMaxSlots();
  }

  public Game getGame(String gameId) throws GameNotFoundException {
    return manager.getGame(gameId).orElseThrow(GameNotFoundException::new);
  }

  public void removePlayerFromLobby(Game game, UUID socketID) {
    Lobby lobby = game.getLobby();
    Pair<CardPair<ObjectiveCard>, PlayableCard> oldPlayerCards =
      lobby.removePlayer(socketID);
    game.insertObjectiveCard(oldPlayerCards.getKey().getFirst());
    game.insertObjectiveCard(oldPlayerCards.getKey().getSecond());
    PlayableCard starterCard = oldPlayerCards.getValue();
    starterCard.clearPlayedSide();
    game.insertStarterCard(starterCard);
    userContexts.get(socketID).removeGameId();
  }

  public void removePlayerFromLobby(String gameId, UUID socketID)
    throws GameNotFoundException {
    Game game = this.getGame(gameId);
    this.removePlayerFromLobby(game, socketID);
  }

  public void joinLobby(String gameId, UUID socketID)
    throws GameNotFoundException, LobbyFullException, GameAlreadyStartedException {
    Game game = this.getGame(gameId);

    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();
    try {
      if (lobby.containsSocketID(socketID)) {
        this.removePlayerFromLobby(game, socketID);
      }
      lobby.addPlayer(
        socketID,
        game.drawObjectiveCardPair(),
        game.drawStarterCard()
      );
      if (userContexts.containsKey(socketID)) {
        userContexts.get(socketID).setLobbyGameId(gameId);
      } else {
        userContexts.put(socketID, new UserGameContext(gameId));
      }
      this.getLobbyListeners(gameId).forEach(listener -> {
          try {
            listener.getValue().playerJoinedLobby(gameId, socketID);
          } catch (RemoteException e) {
            // TODO: Handle in a better way
            throw new RuntimeException(e);
          }
        });
    } catch (EmptyDeckException e) {
      throw new RuntimeException("EmptyDeckException");
    }
  }

  private List<Pair<UUID, RemoteGameEventListener>> getLobbyListeners(
    String gameId
  ) {
    // TODO: uncomment when we first join messages
    return this.getAllListeners();
    //    List<Pair<UUID, RemoteGameEventListener>> pairs = userContexts
    //      .keySet()
    //      .stream()
    //      .filter(
    //        sID ->
    //          userContexts.get(sID).getStatus() == UserGameContextStatus
    //          .IN_LOBBY &&
    //          userContexts.get(sID).getGameId().map(gameId::equals).orElse
    //          (false)
    //      )
    //      .map(sID -> new Pair<>(sID, userContexts.get(sID).getListener()))
    //      .collect(Collectors.toList());
    //
    //    listeners
    //      .stream()
    //      .map(listener -> new Pair<>(UUID.randomUUID(), listener))
    //      .forEach(pairs::add);
    //    return pairs;
  }

  private List<Pair<UUID, RemoteGameEventListener>> getGameListeners(
    String gameID
  ) {
    return this.getAllListeners();
    // TODO: uncomment when we first join messages
    //    List<Pair<UUID, RemoteGameEventListener>> pairs = userContexts
    //      .keySet()
    //      .stream()
    //      .filter(
    //        sID ->
    //          userContexts.get(sID).getStatus() == UserGameContextStatus
    //          .IN_GAME &&
    //          userContexts.get(sID).getGameId().map(gameID::equals).orElse
    //          (false)
    //      )
    //      .map(sID -> new Pair<>(sID, userContexts.get(sID).getListener()))
    //      .collect(Collectors.toList());
    //
    //    listeners
    //      .stream()
    //      .map(listener -> new Pair<>(UUID.randomUUID(), listener))
    //      .forEach(pairs::add);
    //    return pairs;
  }

  private List<Pair<UUID, RemoteGameEventListener>> getMenuListeners() {
    return this.getAllListeners();
    //    List<Pair<UUID, RemoteGameEventListener>> pairs = userContexts
    //      .keySet()
    //      .stream()
    //      .filter(
    //        sID -> userContexts.get(sID).getStatus() ==
    //        UserGameContextStatus.MENU
    //      )
    //      .map(sID -> new Pair<>(sID, userContexts.get(sID).getListener()))
    //      .collect(Collectors.toList());
    //
    //    listeners
    //      .stream()
    //      .map(listener -> new Pair<>(UUID.randomUUID(), listener))
    //      .forEach(pairs::add);
    //    return pairs;
  }

  private List<Pair<UUID, RemoteGameEventListener>> getAllListeners() {
    List<Pair<UUID, RemoteGameEventListener>> pairs = userContexts
      .keySet()
      .stream()
      .map(sID -> new Pair<>(sID, userContexts.get(sID).getListener()))
      .filter(pair -> pair.getValue() != null)
      .collect(Collectors.toList());

    listeners
      .stream()
      .map(listener -> new Pair<>(UUID.randomUUID(), listener))
      .forEach(pairs::add);
    return pairs;
  }

  private List<Pair<UUID, UserGameContext>> getSameContextListeners(
    List<UUID> socketIDs,
    Boolean includeSelf
  ) {
    List<UserGameContext> targetContexts = new ArrayList<>();
    for (UUID sID : socketIDs) {
      if (userContexts.containsKey(sID)) {
        targetContexts.add(userContexts.get(sID));
      }
    }
    return userContexts
      .keySet()
      .stream()
      .filter(
        sID ->
          targetContexts
            .stream()
            .anyMatch(
              targetContext ->
                (includeSelf || socketIDs.contains(sID)) &&
                userContexts.get(sID).getStatus() ==
                  targetContext.getStatus() &&
                ((targetContext.getGameId().isEmpty() &&
                    userContexts.get(sID).getGameId().isEmpty()) ||
                  targetContext
                    .getGameId()
                    .equals(userContexts.get(sID).getGameId())) &&
                targetContext.getConnectionStatus() ==
                  UserGameContext.ConnectionStatus.CONNECTED
            )
      )
      .map(sID -> new Pair<>(sID, userContexts.get(sID)))
      .collect(Collectors.toList());
  }

  private List<Pair<UUID, UserGameContext>> getSameContextListeners(
    UUID socketID,
    Boolean includeSelf
  ) {
    return this.getSameContextListeners(
        Collections.singletonList(socketID),
        includeSelf
      );
  }

  public void clientDisconnected(UUID socketID) {
    if (userContexts.containsKey(socketID)) {
      UserGameContext context = userContexts.get(socketID);
      if (context.disconnected()) {}
    }
  }

  /**
   * @param disconnectedClients list of clients that have disconnected
   */
  public void notifyDisconnections(List<UUID> disconnectedClients) {
    List<Pair<UUID, UUID>> listenersToNotify = new ArrayList<>();

    for (UUID disconnectedClient : disconnectedClients) {
      this.getSameContextListeners(disconnectedClient, false).forEach(
          listener -> {
            listenersToNotify.add(
              new Pair<>(listener.getKey(), disconnectedClient)
            );
          }
        );
    }

    while (!listenersToNotify.isEmpty()) {
      Pair<UUID, UUID> toNotify = listenersToNotify.remove(0);
      UserGameContext clientToNotifyContext = userContexts.get(
        toNotify.getKey()
      );
      UserGameContext clientToCheckContext = userContexts.get(
        toNotify.getValue()
      );
      if (
        clientToNotifyContext == null || clientToCheckContext == null
      ) continue;

      try {
        clientToNotifyContext
          .getListener()
          .playerConnectionChanged(
            toNotify.getValue(),
            clientToNotifyContext.getConnectionStatus()
          );
      } catch (RemoteException e) {
        if (userContexts.containsKey(toNotify.getKey())) {
          if (userContexts.get(toNotify.getKey()).disconnected()) {
            listenersToNotify.removeIf(
              listener -> listener.getKey().equals(toNotify.getKey())
            );
            this.getSameContextListeners(toNotify.getKey(), false).forEach(
                listener ->
                  listenersToNotify.add(
                    new Pair<>(listener.getKey(), toNotify.getKey())
                  )
              );
          }
        }
      }
    }
  }

  private void notifySameContextClients(
    UUID socketID,
    Function<RemoteGameEventListener, Void> function
  ) {
    List<UUID> failedListeners = new ArrayList<>();
    this.getSameContextListeners(socketID, true).forEach(listener -> {
        try {
          function.apply(listener.getValue().getListener());
        } catch (RemoteException e) {
          failedListeners.add(listener.getKey());
        }
      });
    this.notifyDisconnections(failedListeners);
  }

  public void checkClientConnection(UUID clientToCheckID) {
    // First UUID is the socket ID of the client to notify
    // the second socket ID is the id of the client that has disconnected
    List<Pair<UUID, UUID>> listenersToNotify = new ArrayList<>();
    userContexts
      .get(clientToCheckID)
      .checkConnection()
      .ifPresent(connectionStatus -> {
        this.getSameContextListeners(clientToCheckID, false).forEach(
            listener ->
              listenersToNotify.add(
                new Pair<>(listener.getKey(), clientToCheckID)
              )
          );
      });
  }

  public void heartBeat(UUID socketID) {
    // TODO: for now we are checking the heartbeat of the players only when a
    //  player
    // sends a message as we don't care about the connection status of players
    // that are not in the same context of other players
    // should we change this to check the heartbeat of all players every x
    // seconds? 🤷‍♂️

    if (
      userContexts.containsKey(socketID) &&
      userContexts.get(socketID).heartBeat()
    ) {
      this.getSameContextListeners(socketID, false).forEach(listener -> {
          try {
            checkClientConnection(socketID);
            listener.playerConnectionChanged(
              socketID,
              userContexts.get(socketID).getConnectionStatus()
            );
          } catch (RemoteException ignored) {}
        });
    }
  }

  public void lobbySetTokenColor(
    String gameId,
    UUID socketID,
    TokenColor color
  )
    throws GameNotFoundException, TokenAlreadyTakenException, GameAlreadyStartedException {
    Game game = this.getGame(gameId);

    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();
    lobby.setToken(socketID, color);
    this.getLobbyListeners(gameId).forEach(listener -> {
        try {
          listener.getValue().playerSetToken(gameId, socketID, color);
        } catch (RemoteException e) {
          // TODO: handle in a better way
          throw new RuntimeException(e);
        }
      });
  }

  public void lobbySetNickname(String gameId, UUID socketID, String nickname)
    throws GameNotFoundException, NicknameAlreadyTakenException, GameAlreadyStartedException {
    Game game = this.getGame(gameId);

    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();
    lobby.setNickname(socketID, nickname);
    this.getLobbyListeners(gameId).forEach(listener -> {
        try {
          listener.getValue().playerSetNickname(gameId, socketID, nickname);
        } catch (RemoteException e) {
          // TODO: handle in a better way
          throw new RuntimeException(e);
        }
      });
  }

  public void lobbyChooseObjective(String gameId, UUID socketID, Boolean first)
    throws GameNotFoundException, GameAlreadyStartedException {
    Game game = this.getGame(gameId);

    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();
    lobby.setObjectiveCard(socketID, first);
    this.getLobbyListeners(gameId).forEach(listener -> {
        try {
          if (listener.getKey() != socketID) {
            listener
              .getValue()
              .playerChoseObjectiveCard(
                gameId,
                socketID,
                lobby.getPlayerNickname(socketID).orElse(null)
              );
          }
        } catch (RemoteException e) {
          // TODO: handle in a better way
          System.out.print(
            "Error dispatching playerChoseObjectiveCard event for socket: " +
            listener.getKey()
          );
        }
      });
  }

  private void startGame(String gameId, Game game)
    throws GameNotReadyException, GameAlreadyStartedException {
    game.start();
    this.getGameListeners(gameId).forEach(listener -> {
        try {
          Lobby lobby = game.getLobby();
          userContexts
            .get(listener.getKey())
            .setGameId(
              gameId,
              // TODO: add exception instead of null ??
              lobby.getPlayerNickname(listener.getKey()).orElse(null)
            );
          listener.getValue().gameStarted(gameId, game.getPlayerIds());
        } catch (RemoteException e) {
          // TODO: handle in a better way
          System.out.print(
            "Error dispatching gameStarted event for socket: " +
            listener.getKey()
          );
        }
      });
  }

  public void startGame(String gameId)
    throws GameNotFoundException, GameNotReadyException, GameAlreadyStartedException {
    this.startGame(gameId, getGame(gameId));
  }

  public void joinGame(String gameId, UUID socketID, CardSideType sideType)
    throws GameNotFoundException, IncompletePlayerBuilderException, EmptyDeckException, GameNotReadyException, IllegalCardSideChoiceException, IllegalPlacingPositionException, GameAlreadyStartedException {
    Game game = this.getGame(gameId);
    Player newPlayer = game
      .getLobby()
      .finalizePlayer(socketID, sideType, game.drawHand());
    game.addPlayer(newPlayer);
    this.getGameListeners(gameId).forEach(listener -> {
        try {
          listener
            .getValue()
            .playerJoinedGame(
              gameId,
              socketID,
              newPlayer.getNickname(),
              newPlayer.getToken(),
              newPlayer
                .getBoard()
                .getHand()
                .stream()
                .map(PlayableCard::getId)
                .collect(Collectors.toList()),
              newPlayer.getBoard().getPlayedCards().get(new Position()).getId(),
              newPlayer
                .getBoard()
                .getPlayedCards()
                .get(new Position())
                .getPlayedSideType()
                .orElse(CardSideType.FRONT)
            );
        } catch (RemoteException e) {
          // TODO: handle in a better way
          System.out.print(
            "Error dispatching playerJoinedGame event for socket: " +
            listener.getKey()
          );
        }
      });
    if (game.getPlayersSpotsLeft() == 0) {
      this.startGame(gameId, game);
    }
  }

  public void createGame(String gameId, Integer players)
    throws EmptyDeckException {
    manager.createGame(gameId, players);

    this.getAllListeners()
      .forEach(listener -> {
        try {
          listener.getValue().gameCreated(gameId, 0, players);
        } catch (RemoteException e) {
          // TODO: handle in a better way
          System.out.print(
            "Error dispatching gameCreated event for socket: " +
            listener.getKey()
          );
        }
      });
  }

  public Boolean isLastRound(String gameId) throws GameNotFoundException {
    return this.getGame(gameId).isLastRound();
  }

  public void deleteGame(String gameId) {
    manager.deleteGame(gameId);

    List<Pair<UUID, RemoteGameEventListener>> listeners = new ArrayList<>(
      this.getMenuListeners()
    );
    listeners.addAll(this.getGameListeners(gameId));
    listeners.addAll(this.getLobbyListeners(gameId));

    listeners.forEach(listener -> {
      try {
        listener.getValue().gameDeleted(gameId);
      } catch (RemoteException e) {
        // TODO: handle in a better way
        System.out.print(
          "Error dispatching gameDeleted event for socket: " + listener.getKey()
        );
      }
    });
  }

  private void checkIfCurrentPlayer(Game game, String playerNickname)
    throws PlayerNotActive {
    Player player = game.getCurrentPlayer();
    if (
      player.getNickname().equals(playerNickname)
    ) throw new PlayerNotActive();
  }

  public void nextTurn(String gameId, String playerNickname)
    throws GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive, GameOverException {
    Game game = this.getGame(gameId);
    this.checkIfCurrentPlayer(game, playerNickname);
    game.nextTurn();
    this.getGameListeners(gameId).forEach(listener -> {
        try {
          listener
            .getValue()
            .changeTurn(
              gameId,
              game.getCurrentPlayer().getNickname(),
              game.isLastRound()
            );
        } catch (RemoteException e) {
          // TODO: handle in a better way
          System.out.print(
            "Error dispatching changeTurn event for socket: " +
            listener.getKey()
          );
        }
      });
  }

  public void nextTurn(
    String gameId,
    String playerNickname,
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  )
    throws GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive, GameOverException, EmptyDeckException {
    Game game = this.getGame(gameId);
    this.checkIfCurrentPlayer(game, playerNickname);
    game.nextTurn(
      drawingSource,
      deckType,
      (playerCardId, pairCardId) ->
        this.getGameListeners(gameId).forEach(listener -> {
            try {
              listener
                .getValue()
                .changeTurn(
                  gameId,
                  playerNickname,
                  game.isLastRound(),
                  drawingSource,
                  deckType,
                  playerCardId,
                  pairCardId
                );
            } catch (RemoteException e) {
              // TODO: handle in a better way
              System.out.print(
                "Error dispatching changeTurn event for socket: " +
                listener.getKey()
              );
            }
          })
    );
  }

  public void registerListener(
    UUID socketID,
    RemoteGameEventListener listener
  ) {
    if (!userContexts.containsKey(socketID)) {
      userContexts.put(socketID, new UserGameContext(listener));
    } else {
      userContexts.get(socketID).setListener(listener);
    }
  }

  public void registerGlobalListener(RemoteGameEventListener listener) {
    listeners.add(listener);
  }

  public void unregisterGlobalListener(RemoteGameEventListener listener) {
    listeners.remove(listener);
  }

  public void placeCard(
    String gameId,
    String playerNickname,
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  )
    throws GameNotFoundException, PlayerNotActive, IllegalCardSideChoiceException, IllegalPlacingPositionException {
    Game game = this.getGame(gameId);
    this.checkIfCurrentPlayer(game, playerNickname);
    Player currentPlayer = game.getCurrentPlayer();
    PlayableCard playedCard = currentPlayer.placeCard(
      playerHandCardNumber,
      side,
      position
    );
    this.getGameListeners(gameId).forEach(listener -> {
        try {
          listener
            .getValue()
            .cardPlaced(
              gameId,
              playerNickname,
              playerHandCardNumber,
              playedCard.getId(),
              side,
              position,
              currentPlayer.getPoints(),
              currentPlayer.getBoard().getResources(),
              currentPlayer.getBoard().getObjects(),
              currentPlayer.getBoard().getAvailableSpots(),
              currentPlayer.getBoard().getForbiddenSpots()
            );
        } catch (RemoteException e) {
          // TODO: handle in a better way
          System.out.print(
            "Error dispatching cardPlaced event for socket: " +
            listener.getKey()
          );
        }
      });
  }

  public Set<TokenColor> getAvailableTokens(String gameId)
    throws GameNotFoundException {
    Game game = this.getGame(gameId);
    return game.getLobby().getAvailableColors();
  }
}
