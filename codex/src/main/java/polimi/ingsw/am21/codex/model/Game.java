package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.GameBoard.*;

import java.util.*;

import org.json.JSONArray;


public class Game {
  static final int WINNING_POINTS = 20;
  private final List<Player> players;
  private final GameBoard gameBoard;
  private Lobby lobby;
  private GameState state;
  private Integer remainingRounds = null;
  Integer currentPlayer;


  public Game(int players, JSONArray cards) {
    this.lobby = new Lobby();
    this.state = GameState.GAME_INIT;
    this.lobby = new Lobby(players);
    this.gameBoard = GameBoard.fromJSON(cards);
    this.players = new ArrayList<>();
  }


  /**
   * Gets the lobby associated with the game.
   *
   * @return The lobby associated with the game.
   */
  public Lobby getLobby() {
    return this.lobby;
  }


  /**
   * Starts the game.
   */
  public void start() {
    this.state = GameState.PLAYING;
    Collections.shuffle(players);

  }

  /**
   * Draws a pair of objective cards from the deck.
   *
   * @return A pair of objective cards drawn from the deck.
   * @throws EmptyDeckException When the deck being drawn from is empty.
   */
  public CardPair<ObjectiveCard> drawObjectiveCardPair()
  throws EmptyDeckException {
    return this.gameBoard.drawObjectiveCardPair();
  }

  /**
   * Gets the state of the game.
   *
   * @return The state of the game.
   */
  public GameState getState() {
    return this.state;
  }

  /**
   * Gets the state of a player.
   *
   * @param nickname The nickname of the player.
   * @return The state of the player.
   * @throws PlayerNotFoundException If the player is not found.
   */
  public PlayerState getPlayerState(String nickname) {
    int i = 0;
    while (i < players.size() && !players.get(i)
      .getNickname()
      .equals(nickname)) {
      i++;
    }

    if (i >= players.size()) throw new PlayerNotFoundException(nickname);

    if (i == currentPlayer) return PlayerState.PLAYING;
    return PlayerState.WAITING;
  }

  /**
   * Gets the scoreboard of the game.
   *
   * @return The scoreboard of the game.
   */
  public Map<String, Integer> getScoreBoard() {
    Map<String, Integer> scoreBoard = new HashMap<>();
    for (Player player : players) {
      scoreBoard.put(player.getNickname(), player.getPoints());
    }
    return scoreBoard;
  }

  /**
   * Gets the current player.
   *
   * @return The current player.
   */

  public Player getCurrentPlayer() {
    return this.players.get(currentPlayer);
  }


  /**
   * Adds a player to the game.
   *
   * @param player The player to add.
   */
  public void addPlayer(Player player) {
    this.players.add(player);
  }


  /**
   * Advances the game to the next turn.
   *
   * <p>This method increments the turn to the next player in the sequence.
   * If the game is already over, it throws a {@link GameOverException}.
   * After each turn, it checks if the game should end based on either
   * reaching the maximum number of rounds or a player reaching the winning
   * points threshold. If either condition is met, the game state is updated
   * to {@link GameState#GAME_OVER} and a {@link GameOverException} is thrown.
   *
   * @throws GameOverException If the game is already over.
   */
  public void nextTurn() throws GameOverException {

    if (this.state == GameState.GAME_OVER) throw new GameOverException();
    currentPlayer = (currentPlayer + 1) % players.size();
    if (this.currentPlayer == 0 && this.remainingRounds != null) {
      this.remainingRounds--;
      if (this.remainingRounds == 0) {
        this.state = GameState.GAME_OVER;
        for (Player player : players) {
          player.evaluateSecretObjective();
          CardPair<ObjectiveCard> objectiveCards =
            gameBoard.getObjectiveCards();
          player.evaluate(objectiveCards.getFirst());
          player.evaluate(objectiveCards.getSecond());
        }
        throw new GameOverException();
      }
    }
    if (this.players.get(currentPlayer).getPoints() >= Game.WINNING_POINTS) {
      this.state = GameState.GAME_OVER;
      throw new GameOverException();
    }

  }


  /**
   * Checks if the game is over.
   * @return True if the game is over, otherwise false.
   */
  public Boolean isGameOver() {
    return this.state == GameState.GAME_OVER;
  }

  /**
   * Sets the game to be over.
   */
  public void setGameOver() {
    this.state = GameState.GAME_OVER;
  }

  /**
   * Gets the remaining rounds in the game.
   * @return The remaining rounds in the game, if any.
   */
  public Optional<Integer> getRemainingRounds() {
    if (this.remainingRounds == null) return Optional.empty();
    return Optional.of(this.remainingRounds);
  }

  /**
   * Checks if the resource deck is empty.
   * @return True if the resource deck is empty, otherwise false.
   */
  public Boolean isResourceDeckEmpty() {
    return this.gameBoard.resourceCardsLeft() == 0;
  }

  /**
   * Checks if the gold deck is empty.
   * @return True if the gold deck is empty, otherwise false.
   */
  public Boolean isGoldDeckEmpty() {
    return this.gameBoard.goldCardsLeft() == 0;
  }

  /**
   * Checks if both decks are empty.
   * @return True if both decks are empty, otherwise false.
   */
  public Boolean areDecksEmpty() {
    return this.isResourceDeckEmpty() && this.isGoldDeckEmpty();
  }


  /**
   * Draws a card from the current player's deck.
   * @param deckType The type of deck to draw from.
   * @throws EmptyDeckException If the deck being drawn from is empty.
   * @throws GameOverException If the game is over.
   */
  public void drawCurrentPlayerCardFromDeck(DrawingDeckType deckType)
  throws EmptyDeckException, GameOverException {
    if (this.state == GameState.GAME_OVER) throw new GameOverException();
    try {
      PlayableCard card;
      if (deckType == DrawingDeckType.RESOURCE) {
        card = this.gameBoard.drawResourceCardFromDeck();
      } else {
        card = this.gameBoard.drawGoldCardFromDeck();
      }
      this.players.get(this.currentPlayer).drawCard(card);
    } catch (EmptyDeckException e) {
      if (this.remainingRounds == null) {
        this.remainingRounds = 2;

      }
      throw e;
    }
  }

  /**
   * Draws a card from a player's deck pair.
   * @param deckType The type of deck to draw from.
   * @param first Whether to draw from the first or second card of the pair.
   * @return The drawn card.
   * @throws EmptyDeckException If the deck being drawn from is empty.
   * @throws GameOverException If the game is over.
   */
  public PlayableCard drawPlayerCardFromPair(DrawingDeckType deckType,
                                             boolean first)
  throws EmptyDeckException, GameOverException {
    if (this.state == GameState.GAME_OVER) {
      throw new GameOverException();
    }
    try {
      if (deckType == DrawingDeckType.RESOURCE) {
        return this.gameBoard.drawResourceCardFromPair(first);
      } else {
        return this.gameBoard.drawGoldCardFromPair(first);
      }

    } catch (EmptyDeckException e) {
      if (this.remainingRounds == null) {
        this.remainingRounds = 2;
      }
      throw e;
    }
  }


  /**
   * Gets the order of players. (used for tests)
   * @return The order of players.
   */
  protected List<String> getPlayersOrder() {
    return this.players.stream().map(Player::getNickname).toList();
  }

  /**
   * Draws a starter card from the deck.
   * @return The starter card drawn from the deck.
   * @throws EmptyDeckException If the deck being drawn from is empty.
   */
  public PlayableCard drawStarterCard()
  throws EmptyDeckException {
    return this.gameBoard.drawStarterCardFromDeck();
  }

}
