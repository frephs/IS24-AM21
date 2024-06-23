package polimi.ingsw.am21.codex.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javafx.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.Main;
import polimi.ingsw.am21.codex.connection.server.RMI.DummyRemoteGameEventLister;
import polimi.ingsw.am21.codex.controller.exceptions.*;
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
import polimi.ingsw.am21.codex.model.Lobby.exceptions.IncompletePlayerBuilderException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameAlreadyExistsException;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

class GameControllerTest {

  GameController controller;

  @BeforeEach
  void setup() {
    this.controller = new GameController();
  }

  private List<UUID> createGame(String gameId, int maxPlayers, int players) {
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
        controller.connect(connectionID1, new DummyRemoteGameEventLister());
        controller.joinLobby(connectionID1, gameId);
      } catch (NullPointerException | InvalidActionException e) {
        fail("Error joining the game '" + gameId + "': " + e.getMessage());
      }
    }
    for (int i = 1; i < players; i++) {
      UUID connectionID = UUID.randomUUID();
      playerIDs.add(connectionID);
      try {
        controller.connect(connectionID, new DummyRemoteGameEventLister());
        controller.joinLobby(connectionID, gameId);
      } catch (InvalidActionException e) {
        fail("Error joining the game '" + gameId + "': " + e.getMessage());
      }
    }
    return playerIDs;
  }

  @Test
  void registerListener() {
    final UUID connectionID = UUID.randomUUID();
    final RemoteGameEventListener listener = new DummyRemoteGameEventLister();

    assertDoesNotThrow(() -> controller.connect(connectionID, listener));
    controller.userContexts.forEach(
      (key, value) -> assertEquals(listener, value.getListener())
    );
  }

  @Test
  void removeListener() {
    final UUID connectionID = UUID.randomUUID();
    final RemoteGameEventListener listener = new DummyRemoteGameEventLister();

    assertDoesNotThrow(() -> controller.connect(connectionID, listener));
    //TODO
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

    controller.connect(playerId, new DummyRemoteGameEventLister());

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

        assertThrows(
          GameNotReadyException.class,
          () -> controller.startGame(connectionID)
        );

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
    final String gameId = "test";

    List<UUID> connectionIDs = createGame(gameId, 4, 4);

    assertThrows(
      IncompleteLobbyPlayerException.class,
      () ->
        controller.joinGame(connectionIDs.get(0), gameId, CardSideType.FRONT)
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
      PlayerNotFoundException.class,
      () ->
        controller.joinGame(connectionIDs.get(0), gameId, CardSideType.FRONT)
    );

    assertThrows(GameAlreadyStartedException.class, () -> {
      controller.connect(UUID.randomUUID(), new DummyRemoteGameEventLister());
      controller.joinLobby(UUID.randomUUID(), gameId);
    });
  }

  @Test
  void createGame() {
    final String gameId = "test";
    final UUID playerId = UUID.randomUUID();

    assertDoesNotThrow(() -> controller.createGame(playerId, gameId, 4));
    assertThrows(
      GameAlreadyExistsException.class,
      () -> controller.createGame(playerId, gameId, 4)
    );
  }

  @Test
  void isLastRound() {}

  @Test
  void deleteGame() {
    // TODO

    //    final String gameId = "test";
    //    UUID notInGameUser = UUID.randomUUID();
    //    assertThrows(
    //      GameNotFoundException.class,
    //      () -> controller.deleteGame(UUID.randomUUID(), gameId)
    //    );
    //
    //    List<UUID> connectionIDs = createGame(gameId, 1, 1);
    //
    //    assertThrows(
    //      NotInGameException.class,
    //      () -> controller.deleteGame(notInGameUser, gameId)
    //    );
    //
    //    assertThrows(
    //      NotInGameException.class,
    //      () -> controller.quitFromLobby(notInGameUser)
    //    );
    //
    //    assertDoesNotThrow(
    //      () -> controller.quitFromLobby(connectionIDs.getFirst())
    //    );
    //
    //    assertDoesNotThrow(
    //      () -> controller.deleteGame(connectionIDs.getFirst(), gameId)
    //    );
    //
    //    assertThrows(GameNotFoundException.class, () -> controller.getGame(gameId));
  }

  @Test
  void nextTurn() {
    final String gameId = "test";

    List<UUID> connectionIDs = createGame(gameId, 4, 4);

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
    try {
      for (int i = 0; i < connectionIDs.size(); i++) {
        UUID socketId = controller
          .getGame(gameId)
          .getPlayers()
          .get(i)
          .getSocketId();

        int index = connectionIDs.indexOf(socketId);

        for (int j = 1; j <= connectionIDs.size() - 1; j++) {
          int finalJ = j;
          assertThrows(
            PlayerNotActive.class,
            () ->
              controller.nextTurn(
                connectionIDs.get((index + finalJ) % 4),
                DrawingCardSource.Deck,
                DrawingDeckType.RESOURCE
              )
          );
        }

        controller.placeCard(
          connectionIDs.get(index),
          2,
          CardSideType.FRONT,
          new Position(0, 1)
        );

        assertThrows(
          InvalidNextTurnCallException.class,
          () -> controller.nextTurn(connectionIDs.get(index))
        );

        assertDoesNotThrow(
          () ->
            controller.nextTurn(
              connectionIDs.get(index),
              DrawingCardSource.Deck,
              DrawingDeckType.RESOURCE
            )
        );
      }
    } catch (InvalidActionException e) {
      fail("Error starting the game '" + gameId + "': " + e.getMessage());
    }
  }

  @Test
  void placeCard() {
    final String gameId = "test";

    List<UUID> connectionIDs = createGame(gameId, 4, 4);

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

    try {
      for (int i = 0; i < connectionIDs.size(); i++) {
        UUID socketId = controller
          .getGame(gameId)
          .getPlayers()
          .get(i)
          .getSocketId();

        int index = connectionIDs.indexOf(socketId);

        for (int j = 1; j <= connectionIDs.size() - 1; j++) {
          int finalJ = j;
          assertThrows(
            PlayerNotActive.class,
            () ->
              controller.placeCard(
                connectionIDs.get((index + finalJ) % 4),
                2,
                CardSideType.FRONT,
                new Position(0, 1)
              )
          );
        }

        assertDoesNotThrow(
          () ->
            controller.placeCard(
              connectionIDs.get(index),
              2,
              CardSideType.FRONT,
              new Position(0, 1)
            )
        );

        assertThrows(
          AlreadyPlacedCardException.class,
          () ->
            controller.placeCard(
              connectionIDs.get(index),
              2,
              CardSideType.FRONT,
              new Position(0, 1)
            )
        );

        assertDoesNotThrow(
          () ->
            controller.nextTurn(
              connectionIDs.get(index),
              DrawingCardSource.Deck,
              DrawingDeckType.RESOURCE
            )
        );
      }
    } catch (InvalidActionException e) {
      fail("Error starting the game '" + gameId + "': " + e.getMessage());
    }
  }
}
