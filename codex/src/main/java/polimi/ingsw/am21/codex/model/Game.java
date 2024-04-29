package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.GameBoard.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.json.JSONArray;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.PlayerNotFoundException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;
import polimi.ingsw.am21.codex.model.Lobby.Lobby;
import polimi.ingsw.am21.codex.model.Player.Player;
import polimi.ingsw.am21.codex.model.Player.PlayerState;

public class Game {
  static final int WINNING_POINTS = 20;
  private final List<Player> players;
  private final GameBoard gameBoard;
  private Lobby lobby;
  private GameState state;
  private Integer remainingRounds = null;
  Integer currentPlayer;

  public Game(int players) {
    this.lobby = new Lobby();
    this.state = GameState.GAME_INIT;
    this.lobby = new Lobby(players);

    String jsonLocation = "src/main/java/polimi/ingsw/am21/codex/model/Cards" +
      "/Resources/cards.json";
    File file = new File(jsonLocation);
    JSONArray cards;
    try {
      String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
      cards = new JSONArray(content);
      this.gameBoard = GameBoard.fromJSON(cards);
      this.players = new ArrayList<>();
    } catch (IOException e) {
      throw new RuntimeException("Failed loading cards json");
    }
  }

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
   * Gets the index of current player.
   *
   * @return The index of current player.
   */

  public Integer getCurrentPlayerIndex() {
    return this.currentPlayer;
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
   * Checks if the game is over.
   *
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
   *
   * @return The remaining rounds in the game, if any.
   */
  public Optional<Integer> getRemainingRounds() {
    if (this.remainingRounds == null) return Optional.empty();
    return Optional.of(this.remainingRounds);
  }

  /**
   * Checks if the resource deck is empty.
   *
   * @return True if the resource deck is empty, otherwise false.
   */
  public Boolean isResourceDeckEmpty() {
    return this.gameBoard.resourceCardsLeft() == 0;
  }

  /**
   * Checks if the gold deck is empty.
   *
   * @return True if the gold deck is empty, otherwise false.
   */
  public Boolean isGoldDeckEmpty() {
    return this.gameBoard.goldCardsLeft() == 0;
  }

  /**
   * Checks if one of the decks is empty
   *
   * @return True if one of the decks is empty
   */
  public Boolean areDecksEmpty() {
    return this.isResourceDeckEmpty() || this.isGoldDeckEmpty();
  }

  /**
   * Draws the player card and runs nextTurn();
   *
   * @param drawingSource Where we are drawing the card rom
   * @param deckType      The type of deck to draw from.
   * @throws GameOverException If the game is over.
   */
  public void nextTurn(DrawingCardSource drawingSource,
                       DrawingDeckType deckType)
  throws GameOverException, EmptyDeckException, InvalidNextTurnCallException {
    if (this.state == GameState.GAME_OVER) {
      throw new GameOverException();
    }
    if (this.remainingRounds == null) {
      throw new InvalidNextTurnCallException();
    }
    try {
      this.players.get(this.currentPlayer).drawCard(this.gameBoard.drawCard(
        drawingSource,
        deckType
      ));
    } catch (EmptyDeckException e) {
      this.remainingRounds = 2;
      throw e;
    }
    this.nextTurn();
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
   * @throws GameOverException            If the game is already over.
   * @throws InvalidNextTurnCallException if nextTurn is called without the
   *                                      drawing deck information, and it is
   *                                      not the last round
   */
  public void nextTurn()
  throws GameOverException, InvalidNextTurnCallException {
    if (this.state == GameState.GAME_OVER) throw new GameOverException();
    if (this.players.get(currentPlayer).getPoints() >= Game.WINNING_POINTS) {
      this.state = GameState.GAME_OVER;
      throw new GameOverException();
    }
    if (this.remainingRounds == null) throw new InvalidNextTurnCallException();
    currentPlayer = (currentPlayer + 1) % players.size();
    if (this.currentPlayer == 0) {
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
  }

  /**
   * Gets the order of players. (used for tests)
   *
   * @return The order of players.
   */
  protected List<String> getPlayersOrder() {
    return this.players.stream().map(Player::getNickname).toList();
  }

  /**
   * Draws a starter card from the deck.
   *
   * @return The starter card drawn from the deck.
   * @throws EmptyDeckException If the deck being drawn from is empty.
   */
  public PlayableCard drawStarterCard()
  throws EmptyDeckException {
    return this.gameBoard.drawStarterCardFromDeck();
  }

  public void insertObjectiveCard(ObjectiveCard objectiveCard) {
    this.gameBoard.insertObjectiveCard(objectiveCard);
  }

  public void insertStarterCard(PlayableCard starterCard) {
    this.gameBoard.insertStarterCard(starterCard);
  }

  public Boolean isLastRound() {
    return this.remainingRounds == 1;
  }

  public List<PlayableCard> drawHand() {
    List<PlayableCard> hand = new ArrayList<>();
    try {
      hand.add(this.gameBoard.drawGoldCardFromDeck());
      hand.add(this.gameBoard.drawResourceCardFromDeck());
      hand.add(this.gameBoard.drawResourceCardFromDeck());
    } catch (EmptyDeckException ignored) {
    }
    return hand;
  }
}
