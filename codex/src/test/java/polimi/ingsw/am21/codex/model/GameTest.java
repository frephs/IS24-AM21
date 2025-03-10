package polimi.ingsw.am21.codex.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotFoundException;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Lobby.Lobby;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.IncompletePlayerBuilderException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.Player;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.PlayerNotFoundGameException;

class GameTest {

  Game game;

  @BeforeEach
  void initTest() {
    this.game = new Game(2);
  }

  @Test
  void testLobby() {
    try {
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
      try {
        lobby.setNickname(firstPlayer, "FirstPlayer");
      } catch (NicknameAlreadyTakenException e) {
        fail(e);
      }
      try {
        lobby.setToken(firstPlayer, TokenColor.BLUE);
      } catch (TokenAlreadyTakenException e) {
        fail(e);
      }

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
      try {
        lobby.setNickname(secondPlayer, "SecondPlayer");
      } catch (NicknameAlreadyTakenException e) {
        fail(e);
      }
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
        PlayerNotFoundGameException.class,
        () -> lobby.setToken(randomId, TokenColor.RED)
      );
      // the socket ID does not exist cannot set nickname
      assertThrows(
        PlayerNotFoundGameException.class,
        () -> lobby.setNickname(randomId, "test")
      );
      // the lobby is already full cannot add another player
      assertThrows(
        LobbyFullException.LobbyFullInternalException.class,
        () ->
          lobby.addPlayer(
            randomId,
            this.game.drawObjectiveCardPair(),
            this.game.drawStarterCard()
          )
      );

      assertThrows(
        PlayerNotFoundGameException.class,
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
      } catch (
        IllegalCardSideChoiceException | IllegalPlacingPositionException e
      ) {
        fail("Illegal card side choice exception", e);
      } catch (IncompletePlayerBuilderException e) {
        fail("Incomplete player builder exception", e);
      }
      // player was already finalized cannot finalize twice
      assertThrows(
        PlayerNotFoundGameException.class,
        () ->
          lobby.finalizePlayer(
            firstPlayer,
            CardSideType.FRONT,
            this.game.drawHand()
          )
      );
    } catch (PlayerNotFoundGameException e) {
      fail(e);
    }
  }

  void preparePlayers(Game game) {
    try {
      assertNotNull(game);

      // the lobby should be created in the game constructor
      assertNotNull(game.getLobby());

      int players = game.getLobby().getRemainingPlayerSlots();
      int i = -1;
      while (++i < players) {
        UUID playerConnectionID = UUID.randomUUID();

        try {
          game
            .getLobby()
            .addPlayer(
              playerConnectionID,
              game.drawObjectiveCardPair(),
              game.drawStarterCard()
            );
        } catch (Exception e) {
          fail("Failed creating player in lobby");
        }

        try {
          game.getLobby().setNickname(playerConnectionID, "Player_" + i);
        } catch (NicknameAlreadyTakenException e) {
          fail("Nickname already taken exception");
        }

        try {
          game.getLobby().setToken(playerConnectionID, TokenColor.values()[i]);
        } catch (TokenAlreadyTakenException e) {
          fail("Token already taken exception");
        }
        Optional<CardPair<ObjectiveCard>> firstPlayerObjectiveCards = game
          .getLobby()
          .getPlayerObjectiveCards(playerConnectionID);
        if (firstPlayerObjectiveCards.isEmpty()) fail(
          "The first player objective cards are null, this should never " +
          "happened 💀"
        );

        game.getLobby().setObjectiveCard(playerConnectionID, true);
        try {
          Player player = game
            .getLobby()
            .finalizePlayer(
              playerConnectionID,
              CardSideType.FRONT,
              game.drawHand()
            );

          game.addPlayer(player);
        } catch (EmptyDeckException e) {
          fail("Empty deck exception");
        } catch (
          IllegalCardSideChoiceException | IllegalPlacingPositionException e
        ) {
          throw new RuntimeException(e);
        } catch (IncompletePlayerBuilderException e) {
          fail("Incomplete player builder exception", e);
        }
      }
    } catch (PlayerNotFoundGameException e) {
      fail(e);
    }
  }

  @Test
  void testGame() {
    preparePlayers(this.game);

    assertEquals(GameState.GAME_INIT, this.game.getState());
    try {
      game.start();
    } catch (GameNotReadyException e) {
      fail(
        "Not enough players to start the game:\nneeded: " +
        game.getMaxPlayers() +
        "\nfound: " +
        game.getPlayersCount()
      );
    } catch (GameAlreadyStartedException e) {
      fail(e);
    }

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
      try {
        game.start();
      } catch (GameNotReadyException e) {
        fail(
          "Not enough players to start the game:\nneeded: " +
          game.getMaxPlayers() +
          "\nfound: " +
          game.getPlayersCount()
        );
      } catch (GameAlreadyStartedException e) {
        fail(e);
      }
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
