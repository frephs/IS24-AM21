package polimi.ingsw.am21.codex.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;

class GameControllerTest {

  GameController controller;

  @BeforeEach
  void setup() {
    this.controller = new GameController();
  }

  class DummyRemoteGameEventListener implements RemoteGameEventListener {

    @Override
    public void gameCreated(String gameId, int currentPlayers, int maxPlayers)
      throws RemoteException {}

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
      Set<Position> forbiddenSpots
    ) throws RemoteException {}

    @Override
    public void changeTurn(
      String gameId,
      String playerNickname,
      Integer playerIndex,
      Boolean isLastRound,
      Set<Position> availableSpots,
      Set<Position> forbiddenSpots
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
        controller.connect(connectionID1, new DummyRemoteGameEventListener());
        controller.joinLobby(connectionID1, gameId);
      } catch (InvalidActionException e) {
        fail("Error joining the game '" + gameId + "': " + e.getMessage());
      }
    }
    for (int i = 1; i < players; i++) {
      UUID connectionID = UUID.randomUUID();
      playerIDs.add(connectionID);
      try {
        controller.joinLobby(connectionID, gameId);
      } catch (InvalidActionException e) {
        fail("Error joining the game '" + gameId + "': " + e.getMessage());
      }
    }
    return playerIDs;
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

    Integer maxPlayers = 4;

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
      () -> controller.lobbySetNickname(connectionIDs.get(0), "test")
    );
    assertDoesNotThrow(
      () -> controller.lobbySetNickname(connectionIDs.get(1), "test2")
    );

    assertThrows(
      NicknameAlreadyTakenException.class,
      () -> controller.lobbySetNickname(connectionIDs.get(0), "test2")
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
  void joinGame() {}

  @Test
  void createGame() {}

  @Test
  void isLastRound() {}

  @Test
  void deleteGame() {}

  @Test
  void nextTurn() {}

  @Test
  void testNextTurn() {}

  @Test
  void registerListener() {}

  @Test
  void removeListener() {}

  @Test
  void placeCard() {}
}
