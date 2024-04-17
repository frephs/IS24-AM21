package polimi.ingsw.am21.codex.model.GameBoard;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.EmptyDeckException;
import polimi.ingsw.am21.codex.model.TokenColor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LobbyTest {

  final int MAX_PLAYERS = 2;

  Lobby lobby;

  GameBoard mockGameboard;

  @BeforeEach
  void prepareLobbyTest() {
    this.lobby = new Lobby(MAX_PLAYERS);


    String jsonLocation = "src/main/java/polimi/ingsw/am21/codex/model/Cards/cards.json";
    File file = new File(jsonLocation);
    try {
      String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
      JSONArray cards = new JSONArray(content);
      this.mockGameboard = GameBoard.fromJSON(cards);
    } catch (IOException e) {
      e.printStackTrace();
      fail("could not read cards file");
    }
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
        this.lobby.addPlayer(socketID,
          this.mockGameboard.drawObjectiveCardPair());
      } catch (LobbyFullException e) {
        fail("Failed adding a new player in the lobby while testing " +
          "getRemainingPlayerSlots, player number: " + (i) + " of max " +
          "players: " + MAX_PLAYERS);
      } catch (EmptyDeckException e) {
        fail("Invalid mock GameBoard, the decks are empty");
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
        this.lobby.addPlayer(socketID,
          this.mockGameboard.drawObjectiveCardPair());
      } catch (LobbyFullException e) {
        fail("Failed adding a new player in the lobby while testing " +
          "getRemainingPlayerSlots");
      } catch (EmptyDeckException e) {
        fail("Invalid mock GameBoard, the decks are empty");
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
        this.lobby.addPlayer(socketID,
          this.mockGameboard.drawObjectiveCardPair());
      } catch (LobbyFullException e) {
        fail("Lobby full");
      } catch (EmptyDeckException e) {
        fail("Invalid mock GameBoard, the decks are empty");
      }
      lastAdded = socketID;
      socketID = generateNewSocketID();
    }
    assertThrows(LobbyFullException.class,
      () -> this.lobby.addPlayer(generateNewSocketID(),
        this.mockGameboard.drawObjectiveCardPair()));


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
      this.lobby.addPlayer(socketID,
        this.mockGameboard.drawObjectiveCardPair());
    } catch (LobbyFullException e) {
      fail("Lobby full");
    } catch (EmptyDeckException e) {
      fail("Invalid mock GameBoard, the decks are empty");
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
      this.lobby.addPlayer(socketID,
        this.mockGameboard.drawObjectiveCardPair());
    } catch (LobbyFullException e) {
      fail("Lobby full");
    } catch (EmptyDeckException e) {
      fail("Invalid mock GameBoard, the decks are empty");
    }

    this.lobby.setNickname(socketID, "test");
    Optional<String> playerNickname = this.lobby.getPlayerNickname(socketID);
    if (playerNickname.isEmpty())
      fail("could not find player with socket id" + socketID);
    assertEquals(playerNickname.get(), "test");

    UUID socketID2 = generateNewSocketID();
    try {
      this.lobby.addPlayer(socketID2,
        this.mockGameboard.drawObjectiveCardPair());
    } catch (LobbyFullException e) {
      fail("Lobby full");
    } catch (EmptyDeckException e) {
      fail("Invalid mock GameBoard, the decks are empty");
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
      this.lobby.addPlayer(socketID,
        this.mockGameboard.drawObjectiveCardPair());
    } catch (LobbyFullException e) {
      fail("Lobby full");
    } catch (EmptyDeckException e) {
      fail("Invalid mock GameBoard, the decks are empty");
    }

    this.lobby.setToken(socketID, TokenColor.GREEN);
    Optional<TokenColor> playerTokenColor =
      this.lobby.getPlayerTokenColor(socketID);
    if (playerTokenColor.isEmpty())
      fail("could not find player with socket id" + socketID);
    assertEquals(playerTokenColor.get(), TokenColor.GREEN);

    UUID socketID2 = generateNewSocketID();
    try {
      this.lobby.addPlayer(socketID2,
        this.mockGameboard.drawObjectiveCardPair());
    } catch (LobbyFullException e) {
      fail("Lobby full");
    } catch (EmptyDeckException e) {
      fail("Invalid mock GameBoard, the decks are empty");
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
      this.lobby.addPlayer(socketID,
        this.mockGameboard.drawObjectiveCardPair());
    } catch (LobbyFullException e) {
      fail("Lobby full");
    } catch (EmptyDeckException e) {
      fail("Invalid mock GameBoard, the decks are empty");
    }

    this.lobby.setNickname(socketID, "test");

    Optional<String> playerNickname = this.lobby.getPlayerNickname(socketID);
    if (playerNickname.isEmpty())
      fail("could not find player with socket id" + socketID);
    assertEquals(playerNickname.get(), "test");

//    UUID socketID2 = generateNewSocketID();


  }

  @Test
  void getPlayerObjectiveCards() {
  }

}