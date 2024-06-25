package polimi.ingsw.am21.codex.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javafx.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.controller.exceptions.*;
import polimi.ingsw.am21.codex.controller.listeners.FullUserGameContext;
import polimi.ingsw.am21.codex.controller.listeners.GameInfo;
import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.Game;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;

class GameControllerTest {

  GameController controller;

  @BeforeEach
  void setup() {
    this.controller = new GameController();
  }

  static class DummyRemoteGameEventListener implements RemoteGameEventListener {

    Consumer<String> failFn;

    DummyRemoteGameEventListener(Consumer<String> failFn) {
      super();
      this.failFn = failFn;
    }

    @Override
    public void gameCreated(String gameId, int currentPlayers, int maxPlayers)
      throws RemoteException {}

    @Override
    public void refreshLobbies(
      Set<String> lobbyIds,
      Map<String, Integer> currentPlayers,
      Map<String, Integer> maxPlayers
    ) throws RemoteException {}

    @Override
    public void gameDeleted(String gameId) throws RemoteException {}

    @Override
    public void playerJoinedLobby(String gameId, UUID socketID)
      throws RemoteException {}

    @Override
    public void playerLeftLobby(String gameId, UUID socketID)
      throws RemoteException {}

    @Override
    public void playerSetToken(
      String gameId,
      UUID socketID,
      String nickname,
      TokenColor token
    ) throws RemoteException {}

    @Override
    public void playerSetNickname(
      String gameId,
      UUID socketID,
      String nickname
    ) throws RemoteException {}

    @Override
    public void playerChoseObjectiveCard(
      String gameId,
      UUID socketID,
      String nickname
    ) throws RemoteException {}

    @Override
    public void playerJoinedGame(
      String gameId,
      UUID socketID,
      String nickname,
      TokenColor color,
      List<Integer> handIDs,
      Integer starterCard,
      CardSideType starterSide
    ) throws RemoteException {}

    @Override
    public void gameStarted(String gameId, GameInfo gameInfo)
      throws RemoteException {}

    @Override
    public void changeTurn(
      String gameId,
      String playerNickname,
      Integer playerIndex,
      Boolean isLastRound,
      DrawingCardSource source,
      DrawingDeckType deck,
      Integer cardId,
      Integer newPairCardId,
      Set<Position> availableSpots,
      Set<Position> forbiddenSpots,
      Integer resourceDeckTopCardId,
      Integer goldDeckTopCardId
    ) throws RemoteException {}

    @Override
    public void changeTurn(
      String gameId,
      String playerNickname,
      Integer playerIndex,
      Boolean isLastRound,
      Set<Position> availableSpots,
      Set<Position> forbiddenSpots,
      Integer resourceDeckTopCardId,
      Integer goldDeckTopCardId
    ) throws RemoteException {}

    @Override
    public void cardPlaced(
      String gameId,
      String playerId,
      Integer playerHandCardNumber,
      Integer cardId,
      CardSideType side,
      Position position,
      int newPlayerScore,
      Map<ResourceType, Integer> updatedResources,
      Map<ObjectType, Integer> updatedObjects,
      Set<Position> availableSpots,
      Set<Position> forbiddenSpots
    ) throws RemoteException {}

    @Override
    public void gameOver() throws RemoteException {}

    @Override
    public void playerScoresUpdate(Map<String, Integer> newScores)
      throws RemoteException {}

    @Override
    public void remainingRounds(String gameID, int remainingRounds)
      throws RemoteException {}

    @Override
    public void winningPlayer(String nickname) throws RemoteException {}

    @Override
    public void playerConnectionChanged(
      UUID socketID,
      String nickname,
      GameController.UserGameContext.ConnectionStatus status
    ) throws RemoteException {}

    @Override
    public void lobbyInfo(LobbyUsersInfo usersInfo) throws RemoteException {}

    @Override
    public void chatMessage(String gameID, ChatMessage message)
      throws RemoteException {}

    @Override
    public void getObjectiveCards(Pair<Integer, Integer> objectiveCards)
      throws RemoteException {}

    @Override
    public void getStarterCard(Integer cardId) throws RemoteException {}

    @Override
    public void gameHalted(String gameID) throws RemoteException {}

    @Override
    public void gameResumed(String gameID) throws RemoteException {}

    @Override
    public void userContext(FullUserGameContext context)
      throws RemoteException {}
  }

  private List<UUID> createGame(
    String gameId,
    int maxPlayers,
    int players,
    Function<UUID, DummyRemoteGameEventListener> generateListener
  ) {
    List<UUID> playerIDs = new ArrayList<>();
    UUID connectionID1 = UUID.randomUUID();
    try {
      controller.createGame(connectionID1, gameId, maxPlayers);
    } catch (InvalidActionException e) {
      fail("Error creating the game '" + gameId + "': " + e.getMessage());
    }
    if (players > 0) {
      playerIDs.add(connectionID1);
      try {
        controller.connect(
          connectionID1,
          generateListener.apply(connectionID1)
        );
        controller.joinLobby(connectionID1, gameId);
      } catch (NullPointerException | InvalidActionException e) {
        fail("Error joining the game '" + gameId + "': " + e.getMessage());
      }
    }
    for (int i = 1; i < players; i++) {
      UUID connectionID = UUID.randomUUID();
      playerIDs.add(connectionID);
      try {
        controller.connect(connectionID, generateListener.apply(connectionID));
        controller.joinLobby(connectionID, gameId);
      } catch (InvalidActionException e) {
        fail("Error joining the game '" + gameId + "': " + e.getMessage());
      }
    }
    return playerIDs;
  }

  private List<UUID> createGame(String gameId, int maxPlayers, int players) {
    return createGame(
      gameId,
      maxPlayers,
      players,
      connectionID -> new DummyRemoteGameEventListener(Assertions::fail)
    );
  }

  @Test
  void getGames() {
    assertEquals(0, controller.getGames().size());

    try {
      controller.createGame(UUID.randomUUID(), "test1", 4);
    } catch (InvalidActionException e) {
      fail("Error creating the game 'test1': " + e.getMessage());
    }
    assertEquals(1, controller.getGames().size());

    try {
      controller.createGame(UUID.randomUUID(), "test2", 4);
    } catch (InvalidActionException e) {
      fail("Error creating the game 'test2': " + e.getMessage());
    }
    assertEquals(2, controller.getGames().size());
  }

  @Test
  void getCurrentSlots() {
    String gameID1 = "test1";
    String gameID2 = "test2";
    String gameID3 = "test3";

    createGame(gameID1, 4, 0);
    assertEquals(0, controller.getCurrentSlots().get(gameID1));

    createGame(gameID2, 2, 1);
    assertEquals(1, controller.getCurrentSlots().get(gameID2));

    createGame(gameID3, 4, 4);
    assertEquals(4, controller.getCurrentSlots().get(gameID3));
  }

  @Test
  void getMaxSlots() {
    UUID connectionID1 = UUID.randomUUID();
    UUID connectionID2 = UUID.randomUUID();

    String gameID1 = "test1";
    String gameID2 = "test2";

    createGame(gameID1, 4, 0);
    assertEquals(4, controller.getMaxSlots().get(gameID1));

    createGame(gameID2, 2, 0);
    assertEquals(2, controller.getMaxSlots().get(gameID2));
  }

  @Test
  void getGame() throws EmptyDeckException {
    final String gameId = "test";

    assertThrows(GameNotFoundException.class, () -> controller.getGame(gameId));
    createGame(gameId, 4, 0);
    AtomicReference<Game> result = new AtomicReference<>();
    assertDoesNotThrow(() -> result.set(controller.getGame(gameId)));
    assertNotNull(result.get());
  }

  @Test
  void removePlayerFromLobby() {
    final String gameId = "test";

    int maxPlayers = 4;

    List<UUID> connectionIDs = createGame(gameId, maxPlayers, 1);
    assertEquals(1, controller.getCurrentSlots().get(gameId));

    assertDoesNotThrow(() -> controller.quitFromLobby(connectionIDs.get(0)));
    assertEquals(0, controller.getCurrentSlots().get(gameId));

    assertDoesNotThrow(
      () -> controller.joinLobby(connectionIDs.get(0), gameId)
    );
    assertEquals(1, controller.getCurrentSlots().get(gameId));

    assertDoesNotThrow(() -> controller.quitFromLobby(connectionIDs.get(0)));
    assertEquals(0, controller.getCurrentSlots().get(gameId));
  }

  @Test
  void joinLobby() throws EmptyDeckException {
    final String gameId = "test";
    final UUID playerId = UUID.randomUUID();

    controller.connect(
      playerId,
      new DummyRemoteGameEventListener(Assertions::fail)
    );

    assertThrows(
      GameNotFoundException.class,
      () -> controller.joinLobby(playerId, gameId)
    );

    try {
      controller.createGame(playerId, gameId, 4);
    } catch (InvalidActionException e) {
      fail("Error creating the game '" + gameId + "': " + e.getMessage());
    }
    assertDoesNotThrow(() -> controller.joinLobby(playerId, gameId));
  }

  @Test
  void lobbySetTokenColor() {
    final String gameId = "test";

    assertThrows(
      PlayerNotFoundException.class,
      () -> controller.lobbySetTokenColor(UUID.randomUUID(), null)
    );

    List<UUID> connectionIDs = createGame(gameId, 4, 2);

    try {
      assertTrue(
        controller
          .getGame(gameId)
          .getLobby()
          .getAvailableColors()
          .contains(TokenColor.RED)
      );

      assertDoesNotThrow(
        () ->
          controller.lobbySetTokenColor(connectionIDs.get(0), TokenColor.RED)
      );
      assertFalse(
        controller
          .getGame(gameId)
          .getLobby()
          .getAvailableColors()
          .contains(TokenColor.RED)
      );

      assertTrue(
        controller
          .getGame(gameId)
          .getLobby()
          .getAvailableColors()
          .contains(TokenColor.BLUE)
      );

      assertDoesNotThrow(
        () ->
          controller.lobbySetTokenColor(connectionIDs.get(0), TokenColor.RED)
      );
      assertThrows(
        TokenAlreadyTakenException.class,
        () ->
          controller.lobbySetTokenColor(connectionIDs.get(1), TokenColor.RED)
      );
      assertDoesNotThrow(
        () ->
          controller.lobbySetTokenColor(connectionIDs.get(1), TokenColor.BLUE)
      );
      assertFalse(
        controller
          .getGame(gameId)
          .getLobby()
          .getAvailableColors()
          .contains(TokenColor.BLUE)
      );
      assertFalse(
        controller
          .getGame(gameId)
          .getLobby()
          .getAvailableColors()
          .contains(TokenColor.RED)
      );
    } catch (GameNotFoundException e) {
      fail("Error getting the game '" + gameId + "': " + e.getMessage());
    }
  }

  @Test
  void lobbySetNickname() {
    final String gameId = "test";

    assertThrows(
      PlayerNotFoundException.class,
      () -> controller.lobbySetNickname(UUID.randomUUID(), "test")
    );
    List<UUID> connectionIDs = createGame(gameId, 4, 2);
    assertDoesNotThrow(
      () -> controller.lobbySetNickname(connectionIDs.getFirst(), "test")
    );
    assertDoesNotThrow(
      () -> controller.lobbySetNickname(connectionIDs.get(1), "test2")
    );

    assertThrows(
      NicknameAlreadyTakenException.class,
      () -> controller.lobbySetNickname(connectionIDs.getFirst(), "test2")
    );
  }

  @Test
  void lobbyChooseObjective() {
    final String gameId = "test";
    final UUID playerId = UUID.randomUUID();

    assertThrows(
      PlayerNotFoundException.class,
      () -> controller.lobbyChooseObjective(playerId, true)
    );

    List<UUID> connectionIDs = createGame(gameId, 4, 1);

    assertDoesNotThrow(
      () -> controller.lobbyChooseObjective(connectionIDs.get(0), true)
    );
  }

  @Test
  void startGame() {
    final String gameId = "test";

    List<UUID> connectionIDs = createGame(gameId, 4, 4);

    assertThrows(
      GameNotReadyException.class,
      () -> controller.startGame(connectionIDs.get(0))
    );

    List<TokenColor> colors = new ArrayList<>(
      Arrays.asList(TokenColor.values())
    );

    for (UUID connectionID : connectionIDs) {
      try {
        controller.lobbySetTokenColor(connectionID, colors.removeLast());
        controller.lobbySetNickname(connectionID, "test-" + colors.size());
        controller.lobbyChooseObjective(connectionID, true);
        controller.joinGame(connectionID, gameId, CardSideType.FRONT);
      } catch (InvalidActionException e) {
        fail("Error setting up the game '" + gameId + "': " + e.getMessage());
      }
    }

    assertThrows(
      GameAlreadyStartedException.class,
      () -> controller.startGame(connectionIDs.get(0))
    );
  }

  @Test
  void joinGame() {
    //TODO

  }

  @Test
  void createGame() {
    //TODO

  }

  @Test
  void isLastRound() {
    //TODO

  }

  @Test
  void deleteGame() {
    //TODO
  }

  @Test
  void nextTurn() {
    //TODO
  }

  @Test
  void testNextTurn() {
    //TODO
  }

  @Test
  void registerListener() {
    //TODO
  }

  @Test
  void removeListener() {
    //TODO

  }

  @Test
  void placeCard() {
    //TODO
  }

  @Test
  void reconnection() throws InterruptedException {
    AtomicReference<Boolean> gameHalted = new AtomicReference<>(false);
    AtomicReference<Boolean> gameResumed = new AtomicReference<>(false);
    AtomicReference<UUID> remainedID = new AtomicReference<>(null);
    AtomicReference<UUID> quitID = new AtomicReference<>(null);

    Consumer<String> fil = Assertions::fail;

    List<UUID> players = createGame(
      "test",
      2,
      2,
      connectionID ->
        new DummyRemoteGameEventListener(fil) {
          @Override
          public void gameHalted(String gameID) throws RemoteException {
            super.gameHalted(gameID);
            System.out.println(
              connectionID + " received game " + gameID + " halted event"
            );
            UUID expected = remainedID.get();
            if (expected != null && !expected.equals(connectionID)) {
              this.failFn.accept(
                  "Game halted event received by the wrong player: " +
                  connectionID +
                  " instead of " +
                  expected
                );
            }
            gameHalted.set(true);
          }

          @Override
          public void gameResumed(String gameID) throws RemoteException {
            super.gameResumed(gameID);
            System.out.println(
              connectionID + " received game " + gameID + " resumed event"
            );
            UUID expected = remainedID.get();
            if (expected != null && !expected.equals(connectionID)) {
              fail(
                "Game resumed event received by the wrong player: " +
                connectionID +
                " instead of " +
                expected
              );
            }
            gameResumed.set(true);
          }

          @Override
          public void gameStarted(String gameId, GameInfo gameInfo)
            throws RemoteException {
            super.gameStarted(gameId, gameInfo);
            System.out.println(
              connectionID + " received game " + gameId + " started event"
            );
          }

          @Override
          public void userContext(FullUserGameContext context)
            throws RemoteException {
            super.userContext(context);
            System.out.println(connectionID + " received user context event");
            if (!connectionID.equals(quitID.get())) {
              this.failFn.accept(
                  "User context event received by the wrong player: " +
                  connectionID +
                  " instead of " +
                  quitID.get()
                );
            }
          }
        }
    );
    Game game = null;
    try {
      game = controller.getGame("test");
    } catch (GameNotFoundException e) {
      fail("Failed to get the game: " + e.getMessage());
    }

    List<TokenColor> colors = Arrays.stream(TokenColor.values()).collect(
      Collectors.toList()
    );

    AtomicReference<List<UUID>> heartBeatIDs = new AtomicReference<>(players);

    Runnable heartbeat = () ->
      heartBeatIDs
        .get()
        .forEach(connectionID -> {
          try {
            controller.heartBeat(connectionID);
          } catch (PlayerNotFoundException ignored) {
            fail("Player not found during heartbeat: " + connectionID);
          }
        });

    // Simulate a heartbeat every 200ms
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(
      new TimerTask() {
        @Override
        public void run() {
          heartbeat.run();
        }
      },
      0,
      200
    );

    players.forEach(connectionID -> {
      try {
        // we use the connectionID as nickname to simplify the test
        controller.lobbySetNickname(connectionID, connectionID.toString());
        controller.lobbySetTokenColor(connectionID, colors.removeLast());
        controller.lobbyChooseObjective(connectionID, true);
        controller.joinGame(connectionID, "test", CardSideType.FRONT);
      } catch (InvalidActionException e) {
        fail("Failed to initialize player: " + e.getMessage());
      }
    });
    Thread.sleep(500);

    UUID activePlayer = UUID.fromString(game.getCurrentPlayer().getNickname());
    System.out.println("Active player: " + activePlayer);
    remainedID.set(activePlayer);
    heartBeatIDs.set(
      heartBeatIDs
        .get()
        .stream()
        .filter(activePlayer::equals)
        .collect(Collectors.toList())
    );
    players
      .stream()
      .filter(pID -> !pID.equals(activePlayer))
      .forEach(quitID::set);

    int attemptes = 0;

    while (!gameHalted.get()) {
      if (attemptes++ > 150) {
        fail("Game did not halt after 15 seconds");
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        fail("Interrupted while waiting for the game to halt");
      }
    }

    heartBeatIDs.set(players);

    int attempts = 0;

    while (!gameResumed.get()) {
      if (attempts++ > 100) {
        fail("Game did not resume after 5 seconds");
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        fail("Interrupted while waiting for the game to halt");
      }
    }
  }
}
