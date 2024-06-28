package polimi.ingsw.am21.codex.controller;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.exceptions.*;
import polimi.ingsw.am21.codex.controller.listeners.FullUserGameContext;
import polimi.ingsw.am21.codex.controller.listeners.GameInfo;
import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.controller.utils.RemoteListenerFunction;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.Game;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameManager;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Lobby.Lobby;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.IncompletePlayerBuilderException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Player.Player;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.AlreadyPlacedCardGameException;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.PlayerNotFoundGameException;

public class GameController {

  /**
   * The status of a user
   */
  public enum UserGameContextStatus {
    MENU,
    IN_LOBBY,
    IN_GAME,
  }

  public static class UserGameContext {

    public enum ConnectionStatus {
      CONNECTED,
      LOSING,
      DISCONNECTED;

      @Override
      public String toString() {
        return switch (this) {
          case CONNECTED -> "is connected to";
          case DISCONNECTED -> "has disconnected from";
          case LOSING -> "is losing connection to";
        };
      }
    }

    private Date lastHeartBeat;

    private String gameId;
    private UserGameContextStatus status;

    private RemoteGameEventListener listener;
    private String nickname;

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
      String nickname,
      RemoteGameEventListener listener
    ) {
      this.gameId = gameId;
      this.status = status;
      this.nickname = nickname;
      this.connectionStatus = ConnectionStatus.CONNECTED;
      this.listener = listener;
    }

    /* in lobby constructor */
    public UserGameContext(String gameId) {
      this(gameId, UserGameContextStatus.IN_LOBBY, null, null);
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
      this.nickname = null;
    }

    public void setGameId(String gameId, String nickname) {
      this.gameId = gameId;
      this.status = UserGameContextStatus.IN_GAME;
      this.nickname = nickname;
    }

    public void removeGameId() {
      this.gameId = null;
      this.status = UserGameContextStatus.MENU;
      this.nickname = null;
    }

    public void setNickname(String nickname) {
      this.nickname = nickname;
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

    public Optional<String> getNickname() {
      return Optional.ofNullable(nickname);
    }

    public UserGameContextStatus getStatus() {
      return status;
    }

    public ConnectionStatus getConnectionStatus() {
      return connectionStatus;
    }
  }

  /**
   * Used to describe how the events should be dispatched under different circumstances.
   * For each mode we provide a description of the logic structured this way:
   * EVENT_NAME
   * context of the client that caused the event:
   * - listeners that will receive the event
   */
  public enum EventDispatchMode {
    /**
     * menu:<br>
     * - menu listeners<br>
     * lobby:<br>
     * - lobby listeners<br>
     * game:<br>
     * - game listeners<br>
     * - lobby listeners<br>
     */
    TOP_DOWN,

    /**
     * menu:<br>
     * - menu listeners<br>
     * lobby:<br>
     * - lobby listeners<br>
     * - game listeners<br>
     * game:<br>
     * - game listeners<br>
     */
    BOTTOM_UP,

    /**
     * menu:<br>
     * - menu listeners<br>
     * lobby:<br>
     * - lobby listeners<br>
     * - menu listeners<br>
     * game:<br>
     * - game listeners<br>
     * - lobby listeners<br>
     * - menu listeners<br>
     */
    TOP_DOWN_FULL,

    /**
     * menu:<br>
     * - menu listeners<br>
     * - lobby listeners<br>
     * - game listeners<br>
     * lobby:<br>
     * - lobby listeners<br>
     * - game listeners<br>
     * game:<br>
     * - game listeners<br>
     */
    BOTTOM_UP_FULL,

    /**
     * menu:<br>
     * - menu listeners<br>
     * lobby:<br>
     * - lobby listeners<br>
     * - game listeners<br>
     * game:<br>
     * - game listeners<br>
     * - lobby listeners<br>
     */
    BOTH_WAYS,

    /**
     * menu:<br>
     * - menu listeners<br>
     * - game listeners<br>
     * - lobby listeners<br>
     * lobby:<br>
     * - lobby listeners<br>
     * - game listeners<br>
     * - menu listeners<br>
     * game:<br>
     * - game listeners<br>
     * - lobby listeners<br>
     * - menu listeners<br>
     */
    BOTH_WAYS_FULL,

    /**
     * menu:<br>
     * - menu listeners<br>
     * lobby:<br>
     * - lobby listeners<br>
     * game:<br>
     * - game listeners<br>
     */
    SAME_CONTEXT;

    private Integer getContextRanking(UserGameContextStatus contextStatus) {
      return switch (contextStatus) {
        case MENU -> -1;
        case IN_LOBBY -> 0;
        case IN_GAME -> 1;
      };
    }

    private Boolean isMenu(UserGameContextStatus status) {
      return status == UserGameContextStatus.MENU;
    }

    private Boolean isBothMenu(
      UserGameContextStatus dispatcher,
      UserGameContextStatus listener
    ) {
      return this.isMenu(dispatcher) && this.isMenu(listener);
    }

    public Boolean checkDispatchable(
      UserGameContextStatus dispatcher,
      UserGameContextStatus listener
    ) {
      return switch (this) {
        case BOTH_WAYS -> this.isBothMenu(dispatcher, listener) ||
        !this.isMenu(listener);
        case BOTH_WAYS_FULL -> true;
        case BOTTOM_UP -> this.isBothMenu(dispatcher, listener) ||
        (!this.isMenu(listener) &&
          this.getContextRanking(dispatcher) <=
            this.getContextRanking(listener));
        case BOTTOM_UP_FULL -> !this.isMenu(listener) &&
        this.getContextRanking(dispatcher) <= this.getContextRanking(listener);
        case TOP_DOWN -> this.isBothMenu(dispatcher, listener) ||
        (!this.isMenu(listener) &&
          this.getContextRanking(dispatcher) >=
            this.getContextRanking(listener));
        case TOP_DOWN_FULL -> !this.isMenu(listener) &&
        this.getContextRanking(dispatcher) >= this.getContextRanking(listener);
        case SAME_CONTEXT -> dispatcher == listener;
      };
    }
  }

  /**
   * The GameManager associated with this controller
   */
  GameManager manager;

  /**
   * The UserGameContexts associated with this controller, mapped by connection ID
   */
  Map<UUID, UserGameContext> userContexts = new HashMap<>();

  public GameController() {
    manager = new GameManager();
  }

  /**
   * Returns the set of games (by ID) managed by this controller
   */
  public Set<String> getGames() {
    return manager.getGames();
  }

  /**
   * Returns the current slots for each game (mapped by ID) managed by this controller
   */
  public Map<String, Integer> getCurrentSlots() {
    return manager.getCurrentSlots();
  }

  /**
   * Returns the maximum slots for each game (mapped by ID) managed by this controller
   */
  public Map<String, Integer> getMaxSlots() {
    return manager.getMaxSlots();
  }

  /**
   * Returns the game with the given ID, if it exists
   */
  public Game getGame(String gameId) throws GameNotFoundException {
    return manager
      .getGame(gameId)
      .orElseThrow(() -> new GameNotFoundException(gameId));
  }

  /**
   * Notifies the clients that one or more clients have disconnected
   * @param disconnectedClients list of clients that have disconnected
   */
  public void notifyDisconnections(List<UUID> disconnectedClients) {
    List<Pair<UUID, UUID>> listenersToNotify = new ArrayList<>();

    // Populate the list of listeners to notify
    for (UUID disconnectedClient : disconnectedClients) {
      this.getSameContextListeners(
          disconnectedClient,
          false,
          EventDispatchMode.TOP_DOWN
        ).forEach(
          listener ->
            listenersToNotify.add(
              new Pair<>(listener.getKey(), disconnectedClient)
            )
        );
    }

    // For each of the listeners to notify, ...
    while (!listenersToNotify.isEmpty()) {
      Pair<UUID, UUID> toNotify = listenersToNotify.removeFirst();
      UserGameContext clientToNotifyContext = userContexts.get(
        toNotify.getKey()
      );
      UserGameContext clientToCheckContext = userContexts.get(
        toNotify.getValue()
      );
      if (
        clientToNotifyContext == null || clientToCheckContext == null
      ) continue;

      // Call .playerConnectionChanged() on the listener
      try {
        clientToNotifyContext
          .getListener()
          .playerConnectionChanged(
            toNotify.getValue(),
            clientToNotifyContext.getNickname().orElse(null),
            clientToNotifyContext.getConnectionStatus()
          );
      } catch (RemoteException e) {
        if (userContexts.containsKey(toNotify.getKey())) {
          if (userContexts.get(toNotify.getKey()).disconnected()) {
            listenersToNotify.removeIf(
              listener -> listener.getKey().equals(toNotify.getKey())
            );
            this.getSameContextListeners(
                toNotify.getKey(),
                false,
                EventDispatchMode.TOP_DOWN
              ).forEach(
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

  /**
   * Returns the UserGameContext associated with the given connection ID
   * @param connectionID the connection ID
   */
  private UserGameContext getUserContext(UUID connectionID)
    throws PlayerNotFoundException {
    if (
      !userContexts.containsKey(connectionID)
    ) throw new PlayerNotFoundException(connectionID);
    return userContexts.get(connectionID);
  }

  public void notifyDisconnectionsSameContext(
    List<UUID> disconnectedClients,
    List<Pair<UUID, UserGameContext>> sameContextClients
  ) {
    if (disconnectedClients.isEmpty()) return;
    List<Pair<UUID, UUID>> listenersToNotify = new ArrayList<>();

    List<UUID> totalDisconnections = new ArrayList<>(disconnectedClients);

    // first we create a list of pairs that as keys have the client to notify and value the client who disconnected
    for (UUID disconnectedClient : disconnectedClients) {
      sameContextClients
        .stream()
        .filter(
          client ->
            !disconnectedClients.contains(client.getKey()) &&
            client.getValue().getConnectionStatus() ==
              UserGameContext.ConnectionStatus.CONNECTED
        )
        .forEach(
          client ->
            listenersToNotify.add(
              new Pair<>(client.getKey(), disconnectedClient)
            )
        );
    }

    do {
      while (!listenersToNotify.isEmpty()) {
        Pair<UUID, UUID> toNotify = listenersToNotify.removeFirst();
        UserGameContext clientToNotifyContext = userContexts.get(
          toNotify.getKey()
        );
        UserGameContext disconnectingClientContext = userContexts.get(
          toNotify.getValue()
        );
        if (
          clientToNotifyContext == null || disconnectingClientContext == null
        ) continue;

        try {
          clientToNotifyContext
            .getListener()
            .playerConnectionChanged(
              toNotify.getValue(),
              disconnectingClientContext.getNickname().orElse(null),
              disconnectingClientContext.getConnectionStatus()
            );
        } catch (RemoteException e) {
          if (userContexts.containsKey(toNotify.getKey())) {
            totalDisconnections.add(toNotify.getValue());
            if (userContexts.get(toNotify.getKey()).disconnected()) {
              listenersToNotify.removeIf(
                listener -> listener.getKey().equals(toNotify.getKey())
              );
              sameContextClients.forEach(
                listener ->
                  listenersToNotify.add(
                    new Pair<>(listener.getKey(), toNotify.getKey())
                  )
              );
            }
          }
        }
      }
      if (totalDisconnections.isEmpty()) return;

      Set<String> haltedGames = new HashSet<>();
      for (UUID disconnectClient : totalDisconnections) {
        if (userContexts.containsKey(disconnectClient)) {
          UserGameContext userContext = userContexts.get(disconnectClient);
          if (
            userContext.getConnectionStatus() ==
              UserGameContext.ConnectionStatus.DISCONNECTED &&
            userContext.getGameId().isPresent()
          ) {
            if (userContext.getStatus() == UserGameContextStatus.IN_LOBBY) {
              try {
                this.quitFromLobby(disconnectClient);
              } catch (InvalidActionException ignored) {}
            } else if (
              userContext.getStatus() == UserGameContextStatus.IN_GAME
            ) {
              String gameID = userContext.getGameId().get();
              Game game = null;
              try {
                game = this.getGame(gameID);
                Boolean wasGameHalted = game.isGameHalted();
                if (
                  userContext.getNickname().isEmpty()
                ) throw new PlayerNotFoundGameException(disconnectClient);
                game.playerDisconnected(userContext.getNickname().get());
                if (!wasGameHalted && game.isGameHalted()) haltedGames.add(
                  gameID
                );
              } catch (PlayerNotFoundGameException | GameNotFoundException e) {
                userContext.removeGameId();
              }
            }
          }
        }
      }

      userContexts
        .entrySet()
        .stream()
        .filter(
          u ->
            u.getValue().getListener() != null &&
            u.getValue().getConnectionStatus() ==
              UserGameContext.ConnectionStatus.CONNECTED &&
            u.getValue().getGameId().isPresent() &&
            haltedGames.contains(u.getValue().getGameId().get())
        )
        .forEach(u -> {
          try {
            u
              .getValue()
              .getListener()
              .gameHalted(u.getValue().getGameId().get());
          } catch (Exception e) {
            listenersToNotify.removeIf(
              listener -> listener.getKey().equals(u.getKey())
            );
            getSameContextListeners(
              u.getKey(),
              false,
              EventDispatchMode.SAME_CONTEXT
            ).forEach(
              listener ->
                listenersToNotify.add(new Pair<>(listener.getKey(), u.getKey()))
            );
          }
        });
    } while (!listenersToNotify.isEmpty());
  }

  private void notifyClients(
    List<Pair<UUID, UserGameContext>> listeners,
    RemoteListenerFunction function,
    Boolean sameContext
  ) {
    List<UUID> failedListeners = new ArrayList<>();
    listeners.forEach(listener -> {
      try {
        function.apply(listener.getValue().getListener(), listener.getKey());
      } catch (RemoteException e) {
        failedListeners.add(listener.getKey());
      }
    });
    if (sameContext) {
      this.notifyDisconnectionsSameContext(failedListeners, listeners);
    } else {
      this.notifyDisconnections(failedListeners);
    }
  }

  private void notifyClients(
    List<Pair<UUID, UserGameContext>> listeners,
    RemoteListenerFunction function
  ) {
    this.notifyClients(listeners, function, false);
  }

  private void notifySameContextClients(
    UUID connectionID,
    RemoteListenerFunction function,
    EventDispatchMode mode,
    Boolean includeSelf
  ) {
    this.notifyClients(
        this.getSameContextListeners(connectionID, includeSelf, mode),
        function,
        true
      );
  }

  private void notifySameContextClients(
    UUID socketID,
    RemoteListenerFunction function,
    EventDispatchMode mode
  ) {
    this.notifySameContextClients(socketID, function, mode, true);
  }

  private void notifySameContextClients(
    UUID socketID,
    RemoteListenerFunction function,
    Boolean includeSelf
  ) {
    this.notifySameContextClients(
        socketID,
        function,
        EventDispatchMode.BOTH_WAYS,
        includeSelf
      );
  }

  private void notifySameContextClients(
    UUID connectionID,
    RemoteListenerFunction function
  ) {
    this.notifySameContextClients(
        connectionID,
        function,
        EventDispatchMode.BOTH_WAYS,
        true
      );
  }

  public void checkClientConnections() {
    List<UUID> disconnectedClients = new ArrayList<>();
    // these are all the contexts that we need to check and potentially notify

    this.userContexts.entrySet()
      .stream()
      .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
      .forEach(
        context ->
          context
            .getValue()
            .checkConnection()
            .ifPresent(
              connectionStatus -> disconnectedClients.add(context.getKey())
            )
      );

    this.notifyDisconnections(disconnectedClients);
  }

  public void checkClientConnections(UUID heartBeatClient) {
    List<UUID> disconnectedClients = new ArrayList<>();
    // these are all the contexts that we need to check and potentially notify
    List<Pair<UUID, UserGameContext>> contexts =
      this.getSameContextListeners(
          heartBeatClient,
          false,
          EventDispatchMode.BOTH_WAYS,
          true
        );

    contexts.forEach(
      context ->
        context
          .getValue()
          .checkConnection()
          .ifPresent(
            connectionStatus -> disconnectedClients.add(context.getKey())
          )
    );

    this.notifyDisconnectionsSameContext(disconnectedClients, contexts);
  }

  public void heartBeat(UUID connectionID) throws PlayerNotFoundException {
    UserGameContext userGameContext = this.getUserContext(connectionID);
    // TODO: for now we are checking the heartbeat of the players only when a
    //  player
    // sends a message as we don't care about the connection status of players
    // that are not in the same context of other players
    // should we change this to check the heartbeat of all players every x
    // seconds? 🤷‍♂️ ( the actual check is done by `this
    // .checkClientConnections(connectionID);` )
    // there is another method that check all the clients `this
    // .checkAllConnections();`

    // the heartBeat function returns true if the connection has been restored
    // it also updates the last heartbeat value

    boolean wasDisconnected =
      userGameContext.getConnectionStatus() ==
      UserGameContext.ConnectionStatus.DISCONNECTED;
    if (userGameContext.heartBeat()) {
      boolean removeGameID = true;

      if (wasDisconnected && userGameContext.getGameId().isPresent()) {
        String gameID = userGameContext.getGameId().get();
        Game game = null;
        try {
          game = getGame(gameID);
        } catch (GameNotFoundException e) {
          // the game has been removed, removeGameID remains true
        }

        if (
          userGameContext.getStatus() == UserGameContextStatus.IN_GAME &&
          game != null &&
          userGameContext.getNickname().isPresent()
        ) {
          try {
            Boolean wasGameHalted = game.isGameHalted();
            Player player = game.getPlayer(userGameContext.getNickname().get());
            game.playerReconnected(userGameContext.getNickname().get());
            userGameContext
              .getListener()
              .userContext(
                new FullUserGameContext(
                  userGameContext.getGameId().get(),
                  userGameContext.getNickname().get(),
                  player.getToken(),
                  generateGameInfo(connectionID, gameID, game)
                )
              );
            removeGameID = false;
            if (wasGameHalted && !game.isGameHalted()) notifySameContextClients(
              connectionID,
              (listener, targetConnectionID) -> listener.gameResumed(gameID),
              false
            );
          } catch (PlayerNotFoundGameException ignored) {
            userGameContext.removeGameId();
          } catch (RemoteException e) {
            userGameContext.disconnected();
            removeGameID = false;
            try {
              game.playerDisconnected(userGameContext.getNickname().get());
            } catch (PlayerNotFoundGameException ex) {
              userGameContext.removeGameId();
            }
          }
        }
      } else {
        removeGameID = false;
      }

      // if we land here it means that the player is not connected to the game so having a gameID is wrong.
      // NOTE: remember that when a lobby player is disconnected the player gets removed from the lobby
      if (removeGameID) {
        userGameContext.removeGameId();
      }

      this.notifySameContextClients(
          connectionID,
          (listener, targetConnectionID) ->
            listener.playerConnectionChanged(
              connectionID,
              userContexts.get(connectionID).getNickname().orElse(null),
              UserGameContext.ConnectionStatus.CONNECTED
            )
        );
    }
    this.checkClientConnections(connectionID);
  }

  /**
   * Retrieves a list of listeners that share the same context as the specified connection ID.
   *
   * @param connectionID the UUID of the connection to match contexts with
   * @param includeSelf a Boolean indicating whether to include the client that caused the event in the results
   * @param mode the EventDispatchMode that determines if the event should be dispatched based on context status
   * @return a list of pairs containing the UUID of the connections and their respective UserGameContext
   */
  private List<Pair<UUID, UserGameContext>> getSameContextListeners(
    UUID connectionID,
    Boolean includeSelf,
    EventDispatchMode mode
  ) {
    return getSameContextListeners(connectionID, includeSelf, mode, false);
  }

  /**
   * Retrieves a list of listeners that share the same context as the specified connection ID,
   * with an option to include clients that are losing connection.
   *
   * @param connectionID the UUID of the connection to match contexts with
   * @param includeSelf a Boolean indicating whether to include the client that caused the event in the results
   * @param mode the EventDispatchMode that determines if the event should be dispatched based on context status
   * @param includeLosing a Boolean indicating whether to include clients that are losing connection
   * @return a list of pairs containing the UUID of the connections and their respective UserGameContext
   */
  private List<Pair<UUID, UserGameContext>> getSameContextListeners(
    UUID connectionID,
    Boolean includeSelf,
    EventDispatchMode mode,
    Boolean includeLosing
  ) {
    // the socket Ids that dispatched the event
    if (!userContexts.containsKey(connectionID)) return new ArrayList<>();
    UserGameContext targetContext = userContexts.get(connectionID);
    return userContexts
      .keySet()
      .stream()
      .filter(sID ->
        userContexts.get(sID).getListener() != null &&
        // if includeSelf we also include the client that caused the event
        // otherwise we don't
        (includeSelf || connectionID != sID) &&
        // if lobbyGameSharing is true we send the events to both the lobby & the game listeners
        // otherwise we will only send the events to the listeners in the same context
        // ( e.g. lobbyGameSharing is true and the client that caused the event is in the lobby
        // we will send the event to the listeners in the lobby and the game listeners )
        mode.checkDispatchable(
          targetContext.getStatus(),
          userContexts.get(sID).getStatus()
        ) &&
        // here we check that the clients are in the same game or in the
        // same lobby or both in the menu
        ((targetContext.getGameId().isEmpty() &&
            userContexts.get(sID).getGameId().isEmpty()) ||
          targetContext
            .getGameId()
            .equals(userContexts.get(sID).getGameId())) &&
        // we filter out the clients that have disconnected or are having
        // connection problems
        // as those events will fail to be dispatched
        (userContexts.get(sID).getConnectionStatus() ==
            UserGameContext.ConnectionStatus.CONNECTED ||
          (includeLosing &&
            userContexts.get(sID).getConnectionStatus() ==
              UserGameContext.ConnectionStatus.LOSING)))
      .map(sID -> new Pair<>(sID, userContexts.get(sID)))
      .collect(Collectors.toList());
  }

  public void removePlayerFromLobby(Game game, UUID connectionID)
    throws InvalidActionException {
    Lobby lobby = game.getLobby();
    Pair<CardPair<ObjectiveCard>, PlayableCard> oldPlayerCards;
    try {
      oldPlayerCards = lobby.removePlayer(connectionID);
    } catch (PlayerNotFoundGameException e) {
      throw new PlayerNotFoundException(connectionID);
    }
    game.insertObjectiveCard(oldPlayerCards.getKey().getFirst());
    game.insertObjectiveCard(oldPlayerCards.getKey().getSecond());
    PlayableCard starterCard = oldPlayerCards.getValue();
    starterCard.clearPlayedSide();
    game.insertStarterCard(starterCard);
    userContexts.get(connectionID).removeGameId();
  }

  public void quitFromLobby(UUID connectionID) throws InvalidActionException {
    UserGameContext userContext = getUserContext(connectionID);
    String gameId = userContext
      .getGameId()
      .orElseThrow(NotInGameException::new);

    Game game = this.getGame(gameId);
    this.removePlayerFromLobby(game, connectionID);

    userContext.removeGameId();

    notifyClients(
      userContexts
        .entrySet()
        .stream()
        .filter(
          entry ->
            entry.getValue().getStatus().equals(UserGameContextStatus.MENU) ||
            entry
              .getValue()
              .getGameId()
              .map(g -> g.equals(gameId))
              .orElse(false)
        )
        .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList()),
      ((listener, targetConnectionID) ->
          listener.playerLeftLobby(gameId, connectionID))
    );
  }

  public void joinLobby(UUID connectionID, String gameId)
    throws InvalidActionException {
    Game game = this.getGame(gameId);

    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();
    if (lobby.containsConnectionID(connectionID)) {
      this.removePlayerFromLobby(game, connectionID);
    }
    try {
      lobby.addPlayer(
        connectionID,
        game.drawObjectiveCardPair(),
        game.drawStarterCard()
      );
    } catch (LobbyFullException.LobbyFullInternalException e) {
      throw new LobbyFullException(gameId);
    }
    if (userContexts.containsKey(connectionID)) {
      userContexts.get(connectionID).setLobbyGameId(gameId);
    } else {
      userContexts.put(connectionID, new UserGameContext(gameId));
    }

    notifyClients(
      userContexts
        .entrySet()
        .stream()
        .filter(
          user ->
            user.getKey().equals(connectionID) &&
            user.getValue().getListener() != null
        )
        .map(user -> new Pair<>(user.getKey(), user.getValue()))
        .collect(Collectors.toList()),
      ((listener, targetConnectionID) ->
          listener.lobbyInfo(generateLobbyInfo(gameId, game)))
    );

    notifyClients(
      userContexts
        .entrySet()
        .stream()
        .filter(
          entry ->
            entry.getValue().getStatus().equals(UserGameContextStatus.MENU) ||
            entry
              .getValue()
              .getGameId()
              .map(g -> g.equals(gameId))
              .orElse(false)
        )
        .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList()),
      ((listener, targetConnectionID) -> {
          listener.playerJoinedLobby(gameId, connectionID);
        })
    );
  }

  public void lobbySetTokenColor(UUID connectionID, TokenColor color)
    throws InvalidActionException {
    UserGameContext userGameContext = this.getUserContext(connectionID);
    if (color == null) throw new InvalidTokenColorException();
    String gameID = userGameContext
      .getGameId()
      .orElseThrow(NotInGameException::new);

    Game game = this.getGame(gameID);
    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();
    try {
      lobby.setToken(connectionID, color);
    } catch (PlayerNotFoundGameException e) {
      throw new PlayerNotFoundException(connectionID);
    }
    this.notifySameContextClients(
        connectionID,
        (listener, targetConnectionID) ->
          listener.playerSetToken(
            gameID,
            connectionID,
            userGameContext.getNickname().orElse(null),
            color
          )
      );
  }

  public void lobbySetNickname(UUID connectionID, String nickname)
    throws InvalidActionException {
    UserGameContext userGameContext = this.getUserContext(connectionID);
    String gameId = userGameContext
      .getGameId()
      .orElseThrow(NotInGameException::new);
    Game game = this.getGame(gameId);
    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();
    try {
      lobby.setNickname(connectionID, nickname);
    } catch (PlayerNotFoundGameException e) {
      throw new PlayerNotFoundException(connectionID);
    }
    userGameContext.setNickname(nickname);
    this.notifySameContextClients(
        connectionID,
        (listener, targetConnectionID) ->
          listener.playerSetNickname(gameId, connectionID, nickname)
      );
  }

  public void lobbyChooseObjective(UUID connectionID, Boolean first)
    throws InvalidActionException {
    String gameId =
      this.getUserContext(connectionID)
        .getGameId()
        .orElseThrow(NotInGameException::new);
    Game game = this.getGame(gameId);

    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();

    try {
      lobby.setObjectiveCard(connectionID, first);
      String playerNickname = lobby
        .getPlayerNickname(connectionID)
        .orElse(null);
      this.notifySameContextClients(
          connectionID,
          (listener, targetConnectionID) ->
            listener.playerChoseObjectiveCard(
              gameId,
              connectionID,
              playerNickname
            )
        );
    } catch (PlayerNotFoundGameException e) {
      throw new PlayerNotFoundException(connectionID);
    }
  }

  private void sendGameStartedNotification(String gameId, Game game) {
    this.notifyClients(
        userContexts
          .entrySet()
          .stream()
          .filter(
            entry ->
              entry.getValue().getListener() != null &&
              entry.getValue().getConnectionStatus() ==
                UserGameContext.ConnectionStatus.CONNECTED &&
              entry
                .getValue()
                .getGameId()
                .map(gid -> gid.equals(gameId))
                .orElse(false)
          )
          .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
          .collect(Collectors.toList()),
        (listener, targetSocketID) ->
          listener.gameStarted(
            gameId,
            generateGameInfo(targetSocketID, gameId, game)
          )
      );
  }

  public void startGame(UUID connectionID) throws InvalidActionException {
    String gameId = userContexts
      .get(connectionID)
      .getGameId()
      .orElseThrow(NotInGameException::new);

    Game game = this.getGame(gameId);
    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    game.start();

    this.sendGameStartedNotification(gameId, game);
  }

  public void joinGame(UUID connectionID, String gameId, CardSideType sideType)
    throws InvalidActionException {
    Game game = this.getGame(gameId);
    try {
      final Player newPlayer = game
        .getLobby()
        .finalizePlayer(connectionID, sideType, game.drawHand());
      game.addPlayer(newPlayer);
      userContexts.get(connectionID).setGameId(gameId, newPlayer.getNickname());
      this.notifySameContextClients(
          connectionID,
          (listener, targetConnectionID) ->
            listener.playerJoinedGame(
              gameId,
              connectionID,
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
            ),
          EventDispatchMode.BOTH_WAYS
        );
      try {
        game.start();
        this.sendGameStartedNotification(gameId, game);
      } catch (GameAlreadyStartedException ignored) {
        // the game has already started
        // we don't need to do anything
      } catch (GameNotReadyException ignored) {
        // the game is not ready to start
        // we don't need to do anything
      }
    } catch (IncompletePlayerBuilderException e) {
      throw new IncompleteLobbyPlayerException(e);
    } catch (PlayerNotFoundGameException e) {
      throw new PlayerNotFoundException(connectionID);
    }
  }

  public void createGame(UUID connectionID, String gameId, Integer players)
    throws InvalidActionException {
    manager.createGame(gameId, players);

    this.notifySameContextClients(
        connectionID,
        (listener, targetConnectionID) ->
          listener.gameCreated(gameId, 0, players)
      );
  }

  public Boolean isLastRound(String gameId) throws GameNotFoundException {
    return this.getGame(gameId).isLastRound();
  }

  public void deleteGame(UUID connectionID, String gameId)
    throws InvalidActionException {
    if (
      userContexts
        .values()
        .stream()
        .anyMatch(
          uc ->
            uc.getGameId().map(gid -> gid.equals(gameId)).orElse(false) &&
            uc.getConnectionStatus() ==
              UserGameContext.ConnectionStatus.CONNECTED
        )
    ) {
      if (
        !userContexts.containsKey(connectionID) ||
        !userContexts
          .get(connectionID)
          .getGameId()
          .map(gid -> gid.equals(gameId))
          .orElse(false)
      ) {
        throw new NotInGameException();
      }
    }
    manager.deleteGame(gameId);
    // TODO remove also from userContexts?

    this.notifySameContextClients(
        connectionID,
        (listener, targetConnectionID) -> listener.gameDeleted(gameId)
      );
  }

  private void checkIfCurrentPlayer(Game game, UUID connectionID)
    throws InvalidActionException {
    Player player = game.getCurrentPlayer();
    if (
      userContexts.containsKey(connectionID) &&
      player
        .getNickname()
        .equals(
          userContexts
            .get(connectionID)
            .getNickname()
            .orElseThrow(PlayerNotActive::new)
        )
    ) return;
    throw new PlayerNotActive();
  }

  private void checkEnoughConnectedPlayers(String gameID, Game game)
    throws InvalidActionException {
    Set<String> inactivePlayers = userContexts
      .values()
      .stream()
      .filter(
        userGameContext ->
          userGameContext
            .getGameId()
            .map(gid -> gid.equals(gameID))
            .orElse(false) &&
          userGameContext.getConnectionStatus() !=
            UserGameContext.ConnectionStatus.CONNECTED
      )
      .map(userGameContext -> userGameContext.getNickname().orElse(null))
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());

    if (game.getPlayersCount() - inactivePlayers.size() < 2) {
      throw new NotEnoughPlayersConnectedException();
    }
  }

  public void nextTurn(UUID connectionID) throws InvalidActionException {
    UserGameContext userContext = getUserContext(connectionID);

    String gameId = userContext
      .getGameId()
      .orElseThrow(NotInGameException::new);

    Game game = this.getGame(gameId);
    this.checkEnoughConnectedPlayers(gameId, game);
    this.checkIfCurrentPlayer(game, connectionID);

    try {
      game.nextTurn(
        () -> this.nextTurnEvent(connectionID, gameId, game),
        remainingRounds ->
          this.notifySameContextClients(
              connectionID,
              (listener, targetConnectionID) ->
                listener.remainingRounds(gameId, remainingRounds)
            )
      );
    } catch (GameOverException e) {
      evaluateObjectives(game, connectionID);
      throw e;
    }
  }

  private void evaluateObjectives(Game game, UUID connectionID) {
    game
      .getPlayers()
      .forEach(player -> {
        player.evaluateSecretObjective();
        player.evaluate(game.getGameBoard().getObjectiveCards().getFirst());
        player.evaluate(game.getGameBoard().getObjectiveCards().getSecond());
      });

    this.notifySameContextClients(
        connectionID,
        (listener, targetConnectionID) ->
          listener.playerScoresUpdate(game.getScoreBoard())
      );
  }

  private void nextTurnEvent(UUID connectionID, String gameID, Game game) {
    this.notifySameContextClients(
        connectionID,
        (listener, targetConnectionID) -> {
          try {
            listener.changeTurn(
              gameID,
              getLastPlayerNickname(gameID),
              game.getCurrentPlayerIndex(),
              game.isLastRound(),
              game.getCurrentPlayer().getBoard().getAvailableSpots(),
              game.getCurrentPlayer().getBoard().getForbiddenSpots(),
              game.getGameBoard().peekResourceCardFromDeck().getId(),
              game.getGameBoard().peekGoldCardFromDeck().getId()
            );
          } catch (GameNotFoundException e) {
            throw new RuntimeException(e);
          }
        }
      );
  }

  public void nextTurn(
    UUID connectionID,
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  ) throws InvalidActionException {
    if (deckType == null || drawingSource == null) {
      this.nextTurn(connectionID);
      return;
    }

    String gameId = userContexts
      .get(connectionID)
      .getGameId()
      .orElseThrow(NotInGameException::new);

    Game game = this.getGame(gameId);
    this.checkEnoughConnectedPlayers(gameId, game);
    this.checkIfCurrentPlayer(game, connectionID);

    // If the player has not placed a card yet, throw an exception
    game
      .getPlayers()
      .stream()
      .filter(
        player ->
          player.getConnectionID().equals(connectionID) &&
          player.getCardPlaced()
      )
      .findFirst()
      .orElseThrow(CardNotPlacedException::new);
    try {
      game.nextTurn(
        drawingSource,
        deckType,
        (playerCardId, pairCardId) ->
          this.notifySameContextClients(
              connectionID,
              (listener, targetConnectionID) -> {
                try {
                  listener.changeTurn(
                    gameId,
                    getLastPlayerNickname(gameId),
                    game.getCurrentPlayerIndex(),
                    game.isLastRound(),
                    drawingSource,
                    deckType,
                    targetConnectionID.equals(connectionID)
                      ? playerCardId
                      : null,
                    pairCardId,
                    game.getCurrentPlayer().getBoard().getAvailableSpots(),
                    game.getCurrentPlayer().getBoard().getForbiddenSpots(),
                    game.getGameBoard().peekResourceCardFromDeck().getId(),
                    game.getGameBoard().peekGoldCardFromDeck().getId()
                  );
                } catch (GameNotFoundException e) {
                  throw new RuntimeException(e);
                }
              }
            ),
        () -> this.nextTurnEvent(connectionID, gameId, game),
        remainingRounds ->
          this.notifySameContextClients(
              connectionID,
              (listener, targetConnectionID) ->
                listener.remainingRounds(gameId, remainingRounds)
            )
      );
    } catch (GameOverException e) {
      evaluateObjectives(game, connectionID);
      throw e;
    }
  }

  private String getLastPlayerNickname(String gameId)
    throws GameNotFoundException {
    Game game = this.getGame(gameId);
    Set<String> connectedNicknames = userContexts
      .values()
      .stream()
      .filter(
        userGameContext ->
          userGameContext.connectionStatus.equals(
            UserGameContext.ConnectionStatus.CONNECTED
          )
      )
      .map(userGameContext -> userGameContext.getNickname().get())
      .collect(Collectors.toSet());
    String lastNickname;
    int i = 0;
    do {
      i++;
      lastNickname = game
        .getPlayers()
        .get(
          (game.getCurrentPlayerIndex() - i + game.getPlayers().size()) %
          game.getPlayers().size()
        )
        .getNickname();
    } while (!connectedNicknames.contains(lastNickname));
    return lastNickname;
  }

  private LobbyUsersInfo generateLobbyInfo(String gameID, Game game) {
    return new LobbyUsersInfo(userContexts, gameID, game);
  }

  private GameInfo generateGameInfo(
    UUID targetSocketID,
    String gameID,
    Game game
  ) {
    List<GameInfo.GameInfoUser> users = new ArrayList<>();

    userContexts
      .entrySet()
      .stream()
      .filter(
        entry ->
          entry
            .getValue()
            .getGameId()
            .map(gid -> gid.equals(gameID))
            .orElse(false)
      )
      .forEach(entry -> {
        try {
          String nickname = entry.getValue().getNickname().orElse(null);
          Player player = game.getPlayer(nickname);
          PlayerBoard playerBoard = player.getBoard();
          users.add(
            new GameInfo.GameInfoUser(
              entry.getValue().getNickname().orElse(null),
              player.getToken(),
              entry.getKey(),
              entry.getValue().getConnectionStatus(),
              playerBoard
                .getPlayedCards()
                .entrySet()
                .stream()
                .collect(
                  Collectors.toMap(
                    Map.Entry::getKey,
                    entry2 ->
                      new Pair<>(
                        entry2.getValue().getId(),
                        entry2.getValue().getPlayedSideType().orElseThrow()
                      )
                  )
                ),
              playerBoard
                .getHand()
                .stream()
                .map(PlayableCard::getId)
                .collect(Collectors.toList()),
              game.getScoreBoard().get(nickname),
              entry.getKey().equals(targetSocketID)
                ? playerBoard.getObjectiveCard().getId()
                : null,
              playerBoard.getAvailableSpots(),
              playerBoard.getForbiddenSpots(),
              game.getPlayers().indexOf(player),
              playerBoard.getResources(),
              playerBoard.getObjects()
            )
          );
        } catch (PlayerNotFoundGameException e) {
          userContexts.put(
            entry.getKey(),
            new UserGameContext(entry.getValue().getListener())
          );
        }
      });

    return new GameInfo(
      gameID,
      users
        .stream()
        .sorted(Comparator.comparingInt(GameInfo.GameInfoUser::getIndex))
        .toList(),
      game.getCurrentPlayerIndex(),
      game.getRemainingRounds().orElse(null),
      game.getObjectiveCards(),
      game.getResourceCards(),
      game.getGoldCards(),
      game.getGameBoard().peekResourceCardFromDeck().getId(),
      game.getGameBoard().peekGoldCardFromDeck().getId()
    );
  }

  public void connect(UUID connectionID, RemoteGameEventListener listener) {
    if (!userContexts.containsKey(connectionID)) {
      userContexts.put(connectionID, new UserGameContext(listener));
    } else {
      UserGameContext userGameContext = userContexts.get(connectionID);
      userGameContext.setListener(listener);

      if (userGameContext.getStatus() == UserGameContextStatus.IN_GAME) {
        try {
          if (userGameContext.getGameId().isPresent()) {
            String gameId = userGameContext.getGameId().get();
            Game game = this.getGame(gameId);
            if (userGameContext.getNickname().isPresent()) {
              String nickname = userGameContext.getNickname().get();
              Player player = game.getPlayer(nickname);
              notifyClients(
                List.of(new Pair<>(connectionID, userGameContext)),
                (l, targetSocketID) ->
                  l.userContext(
                    new FullUserGameContext(
                      gameId,
                      player.getNickname(),
                      player.getToken(),
                      generateGameInfo(targetSocketID, gameId, game)
                    )
                  )
              );
              return;
            }
          }
        } catch (GameNotFoundException | PlayerNotFoundGameException ignored) {}
        userGameContext.removeGameId();
      } else {
        notifyClients(
          List.of(new Pair<>(connectionID, userGameContext)),
          (l, targetConnectionID) -> l.userContext(new FullUserGameContext())
        );
      }
    }
  }

  public void placeCard(
    UUID connectionID,
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  ) throws InvalidActionException {
    UserGameContext userContext = getUserContext(connectionID);

    String gameId = userContext
      .getGameId()
      .orElseThrow(NotInGameException::new);

    Game game = this.getGame(gameId);
    this.checkEnoughConnectedPlayers(gameId, game);
    this.checkIfCurrentPlayer(game, connectionID);
    Player currentPlayer = game.getCurrentPlayer();
    try {
      PlayableCard playedCard = currentPlayer.placeCard(
        playerHandCardNumber,
        side,
        position
      );
      this.notifySameContextClients(
          connectionID,
          (listener, targetConnectionID) ->
            listener.cardPlaced(
              gameId,
              game.getCurrentPlayer().getNickname(),
              playerHandCardNumber,
              playedCard.getId(),
              side,
              position,
              currentPlayer.getPoints(),
              currentPlayer.getBoard().getResources(),
              currentPlayer.getBoard().getObjects(),
              currentPlayer.getBoard().getAvailableSpots(),
              currentPlayer.getBoard().getForbiddenSpots()
            )
        );
    } catch (AlreadyPlacedCardGameException e) {
      // we throw another exception because we want to keep the model (game)
      // exceptions separate from
      // the controller exceptions.
      throw new AlreadyPlacedCardException();
    }
  }

  public Set<TokenColor> getAvailableTokens(String gameId)
    throws InvalidActionException {
    Game game = this.getGame(gameId);
    return game.getLobby().getAvailableColors();
  }

  public Map<String, Integer> getGamesCurrentPlayers() {
    Map<String, Integer> playerCounts = new HashMap<>();
    for (String gameId : this.getGames()) {
      try {
        playerCounts.put(gameId, this.getGame(gameId).getPlayers().size());
      } catch (GameNotFoundException e) {
        playerCounts.put(gameId, 0);
      }
    }
    return playerCounts;
  }

  public Map<String, Integer> getGamesMaxPlayers() {
    Map<String, Integer> maxPlayers = new HashMap<>();
    for (String gameId : this.getGames()) {
      try {
        maxPlayers.put(gameId, this.getGame(gameId).getMaxPlayers());
      } catch (GameNotFoundException e) {
        maxPlayers.put(gameId, 0);
      }
    }
    return maxPlayers;
  }

  public Pair<Integer, Integer> getLobbyObjectiveCards(UUID connectionID)
    throws InvalidActionException {
    String gameId = userContexts
      .get(connectionID)
      .getGameId()
      .orElseThrow(NotInGameException::new);
    this.getGame(gameId);
    Game game = manager
      .getGame(gameId)
      .orElseThrow(() -> new GameNotFoundException(gameId));
    return game
      .getLobby()
      .getPlayerObjectiveCards(connectionID)
      .map(p -> new Pair<>(p.getFirst().getId(), p.getSecond().getId()))
      .orElseThrow(InvalidGetObjectiveCardsCallException::new);
  }

  public Integer getLobbyStarterCard(UUID connectionID)
    throws InvalidActionException {
    try {
      return this.getGame(
          userContexts
            .get(connectionID)
            .getGameId()
            .orElseThrow(NotInGameException::new)
        )
        .getLobby()
        .getStarterCard(connectionID)
        .getId();
    } catch (PlayerNotFoundGameException e) {
      throw new PlayerNotFoundException(connectionID);
    }
  }

  public void sendChatMessage(UUID connectionID, ChatMessage chatMessage)
    throws InvalidActionException {
    UserGameContext userContext = this.getUserContext(connectionID);

    String gameId = userContext
      .getGameId()
      .orElseThrow(NotInGameException::new);

    Game game = this.getGame(gameId);

    game.getChat().postMessage(chatMessage);

    notifySameContextClients(connectionID, (listener, targetConnectionID) -> {
      //if there is a recipient in the message filter the listeners

      try {
        if (
          chatMessage
            .getRecipient()
            .map(
              recipient ->
                recipient.equals(
                  userContexts
                    .get(targetConnectionID)
                    .getNickname()
                    .orElse(null)
                )
            )
            .orElse(true) &&
          !userContexts
            .get(targetConnectionID)
            .getNickname()
            .map(nickname -> nickname.equals(chatMessage.getSender()))
            .orElse(false)
        ) {
          listener.chatMessage(gameId, chatMessage);
        }
      } catch (RemoteException e) {
        if (userContexts.containsKey(targetConnectionID)) {
          userContexts.get(targetConnectionID).disconnected();
        }
      }
    });
  }
}
