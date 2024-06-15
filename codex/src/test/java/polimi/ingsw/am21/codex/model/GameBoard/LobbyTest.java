package polimi.ingsw.am21.codex.model.GameBoard;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Lobby.Lobby;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.PlayerNotFoundGameException;

class LobbyTest {

  final int MAX_PLAYERS = 2;

  Lobby lobby;

  GameBoard mockGameboard;

  @BeforeEach
  void prepareLobbyTest() {
    this.lobby = new Lobby(MAX_PLAYERS);
    this.mockGameboard = new GameBoard(new CardsLoader());
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
        this.lobby.addPlayer(
            socketID,
            this.mockGameboard.drawObjectiveCardPair(),
            this.mockGameboard.drawStarterCardFromDeck()
          );
      } catch (LobbyFullException.LobbyFullInternalException e) {
        fail(
          "Failed adding a new player in the lobby while testing " +
          "getRemainingPlayerSlots, player number: " +
          (i) +
          " of max " +
          "players: " +
          MAX_PLAYERS
        );
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
        this.lobby.addPlayer(
            socketID,
            this.mockGameboard.drawObjectiveCardPair(),
            this.mockGameboard.drawStarterCardFromDeck()
          );
      } catch (LobbyFullException.LobbyFullInternalException e) {
        fail(
          "Failed adding a new player in the lobby while testing " +
          "getRemainingPlayerSlots"
        );
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
        this.lobby.addPlayer(
            socketID,
            this.mockGameboard.drawObjectiveCardPair(),
            this.mockGameboard.drawStarterCardFromDeck()
          );
      } catch (LobbyFullException.LobbyFullInternalException e) {
        fail("Lobby full");
      } catch (EmptyDeckException e) {
        fail("Invalid mock GameBoard, the decks are empty");
      }
      lastAdded = socketID;
      socketID = generateNewSocketID();
    }
    assertThrows(
      LobbyFullException.LobbyFullInternalException.class,
      () ->
        this.lobby.addPlayer(
            generateNewSocketID(),
            this.mockGameboard.drawObjectiveCardPair(),
            this.mockGameboard.drawStarterCardFromDeck()
          )
    );

    try {
      this.lobby.setNickname(firstAdded, "test1");
    } catch (PlayerNotFoundGameException | NicknameAlreadyTakenException e) {
      fail(e);
    }
    try {
      this.lobby.setNickname(lastAdded, "test2");
    } catch (PlayerNotFoundGameException | NicknameAlreadyTakenException e) {
      fail(e);
    }
  }

  @Test
  void removePlayer() {
    UUID socketID = generateNewSocketID();
    try {
      this.lobby.addPlayer(
          socketID,
          this.mockGameboard.drawObjectiveCardPair(),
          this.mockGameboard.drawStarterCardFromDeck()
        );
    } catch (LobbyFullException.LobbyFullInternalException e) {
      fail("Lobby full");
    } catch (EmptyDeckException e) {
      fail("Invalid mock GameBoard, the decks are empty");
    }

    UUID fakeUUID = generateNewSocketID();
    try {
      this.lobby.removePlayer(socketID);
    } catch (PlayerNotFoundGameException e) {
      fail("Player not found");
    }
    assertThrows(
      PlayerNotFoundGameException.class,
      () -> this.lobby.removePlayer(fakeUUID)
    );
  }

  @Test
  void setNickname() {
    UUID socketID = generateNewSocketID();
    try {
      this.lobby.addPlayer(
          socketID,
          this.mockGameboard.drawObjectiveCardPair(),
          this.mockGameboard.drawStarterCardFromDeck()
        );
    } catch (LobbyFullException.LobbyFullInternalException e) {
      fail("Lobby full");
    } catch (EmptyDeckException e) {
      fail("Invalid mock GameBoard, the decks are empty");
    }

    try {
      this.lobby.setNickname(socketID, "test");
    } catch (NicknameAlreadyTakenException | PlayerNotFoundGameException e) {
      fail(e);
    }
    Optional<String> playerNickname = Optional.empty();
    try {
      playerNickname = this.lobby.getPlayerNickname(socketID);
    } catch (PlayerNotFoundGameException e) {
      fail(e);
    }
    if (playerNickname.isEmpty()) fail(
      "could not find player with socket id" + socketID
    );
    assertEquals(playerNickname.get(), "test");

    UUID socketID2 = generateNewSocketID();
    try {
      this.lobby.addPlayer(
          socketID2,
          this.mockGameboard.drawObjectiveCardPair(),
          this.mockGameboard.drawStarterCardFromDeck()
        );
    } catch (LobbyFullException.LobbyFullInternalException e) {
      fail("Lobby full");
    } catch (EmptyDeckException e) {
      fail("Invalid mock GameBoard, the decks are empty");
    }

    assertThrows(
      NicknameAlreadyTakenException.class,
      () -> this.lobby.setNickname(socketID2, "test")
    );

    try {
      this.lobby.setNickname(socketID2, "test2");
    } catch (NicknameAlreadyTakenException e) {
      fail("Wrongfully thrown NicknameAlreadyTakenException");
    } catch (PlayerNotFoundGameException e) {
      fail("Wrongfully thrown PlayerNotFoundGameException");
    }
  }

  @Test
  void setToken() {
    UUID socketID = generateNewSocketID();
    try {
      this.lobby.addPlayer(
          socketID,
          this.mockGameboard.drawObjectiveCardPair(),
          this.mockGameboard.drawStarterCardFromDeck()
        );
    } catch (LobbyFullException.LobbyFullInternalException e) {
      fail("Lobby full");
    } catch (EmptyDeckException e) {
      fail("Invalid mock GameBoard, the decks are empty");
    }

    try {
      this.lobby.setToken(socketID, TokenColor.GREEN);
    } catch (TokenAlreadyTakenException e) {
      fail("Wrongfully thrown TokenAlreadyTakenException");
    } catch (PlayerNotFoundGameException e) {
      fail("Wrongfully thrown PlayerNotFoundGameException");
    }
    Optional<TokenColor> playerTokenColor = null;
    try {
      playerTokenColor = this.lobby.getPlayerTokenColor(socketID);
    } catch (PlayerNotFoundGameException e) {
      fail("Wrongfully thrown PlayerNotFoundGameException");
    }
    if (playerTokenColor.isEmpty()) fail(
      "could not find player with socket id" + socketID
    );
    assertEquals(playerTokenColor.get(), TokenColor.GREEN);

    UUID socketID2 = generateNewSocketID();
    try {
      this.lobby.addPlayer(
          socketID2,
          this.mockGameboard.drawObjectiveCardPair(),
          this.mockGameboard.drawStarterCardFromDeck()
        );
    } catch (LobbyFullException.LobbyFullInternalException e) {
      fail("Lobby full");
    } catch (EmptyDeckException e) {
      fail("Invalid mock GameBoard, the decks are empty");
    }

    assertThrows(
      TokenAlreadyTakenException.class,
      () -> this.lobby.setToken(socketID2, TokenColor.GREEN)
    );
    try {
      this.lobby.setToken(socketID2, TokenColor.RED);
    } catch (TokenAlreadyTakenException e) {
      fail("Wrongfully thrown TokenAlreadyTakenException");
    } catch (PlayerNotFoundGameException e) {
      fail("Wrongfully thrown PlayerNotFoundGameException");
    }
  }

  @Test
  void finalizePlayer() {
    UUID socketID = generateNewSocketID();
    try {
      this.lobby.addPlayer(
          socketID,
          this.mockGameboard.drawObjectiveCardPair(),
          this.mockGameboard.drawStarterCardFromDeck()
        );
    } catch (LobbyFullException.LobbyFullInternalException e) {
      fail("Lobby full");
    } catch (EmptyDeckException e) {
      fail("Invalid mock GameBoard, the decks are empty");
    }

    try {
      this.lobby.setNickname(socketID, "test");
    } catch (NicknameAlreadyTakenException | PlayerNotFoundGameException e) {
      fail(e);
    }

    Optional<String> playerNickname = Optional.empty();
    try {
      playerNickname = this.lobby.getPlayerNickname(socketID);
    } catch (PlayerNotFoundGameException e) {
      fail(e);
    }
    if (playerNickname.isEmpty()) fail(
      "could not find player with socket id" + socketID
    );
    assertEquals(playerNickname.get(), "test");
  }

  @Test
  void getPlayerObjectiveCards() {
    UUID playerId = generateNewSocketID();
    try {
      CardPair<ObjectiveCard> objectiveCards =
        this.mockGameboard.drawObjectiveCardPair();
      this.lobby.addPlayer(
          playerId,
          objectiveCards,
          this.mockGameboard.drawStarterCardFromDeck()
        );
      Optional<CardPair<ObjectiveCard>> returnedObjectiveCards =
        this.lobby.getPlayerObjectiveCards(playerId);

      if (returnedObjectiveCards.isPresent()) assertEquals(
        returnedObjectiveCards.get(),
        objectiveCards
      );
      else fail("Could not find player with socket id" + playerId);
    } catch (LobbyFullException.LobbyFullInternalException e) {
      fail("Lobby full");
    } catch (EmptyDeckException e) {
      fail("Invalid mock GameBoard, the decks are empty");
    }
  }

  @Test
  void setObjectiveCard() {}

  @Test
  void containsSocketID() {
    UUID existingID = UUID.randomUUID();
    UUID nonExistingID = UUID.randomUUID();
    while (nonExistingID.equals(existingID)) nonExistingID = UUID.randomUUID();
    try {
      lobby.addPlayer(
        existingID,
        mockGameboard.drawObjectiveCardPair(),
        mockGameboard.drawStarterCardFromDeck()
      );
    } catch (Exception e) {
      fail("Failed to add player", e);
    }

    assertTrue(this.lobby.containsSocketID(existingID));
    assertFalse(this.lobby.containsSocketID(nonExistingID));
  }

  @Test
  void getPlayerNickname() {
    UUID firstPlayerID = UUID.randomUUID();
    try {
      try {
        lobby.addPlayer(
          firstPlayerID,
          mockGameboard.drawObjectiveCardPair(),
          mockGameboard.drawStarterCardFromDeck()
        );
      } catch (Exception e) {
        fail("Failed to add player", e);
      }

      Optional<String> playerNickname = lobby.getPlayerNickname(firstPlayerID);
      assertEquals(Optional.empty(), playerNickname);

      try {
        lobby.setNickname(firstPlayerID, "firstPlayer");
      } catch (NicknameAlreadyTakenException e) {
        fail(e);
      }
      playerNickname = lobby.getPlayerNickname(firstPlayerID);
      if (playerNickname.isEmpty()) {
        fail("Empty player nickname");
      }
      assertEquals("firstPlayer", playerNickname.get());
    } catch (PlayerNotFoundGameException e) {
      fail("Player not found", e);
    }
  }

  @Test
  void getPlayerTokenColor() {
    try {
      UUID firstPlayerID = UUID.randomUUID();
      try {
        lobby.addPlayer(
          firstPlayerID,
          mockGameboard.drawObjectiveCardPair(),
          mockGameboard.drawStarterCardFromDeck()
        );
      } catch (Exception e) {
        fail("Failed to add player", e);
      }

      Optional<TokenColor> playerTokenColor = lobby.getPlayerTokenColor(
        firstPlayerID
      );
      assertEquals(Optional.empty(), playerTokenColor);

      try {
        lobby.setToken(firstPlayerID, TokenColor.RED);
      } catch (TokenAlreadyTakenException e) {
        fail("wrongfully thrown TokenAlreadyTakenException");
      }
      playerTokenColor = lobby.getPlayerTokenColor(firstPlayerID);
      if (playerTokenColor.isEmpty()) {
        fail("Empty player token");
      }
      assertEquals(TokenColor.RED, playerTokenColor.get());
    } catch (PlayerNotFoundGameException e) {
      fail("Player not found", e);
    }
  }

  @Test
  void getStarterCard() {
    UUID firstPlayerID = UUID.randomUUID();
    PlayableCard starterCard;
    try {
      starterCard = mockGameboard.drawStarterCardFromDeck();
      lobby.addPlayer(
        firstPlayerID,
        mockGameboard.drawObjectiveCardPair(),
        starterCard
      );
      assertEquals(starterCard, lobby.getStarterCard(firstPlayerID));
    } catch (Exception e) {
      fail("Failed to add player", e);
    }
  }
}
