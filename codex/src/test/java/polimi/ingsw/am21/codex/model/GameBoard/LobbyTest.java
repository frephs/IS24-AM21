package polimi.ingsw.am21.codex.model.GameBoard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Player;
import polimi.ingsw.am21.codex.model.TokenColor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LobbyTest {

  final int MAX_PLAYERS = 2;

  Lobby lobby;

  @BeforeEach
  void prepareLobbyTest() {
    this.lobby = new Lobby(MAX_PLAYERS);
  }

  UUID generateNewSocketID() {

    UUID socketID;
    do {
      socketID = UUID.randomUUID();
    } while (this.lobby.containsSocketID(socketID));
    return socketID;
  }


  @Test
  void getRemainingPlayerSlots() {
    assertEquals(MAX_PLAYERS, this.lobby.getRemainingPlayerSlots());
    for (int i = 0; i < MAX_PLAYERS; i++) {
      UUID socketID = generateNewSocketID();
      try {
        this.lobby.addPlayer(socketID);
      } catch (LobbyFullException e) {
        fail("Failed adding a new player in the lobby while testing " +
          "getRemainingPlayerSlots, player number: " + (i) + " of max " +
          "players: " + MAX_PLAYERS);
      }
      assertEquals(MAX_PLAYERS - i - 1, this.lobby.getRemainingPlayerSlots());
    }
  }

  @Test
  void getPlayersCount() {
    assertEquals(0, this.lobby.getPlayersCount());
    for (int i = 0; i < MAX_PLAYERS; ++i) {
      UUID socketID = generateNewSocketID();
      try {
        this.lobby.addPlayer(socketID);
      } catch (LobbyFullException e) {
        fail("Failed adding a new player in the lobby while testing " +
          "getRemainingPlayerSlots");
      }
      assertEquals(i + 1, this.lobby.getPlayersCount());
    }
  }

  @Test
  void addPlayer() {
    UUID socketID = generateNewSocketID();
    UUID firstAdded = socketID;
    UUID lastAdded = socketID;
    for (int i = 0; i < MAX_PLAYERS; ++i) {
      try {
        this.lobby.addPlayer(socketID);
      } catch (LobbyFullException e) {
        fail("Lobby full");
      }
      lastAdded = socketID;
      socketID = generateNewSocketID();
    }
    assertThrows(LobbyFullException.class,
      () -> this.lobby.addPlayer(generateNewSocketID()));


    try {
      this.lobby.setNickname(firstAdded, "test1");
    } catch (PlayerNotFoundException e) {
      fail("First player is not in the lobby");
    }
    try {
      this.lobby.setNickname(lastAdded, "test2");
    } catch (PlayerNotFoundException e) {
      fail("Last player is not in the lobby");
    }

  }

  @Test
  void removePlayer() {
    UUID socketID = generateNewSocketID();
    try {
      this.lobby.addPlayer(socketID);
    } catch (LobbyFullException e) {
      fail("Lobby full");
    }
    UUID fakeUUID = generateNewSocketID();
    try {
      this.lobby.removePlayer(socketID);
    } catch (PlayerNotFoundException e) {
      fail("Player not found");
    }
    assertThrows(PlayerNotFoundException.class,
      () -> this.lobby.removePlayer(fakeUUID));
  }

  @Test
  void setNickname() {
    UUID socketID = generateNewSocketID();
    try {
      this.lobby.addPlayer(socketID);
    } catch (LobbyFullException e) {
      fail("Lobby full");
    }

    this.lobby.setNickname(socketID, "test");
    Optional<String> playerNickname = this.lobby.getPlayerNickname(socketID);
    if (playerNickname.isEmpty())
      fail("could not find player with socket id" + socketID);
    assertEquals(playerNickname.get(), "test");

    UUID socketID2 = generateNewSocketID();
    try {
      this.lobby.addPlayer(socketID2);
    } catch (LobbyFullException e) {
      fail("Lobby full");
    }

    assertThrows(NicknameAlreadyTakenException.class,
      () -> this.lobby.setNickname(socketID2, "test"));

    try {
      this.lobby.setNickname(socketID2, "test2");
    } catch (NicknameAlreadyTakenException e) {
      fail("Wrongfully thrown NicknameAlreadyTakenException");
    }

  }

  @Test
  void setToken() {
    UUID socketID = generateNewSocketID();
    try {
      this.lobby.addPlayer(socketID);
    } catch (LobbyFullException e) {
      fail("Lobby full");
    }

    this.lobby.setToken(socketID, TokenColor.GREEN);
    Optional<TokenColor> playerTokenColor =
      this.lobby.getPlayerTokenColor(socketID);
    if (playerTokenColor.isEmpty())
      fail("could not find player with socket id" + socketID);
    assertEquals(playerTokenColor.get(), TokenColor.GREEN);

    UUID socketID2 = generateNewSocketID();
    try {
      this.lobby.addPlayer(socketID2);
    } catch (LobbyFullException e) {
      fail("Lobby full");
    }

    assertThrows(TokenAlreadyTakenException.class,
      () -> this.lobby.setToken(socketID2, TokenColor.GREEN));
    try {
      this.lobby.setToken(socketID2, TokenColor.RED);
    } catch (TokenAlreadyTakenException e) {
      fail("Wrongfully thrown TokenAlreadyTakenException");
    }
  }

  //@Test
  //void setExtractedCard() {
  //TODO
  // }

  @Test
  void finalizePlayer() {
    UUID socketID = generateNewSocketID();
    try {
      this.lobby.addPlayer(socketID);
    } catch (LobbyFullException e) {
      fail("Lobby full");
    }

    this.lobby.setNickname(socketID, "test");

    Optional<String> playerNickname = this.lobby.getPlayerNickname(socketID);
    if (playerNickname.isEmpty())
      fail("could not find player with socket id" + socketID);
    assertEquals(playerNickname.get(), "test");

    UUID socketID2 = generateNewSocketID();


  }

  @Test
  void getPlayerObjectiveCards() {
  }

}