package polimi.ingsw.am21.codex.controller;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.exceptions.*;
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

  public enum UserGameContextStatus {
    MENU,
    IN_LOBBY,
    IN_GAME,
  }

  public static class UserGameContext {

    public enum ConnectionStatus {
      CONNECTED,
      LOSING,
      DISCONNECTED,
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

  public enum EventDispatchMode {
    // EventDispatchMode is an enum used to describe how the events should be dispatched
    // under different circumstances. for each mode we provide a description of the logic structured this way:
    // EVENT_NAME
    // context of the client that caused the event:
    // - listeners that will receive the event
    TOP_DOWN,
    // menu:
    // - menu listeners
    // lobby:
    // - lobby listeners
    // game:
    // - game listeners
    // - lobby listeners
    BOTTOM_UP,
    // menu:
    // - menu listeners
    // lobby:
    // - lobby listeners
    // - game listeners
    // game:
    // - game listeners
    TOP_DOWN_FULL,
    // menu:
    // - menu listeners
    // lobby:
    // - lobby listeners
    // - menu listeners
    // game:
    // - game listeners
    // - menu listeners
    // - lobby listeners
    BOTTOM_UP_FULL,
    // menu:
    // - menu listeners
    // - lobby listeners
    // - game listeners
    // lobby:
    // - lobby listeners
    // - game listeners
    // game:
    // - game listeners
    BOTH_WAYS,
    // menu:
    // - menu listeners
    // lobby:
    // - lobby listeners
    // - game listeners
    // game:
    // - game listeners
    // - lobby listeners
    BOTH_WAYS_FULL,

    // menu:
    // - menu listeners
    // - game listeners
    // - lobby listeners
    // lobby:
    // - lobby listeners
    // - game listeners
    // - menu listeners
    // game:
    // - game listeners
    // - lobby listeners
    // - menu listeners
    SAME_CONTEXT;

    // menu:
    // - menu listeners
    // lobby:
    // - lobby listeners
    // game:
    // - game listeners

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

  GameManager manager;

  Map<UUID, UserGameContext> userContexts = new HashMap<>();

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
    return manager
      .getGame(gameId)
      .orElseThrow(() -> new GameNotFoundException(gameId));
  }

  /**
   * @param disconnectedClients list of clients that have disconnected
   */
  public void notifyDisconnections(List<UUID> disconnectedClients) {
    List<Pair<UUID, UUID>> listenersToNotify = new ArrayList<>();

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

  private UserGameContext getUserContext(UUID connectionID)
    throws InvalidActionException {
    if (
      !userContexts.containsKey(connectionID)
    ) throw new PlayerNotFoundException(connectionID);
    return userContexts.get(connectionID);
  }

  public void notifyDisconnectionsSameContext(
    List<UUID> disconnectedClients,
    List<Pair<UUID, UserGameContext>> sameContextClients
  ) {
    List<Pair<UUID, UUID>> listenersToNotify = new ArrayList<>();

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

    while (!listenersToNotify.isEmpty()) {
      Pair<UUID, UUID> toNotify = listenersToNotify.remove(0);
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
    UUID socketID,
    RemoteListenerFunction function,
    EventDispatchMode mode
  ) {
    this.notifyClients(
        this.getSameContextListeners(socketID, true, mode),
        function,
        true
      );
  }

  private void notifySameContextClients(
    UUID socketID,
    RemoteListenerFunction function
  ) {
    this.notifySameContextClients(
        socketID,
        function,
        EventDispatchMode.BOTH_WAYS
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
          EventDispatchMode.BOTH_WAYS
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

  public void heartBeat(UUID socketID) {
    // TODO: for now we are checking the heartbeat of the players only when a
    //  player
    // sends a message as we don't care about the connection status of players
    // that are not in the same context of other players
    // should we change this to check the heartbeat of all players every x
    // seconds? 🤷‍♂️ ( the actual check is done by `this.checkClientConnections(socketID);` )
    // there is another method that check all the clients `this.checkAllConnections();`

    // the heartBeat function returns true if the connection has been restored
    // it also updates the last heartbeat value

    if (
      userContexts.containsKey(socketID) &&
      userContexts.get(socketID).heartBeat()
    ) {
      this.notifySameContextClients(
          socketID,
          (listener, targetSocketID) ->
            listener.playerConnectionChanged(
              socketID,
              userContexts.get(socketID).getNickname().orElse(null),
              UserGameContext.ConnectionStatus.CONNECTED
            )
        );
    }
    this.checkClientConnections(socketID);
  }

  private List<Pair<UUID, UserGameContext>> getSameContextListeners(
    UUID socketID,
    Boolean includeSelf,
    EventDispatchMode mode
  ) {
    // the socket Ids that dispatched the event
    if (!userContexts.containsKey(socketID)) return new ArrayList<>();
    UserGameContext targetContext = userContexts.get(socketID);
    return userContexts
      .keySet()
      .stream()
      .filter(sID ->
        userContexts.get(sID).getListener() != null &&
        // if includeSelf we also include the client that caused the event
        // otherwise we don't
        (includeSelf || socketID != sID) &&
        // if lobbyGameSharing is true we send the events to both the lobby & the game listeners
        // otherwise we will only send the events to the listeners in the same context
        // ( e.g. lobbyGameSharing is true and the client that caused the event is in the lobby
        // we will send the event to the listeners in the lobby and the game listeners )
        mode.checkDispatchable(
          targetContext.getStatus(),
          userContexts.get(sID).getStatus()
        ) &&
        // here we check that the clients are in the same game or in the same lobby or both in the menu
        ((targetContext.getGameId().isEmpty() &&
            userContexts.get(sID).getGameId().isEmpty()) ||
          targetContext
            .getGameId()
            .equals(userContexts.get(sID).getGameId())) &&
        // we filter out the clients that have disconnected or are having connection problems
          // as those events will fail to be dispatched
          targetContext.getConnectionStatus() ==
          UserGameContext.ConnectionStatus.CONNECTED)
      .map(sID -> new Pair<>(sID, userContexts.get(sID)))
      .collect(Collectors.toList());
  }

  public void removePlayerFromLobby(Game game, UUID socketID)
    throws InvalidActionException {
    Lobby lobby = game.getLobby();
    Pair<CardPair<ObjectiveCard>, PlayableCard> oldPlayerCards = null;
    try {
      oldPlayerCards = lobby.removePlayer(socketID);
    } catch (PlayerNotFoundGameException e) {
      throw new PlayerNotFoundException(socketID);
    }
    game.insertObjectiveCard(oldPlayerCards.getKey().getFirst());
    game.insertObjectiveCard(oldPlayerCards.getKey().getSecond());
    PlayableCard starterCard = oldPlayerCards.getValue();
    starterCard.clearPlayedSide();
    game.insertStarterCard(starterCard);
    userContexts.get(socketID).removeGameId();
  }

  public void quitFromLobby(UUID socketID) throws InvalidActionException {
    String gameId = userContexts
      .get(socketID)
      .getGameId()
      .orElseThrow(NotInGameException::new);

    Game game = this.getGame(gameId);
    this.removePlayerFromLobby(game, socketID);
  }

  public void joinLobby(UUID socketID, String gameId)
    throws InvalidActionException {
    Game game = this.getGame(gameId);

    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();
    if (lobby.containsSocketID(socketID)) {
      this.removePlayerFromLobby(game, socketID);
    }
    try {
      lobby.addPlayer(
        socketID,
        game.drawObjectiveCardPair(),
        game.drawStarterCard()
      );
    } catch (LobbyFullException.LobbyFullInternalException e) {
      throw new LobbyFullException(gameId);
    }
    if (userContexts.containsKey(socketID)) {
      userContexts.get(socketID).setLobbyGameId(gameId);
    } else {
      userContexts.put(socketID, new UserGameContext(gameId));
    }

    this.notifySameContextClients(
        socketID,
        (listener, targetSocketID) ->
          listener.playerJoinedLobby(gameId, socketID)
      );

    //    if (userContexts.get(socketID).getListener() != null) {
    this.notifySameContextClients(
        socketID,
        (listener, targetSocketID) ->
          listener.lobbyInfo(new LobbyUsersInfo(userContexts, gameId, game))
      );
    //      try {
    //        userContexts
    //          .get(socketID)
    //          .getListener()
    //          .lobbyInfo(new LobbyUsersInfo(userContexts, gameId, game));
    //      } catch (RemoteException e) {
    //        this.notifyDisconnections(List.of(socketID));
    //      }
    //    }
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
        (listener, targetSocketID) ->
          listener.playerSetToken(
            gameID,
            connectionID,
            userGameContext.getNickname().orElse(null),
            color
          )
      );
  }

  public void lobbySetNickname(UUID socketID, String nickname)
    throws InvalidActionException {
    UserGameContext userGameContext = this.getUserContext(socketID);
    String gameId = userGameContext
      .getGameId()
      .orElseThrow(NotInGameException::new);
    Game game = this.getGame(gameId);
    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();
    try {
      lobby.setNickname(socketID, nickname);
    } catch (PlayerNotFoundGameException e) {
      throw new PlayerNotFoundException(socketID);
    }
    userGameContext.setNickname(nickname);
    this.notifySameContextClients(
        socketID,
        (listener, targetSocketID) ->
          listener.playerSetNickname(gameId, socketID, nickname)
      );
  }

  public void lobbyChooseObjective(UUID socketID, Boolean first)
    throws InvalidActionException {
    String gameId =
      this.getUserContext(socketID)
        .getGameId()
        .orElseThrow(NotInGameException::new);
    Game game = this.getGame(gameId);

    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    Lobby lobby = game.getLobby();

    try {
      lobby.setObjectiveCard(socketID, first);
      String playerNickname = lobby.getPlayerNickname(socketID).orElse(null);
      this.notifySameContextClients(
          socketID,
          (listener, targetSocketID) ->
            listener.playerChoseObjectiveCard(gameId, socketID, playerNickname)
        );
    } catch (PlayerNotFoundGameException e) {
      throw new PlayerNotFoundException(socketID);
    }
  }

  private void sendGameStartedNotification(String gameId, Game game) {
    this.notifyClients(userContexts
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
          .collect(Collectors.toList()), (listener, targetSocketID) -> {
          List<GameInfo.GameInfoUser> users = new ArrayList<>();

          userContexts
            .entrySet()
            .stream()
            .filter(
              entry ->
                entry
                  .getValue()
                  .getGameId()
                  .map(gid -> gid.equals(gameId))
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
                          entry2 -> entry2.getValue().getId()
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
                    game.getPlayers().indexOf(player)
                  )
                );
              } catch (PlayerNotFoundGameException e) {
                userContexts.put(
                  entry.getKey(),
                  new UserGameContext(entry.getValue().getListener())
                );
              }
            });

          GameInfo gameInfo = new GameInfo(
            gameId,
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
          listener.gameStarted(gameId, gameInfo);
        });
  }

  public void startGame(UUID socketID) throws InvalidActionException {
    String gameId = userContexts
      .get(socketID)
      .getGameId()
      .orElseThrow(NotInGameException::new);

    Game game = this.getGame(gameId);
    if (
      game.getState() != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

    game.start();

    this.sendGameStartedNotification(gameId, game);
  }

  public void joinGame(UUID socketID, String gameId, CardSideType sideType)
    throws InvalidActionException {
    Game game = this.getGame(gameId);
    try {
      final Player newPlayer = game
        .getLobby()
        .finalizePlayer(socketID, sideType, game.drawHand());
      game.addPlayer(newPlayer);
      userContexts.get(socketID).setGameId(gameId, newPlayer.getNickname());
      this.notifySameContextClients(
          socketID,
          (listener, targetSocketID) ->
            listener.playerJoinedGame(
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
      throw new PlayerNotFoundException(socketID);
    }
  }

  public void createGame(UUID connectionID, String gameId, Integer players)
    throws InvalidActionException {
    manager.createGame(gameId, players);

    this.notifySameContextClients(
        connectionID,
        (listener, targetSocketID) -> listener.gameCreated(gameId, 0, players)
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

    this.notifySameContextClients(
        connectionID,
        (listener, targetSocketID) -> listener.gameDeleted(gameId)
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

  public void nextTurn(UUID connectionID) throws InvalidActionException {
    String gameId = userContexts
      .get(connectionID)
      .getGameId()
      .orElseThrow(NotInGameException::new);

    Game game = this.getGame(gameId);
    this.checkIfCurrentPlayer(game, connectionID);
    try {
      game.nextTurn(
        () -> this.nextTurnEvent(connectionID, gameId, game),
        remainingRounds ->
          this.notifySameContextClients(
              connectionID,
              (listener, targetSocketID) ->
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
        (listener, targetSocketID) ->
          listener.playerScoresUpdate(game.getScoreBoard())
      );
  }

  private void nextTurnEvent(UUID connectionID, String gameID, Game game) {
    this.notifySameContextClients(
        connectionID,
        (listener, targetSocketID) ->
          listener.changeTurn(
            gameID,
            game.getCurrentPlayer().getNickname(),
            game.getCurrentPlayerIndex(),
            game.isLastRound(),
            game.getCurrentPlayer().getBoard().getAvailableSpots(),
            game.getCurrentPlayer().getBoard().getForbiddenSpots(),
            game.getGameBoard().peekResourceCardFromDeck().getId(),
            game.getGameBoard().peekGoldCardFromDeck().getId()
          )
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
    this.checkIfCurrentPlayer(game, connectionID);

    // If the player has not placed a card yet, throw an exception
    game
      .getPlayers()
      .stream()
      .filter(
        player ->
          player.getSocketId().equals(connectionID) && player.getCardPlaced()
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
              (listener, targetSocketID) ->
                listener.changeTurn(
                  gameId,
                  game.getCurrentPlayer().getNickname(),
                  game.getCurrentPlayerIndex(),
                  game.isLastRound(),
                  drawingSource,
                  deckType,
                  targetSocketID.equals(connectionID) ? playerCardId : null,
                  pairCardId,
                  game.getCurrentPlayer().getBoard().getAvailableSpots(),
                  game.getCurrentPlayer().getBoard().getForbiddenSpots(),
                  game.getGameBoard().peekResourceCardFromDeck().getId(),
                  game.getGameBoard().peekGoldCardFromDeck().getId()
                )
            ),
        () -> this.nextTurnEvent(connectionID, gameId, game),
        remainingRounds ->
          this.notifySameContextClients(
              connectionID,
              (listener, targetSocketID) ->
                listener.remainingRounds(gameId, remainingRounds)
            )
      );
    } catch (GameOverException e) {
      evaluateObjectives(game, connectionID);
      throw e;
    }
  }

  public void connect(UUID socketID, RemoteGameEventListener listener) {
    if (!userContexts.containsKey(socketID)) {
      userContexts.put(socketID, new UserGameContext(listener));
    } else {
      userContexts.get(socketID).setListener(listener);
    }
  }

  public void placeCard(
    UUID connectionID,
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  ) throws InvalidActionException {
    String gameId = userContexts
      .get(connectionID)
      .getGameId()
      .orElseThrow(NotInGameException::new);

    Game game = this.getGame(gameId);
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
          (listener, targetSocketID) ->
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
      // we throw another exception because we want to keep the model (game) exceptions separate from
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

  public Pair<Integer, Integer> getLobbyObjectiveCards(UUID socketID)
    throws InvalidActionException {
    String gameId = userContexts
      .get(socketID)
      .getGameId()
      .orElseThrow(NotInGameException::new);
    this.getGame(gameId);
    Game game = manager
      .getGame(gameId)
      .orElseThrow(() -> new GameNotFoundException(gameId));
    return game
      .getLobby()
      .getPlayerObjectiveCards(socketID)
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
    Optional<UserGameContext> userGameContext = Optional.ofNullable(
      userContexts.get(connectionID)
    );

    if (userGameContext.isEmpty()) throw new PlayerNotFoundException(
      connectionID
    );

    String gameId = userGameContext
      .get()
      .getGameId()
      .orElseThrow(NotInGameException::new);

    Game game = this.getGame(gameId);

    game.getChat().postMessage(chatMessage);

    notifySameContextClients(connectionID, (listener, targetSocketID) -> {
      //if there is a recipient in the message filter the listeners

      try {
        if (
          chatMessage
            .getRecipient()
            .map(
              recipient ->
                recipient.equals(
                  userContexts.get(targetSocketID).getNickname().orElse(null)
                )
            )
            .orElse(true) &&
          !userContexts
            .get(targetSocketID)
            .getNickname()
            .map(nickname -> nickname.equals(chatMessage.getSender()))
            .orElse(false)
        ) {
          listener.chatMessage(gameId, chatMessage);
        }
      } catch (RemoteException e) {
        if (userContexts.containsKey(targetSocketID)) {
          userContexts.get(targetSocketID).disconnected();
        }
      }
    });
  }
}
