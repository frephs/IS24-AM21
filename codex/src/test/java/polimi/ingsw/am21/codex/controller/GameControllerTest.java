package polimi.ingsw.am21.codex.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Game;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

class GameControllerTest {

  GameController controller;

  @BeforeEach
  void setup() {
    this.controller = new GameController();
  }

  @Test
  void getGames() throws EmptyDeckException {
    assertEquals(0, controller.getGames().size());

    controller.createGame("test", 4);
    assertEquals(1, controller.getGames().size());

    controller.createGame("test2", 4);
    assertEquals(2, controller.getGames().size());
  }

  @Test
  void getCurrentSlots()
    throws EmptyDeckException, GameAlreadyStartedException, LobbyFullException, GameNotFoundException {
    final String gameId1 = "test1";
    final String gameId2 = "test2";

    controller.createGame(gameId1, 4);
    assertEquals(0, controller.getCurrentSlots().get(gameId1));

    controller.joinLobby(gameId1, UUID.randomUUID());
    assertEquals(1, controller.getCurrentSlots().get(gameId1));

    controller.createGame(gameId2, 4);
    assertEquals(1, controller.getCurrentSlots().get(gameId1));
    assertEquals(0, controller.getCurrentSlots().get(gameId2));
  }

  @Test
  void getMaxSlots() throws EmptyDeckException {
    final String gameId1 = "test1";
    final String gameId2 = "test2";

    controller.createGame(gameId1, 4);
    assertEquals(4, controller.getMaxSlots().get(gameId1));

    controller.createGame(gameId2, 2);
    assertEquals(4, controller.getMaxSlots().get(gameId1));
    assertEquals(2, controller.getMaxSlots().get(gameId2));
  }

  @Test
  void getGame() throws EmptyDeckException {
    final String gameId = "test";

    assertThrows(GameNotFoundException.class, () -> controller.getGame(gameId));

    controller.createGame(gameId, 4);

    AtomicReference<Game> result = new AtomicReference<>();
    assertDoesNotThrow(() -> result.set(controller.getGame(gameId)));
    assertNotNull(result.get());
  }

  @Test
  void removePlayerFromLobby()
    throws EmptyDeckException, GameAlreadyStartedException, LobbyFullException, GameNotFoundException {
    final String gameId = "test";
    final UUID playerId = UUID.randomUUID();

    controller.createGame(gameId, 4);
    assertEquals(0, controller.getCurrentSlots().get(gameId));
    controller.joinLobby(gameId, playerId);
    assertEquals(1, controller.getCurrentSlots().get(gameId));
    assertDoesNotThrow(
      () -> controller.removePlayerFromLobby(gameId, playerId)
    );
    assertEquals(0, controller.getCurrentSlots().get(gameId));
    controller.joinLobby(gameId, playerId);
    assertDoesNotThrow(
      () ->
        controller.removePlayerFromLobby(controller.getGame(gameId), playerId)
    );
    assertEquals(0, controller.getCurrentSlots().get(gameId));
  }

  @Test
  void joinLobby() throws EmptyDeckException {
    final String gameId = "test";
    final UUID playerId = UUID.randomUUID();

    assertThrows(
      GameNotFoundException.class,
      () -> controller.joinLobby(gameId, playerId)
    );

    controller.createGame(gameId, 4);
    assertDoesNotThrow(() -> controller.joinLobby(gameId, playerId));
  }

  @Test
  void lobbySetTokenColor()
    throws EmptyDeckException, GameAlreadyStartedException, LobbyFullException, GameNotFoundException {
    final String gameId = "test";
    final UUID playerId1 = UUID.randomUUID();
    final UUID playerId2 = UUID.randomUUID();

    assertThrows(
      GameNotFoundException.class,
      () -> controller.lobbySetTokenColor(gameId, playerId1, null)
    );

    controller.createGame(gameId, 4);
    controller.joinLobby(gameId, playerId1);
    assertTrue(
      controller
        .getGame(gameId)
        .getLobby()
        .getAvailableColors()
        .contains(TokenColor.RED)
    );
    assertDoesNotThrow(
      () -> controller.lobbySetTokenColor(gameId, playerId1, TokenColor.RED)
    );
    assertFalse(
      controller
        .getGame(gameId)
        .getLobby()
        .getAvailableColors()
        .contains(TokenColor.RED)
    );

    controller.joinLobby(gameId, playerId2);
    assertTrue(
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

    assertThrows(
      TokenAlreadyTakenException.class,
      () -> controller.lobbySetTokenColor(gameId, playerId2, TokenColor.RED)
    );
    assertDoesNotThrow(
      () -> controller.lobbySetTokenColor(gameId, playerId2, TokenColor.BLUE)
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
  }

  @Test
  void lobbySetNickname()
    throws EmptyDeckException, GameAlreadyStartedException, LobbyFullException, GameNotFoundException {
    final String gameId = "test";
    final UUID playerId = UUID.randomUUID();

    assertThrows(
      GameNotFoundException.class,
      () -> controller.lobbySetNickname(gameId, playerId, "test")
    );

    controller.createGame(gameId, 4);
    controller.joinLobby(gameId, playerId);
    assertDoesNotThrow(
      () -> controller.lobbySetNickname(gameId, playerId, "test")
    );
  }

  @Test
  void lobbyChooseObjective()
    throws EmptyDeckException, GameAlreadyStartedException, LobbyFullException, GameNotFoundException {
    final String gameId = "test";
    final UUID playerId = UUID.randomUUID();

    assertThrows(
      GameNotFoundException.class,
      () -> controller.lobbyChooseObjective(gameId, playerId, true)
    );

    controller.createGame(gameId, 4);
    controller.joinLobby(gameId, playerId);
    assertDoesNotThrow(
      () -> controller.lobbyChooseObjective(gameId, playerId, true)
    );
  }

  @Test
  void startGame() throws EmptyDeckException {
    // TODO finish implementation

    //    final String gameId = "test";
    //    final UUID playerId = UUID.randomUUID();
    //
    //    assertThrows(
    //      GameNotFoundException.class,
    //      () -> controller.startGame(gameId)
    //    );
    //
    //    controller.createGame(gameId, 4);
    //    assertDoesNotThrow(() -> controller.startGame(gameId));
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
