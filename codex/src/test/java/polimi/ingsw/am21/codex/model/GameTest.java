package polimi.ingsw.am21.codex.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.PlayerNotFoundException;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Lobby.Lobby;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Player.Player;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

class GameTest {

  Game game;
  JSONArray cardsJSON;

  GameTest() {
    String jsonLocation =
      "src/main/java/polimi/ingsw/am21/codex/model/Cards" +
      "/Resources/cards.json";
    File file = new File(jsonLocation);
    try {
      String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
      this.cardsJSON = new JSONArray(content);
    } catch (IOException e) {
      e.printStackTrace();
      fail("could not read cards file");
    }
  }

  @BeforeEach
  void initTest() {
    this.game = new Game(2, this.cardsJSON);
  }

  @Test
  void testLobby() {
    // the lobby should be created inside the Game constructor
    Lobby lobby = game.getLobby();
    assertNotNull(lobby);
    assertEquals(2, lobby.getRemainingPlayerSlots());
    assertEquals(0, lobby.getPlayersCount());
    UUID firstPlayer = UUID.randomUUID();
    try {
      lobby.addPlayer(
        firstPlayer,
        this.game.drawObjectiveCardPair(),
        this.game.drawStarterCard()
      );
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception thrown while adding first player");
    }
    // the maximum number of players is 2, one slot is taken so the number of
    // remaining player slots is 1
    assertEquals(1, lobby.getRemainingPlayerSlots());
    // we added a player, so player count should be 1
    assertEquals(1, lobby.getPlayersCount());
    lobby.setNickname(firstPlayer, "FirstPlayer");
    lobby.setToken(firstPlayer, TokenColor.BLUE);

    UUID secondPlayer = UUID.randomUUID();
    try {
      lobby.addPlayer(
        secondPlayer,
        this.game.drawObjectiveCardPair(),
        this.game.drawStarterCard()
      );
    } catch (Exception e) {
      // we pray you don't get a collision 🙏 ( joking a part the probability of a collision is one in a billion, source: https://en.wikipedia.org/wiki/Universally_unique_identifier#Collisions
      // )
      fail("Exception thrown while adding second player");
    }
    // the lobby had a maximum number of 2 players, the remaining player
    // slots is 0, we cannot add players anymore
    assertEquals(0, lobby.getRemainingPlayerSlots());
    // we added 2 players so the player count shall be 2
    assertEquals(2, lobby.getPlayersCount());
    lobby.setNickname(secondPlayer, "SecondPlayer");
    // we pray you don't get a collision 🙏 ( joking a part the probability of a collision is one in a billion, source: https://en.wikipedia.org/wiki/Universally_unique_identifier#Collisions
    // )
    UUID randomId = UUID.randomUUID();
    // the blue token was already taken by firstPlayer and therefore cannot
    // be selected by SecondPlayer
    assertThrows(
      TokenAlreadyTakenException.class,
      () -> lobby.setToken(secondPlayer, TokenColor.BLUE)
    );
    // the socked ID does not exist cannot set token color
    assertThrows(
      PlayerNotFoundException.class,
      () -> lobby.setToken(randomId, TokenColor.RED)
    );
    // the socket ID does not exist cannot set nickname
    assertThrows(
      PlayerNotFoundException.class,
      () -> lobby.setNickname(randomId, "test")
    );
    // the lobby is already full cannot add another player
    assertThrows(
      LobbyFullException.class,
      () ->
        lobby.addPlayer(
          randomId,
          this.game.drawObjectiveCardPair(),
          this.game.drawStarterCard()
        )
    );

    assertThrows(
      PlayerNotFoundException.class,
      () -> lobby.setNickname(randomId, "test")
    );

    lobby.setObjectiveCard(firstPlayer, true);
    try {
      lobby.finalizePlayer(
        firstPlayer,
        CardSideType.BACK,
        this.game.drawHand()
      );
    } catch (EmptyDeckException e) {
      fail("Empty deck exception");
    }
    // player was already finalized cannot finalize twice
    assertThrows(
      PlayerNotFoundException.class,
      () ->
        lobby.finalizePlayer(
          firstPlayer,
          CardSideType.FRONT,
          this.game.drawHand()
        )
    );
  }

  void preparePlayers(Game game) {
    assertNotNull(game);
    // the lobby should be created in the game constructor
    assertNotNull(game.getLobby());

    int players = game.getLobby().getRemainingPlayerSlots();
    int i = 0;
    while (players-- != 0) {
      UUID playerSocketID = UUID.randomUUID();

      try {
        game
          .getLobby()
          .addPlayer(
            playerSocketID,
            game.drawObjectiveCardPair(),
            game.drawStarterCard()
          );
      } catch (Exception e) {
        fail("Failed creating player in lobby");
      }

      game.getLobby().setNickname(playerSocketID, "Player_" + i++);

      game.getLobby().setToken(playerSocketID, TokenColor.GREEN);
      game.getLobby().setToken(playerSocketID, TokenColor.RED);

      Optional<CardPair<ObjectiveCard>> firstPlayerObjectiveCards = game
        .getLobby()
        .getPlayerObjectiveCards(playerSocketID);
      if (firstPlayerObjectiveCards.isEmpty()) fail(
        "The first player objective cards are null, this should never " +
        "happened 💀"
      );

      game.getLobby().setObjectiveCard(playerSocketID, true);
      try {
        Player player = game
          .getLobby()
          .finalizePlayer(playerSocketID, CardSideType.FRONT, game.drawHand());

        game.addPlayer(player);
      } catch (EmptyDeckException e) {
        fail("Empty deck exception");
      }
    }
  }

  @Test
  void testGame() {
    preparePlayers(this.game);

    assertEquals(GameState.GAME_INIT, this.game.getState());
    game.start();

    assertEquals(GameState.PLAYING, this.game.getState());

    // at the game start we shouldn't have remainingRounds as it signals that
    // the game is about to end in next round
    assertEquals(Optional.empty(), this.game.getRemainingRounds());
  }

  @Test
  void testPlayerShuffling() {
    boolean isDifferent = false;
    for (int i = 0; i < 10000 && !isDifferent; ++i) {
      this.game = new Game(4);
      preparePlayers(game);
      game.start();
      List<String> order = game.getPlayersOrder();
      if (
        order.get(0).compareTo("Player_0") != 0 ||
        order.get(1).compareTo("Player_1") != 0 ||
        order.get(2).compareTo("Player_2") != 0
      ) {
        isDifferent = true;
      }

      if (order.size() != 4) {
        fail("The shuffled players count is not the same as the initial one");
      }

      if (new HashSet<>(order).size() != 4) {
        fail("The shuffled players contain duplicate nicknames");
      }
    }
    if (!isDifferent) fail(
      "After 10k tests the player order was never shuffled"
    );
  }
}
