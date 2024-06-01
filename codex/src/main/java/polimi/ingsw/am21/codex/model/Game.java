package polimi.ingsw.am21.codex.model;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import org.json.JSONArray;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Chat.Chat;
import polimi.ingsw.am21.codex.model.GameBoard.*;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.PlayerNotFoundException;
import polimi.ingsw.am21.codex.model.Lobby.Lobby;
import polimi.ingsw.am21.codex.model.Player.Player;
import polimi.ingsw.am21.codex.model.Player.PlayerState;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

public class Game {

  static final int WINNING_POINTS = 20;
  private final List<Player> players;
  private final GameBoard gameBoard;
  private Lobby lobby;
  private GameState state;
  private Integer remainingRounds = null;
  Integer currentPlayer;
  private final Integer maxPlayers;
  private final Chat chat = new Chat();

  public Game(int players) {
    this.state = GameState.GAME_INIT;
    this.lobby = new Lobby(players);

    this.gameBoard = new GameBoard(new CardsLoader());
    this.players = new ArrayList<>();
    this.maxPlayers = players;
  }

  public Game(int players, JSONArray cards) {
    this.state = GameState.GAME_INIT;
    this.lobby = new Lobby(players);
    this.gameBoard = GameBoard.fromJSON(cards);
    this.players = new ArrayList<>();
    this.maxPlayers = players;
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
  public void start()
    throws GameNotReadyException, GameAlreadyStartedException {
    if (this.getPlayersSpotsLeft() != 0) throw new GameNotReadyException();
    if (
      this.state != GameState.GAME_INIT
    ) throw new GameAlreadyStartedException();

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
    while (
      i < players.size() && !players.get(i).getNickname().equals(nickname)
    ) {
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
   * Gets a player from the nickname
   *
   * @param nickname The nickname of the player you're looking for
   */
  public Player getPlayer(String nickname) throws PlayerNotFoundException {
    return this.players.stream()
      .filter(player -> Objects.equals(player.getNickname(), nickname))
      .findFirst()
      .orElseThrow(() -> new PlayerNotFoundException(nickname));
  }

  /**
   * Gets the players in the game.
   */
  public List<Player> getPlayers() {
    return this.players;
  }

  /**
   * Gets the nicknames of the players in the game.
   */
  public List<String> getPlayerIds() {
    return this.players.stream().map(Player::getNickname).toList();
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
   * @param drawingSource      Where we are drawing the card rom
   * @param deckType           The type of deck to draw from.
   * @param drawnCardsCallback Callback to be called after the cards are
   *                           drawn: the first parameter is the id of the card drawn
   *                           by the player, the second is from the one drawn for
   *                           the card pair (if any, null otherwise).
   * @throws GameOverException If the game is over.
   */
  public void nextTurn(
    DrawingCardSource drawingSource,
    DrawingDeckType deckType,
    BiConsumer<Integer, Integer> drawnCardsCallback
  ) throws GameOverException, EmptyDeckException, InvalidNextTurnCallException {
    int playerCardId;
    AtomicReference<Integer> pairCardId = new AtomicReference<>();

    if (this.state == GameState.GAME_OVER) {
      throw new GameOverException();
    }
    if (this.remainingRounds == null) {
      throw new InvalidNextTurnCallException();
    }
    try {
      PlayableCard playerCard =
        this.gameBoard.drawCard(drawingSource, deckType, replacementCard -> {
            pairCardId.set(replacementCard.getId());
          });
      playerCardId = playerCard.getId();

      this.players.get(this.currentPlayer).drawCard(playerCard);

      drawnCardsCallback.accept(playerCardId, pairCardId.get());
    } catch (EmptyDeckException e) {
      this.remainingRounds = 2;
      throw e;
    }
    this.nextTurn();
  }

  public void nextTurn(
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  ) throws GameOverException, EmptyDeckException, InvalidNextTurnCallException {
    this.nextTurn(drawingSource, deckType, (a, b) -> {});
  }

  /**
   * Advances the game to the next turn.
   * <p>
   * This method increments the turn to the next player in the sequence.
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
    players.get(currentPlayer).toggleCardPlacedThisTurn();
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
  public PlayableCard drawStarterCard() throws EmptyDeckException {
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

  public List<PlayableCard> drawHand() throws EmptyDeckException {
    List<PlayableCard> hand = new ArrayList<>();
    hand.add(this.gameBoard.drawGoldCardFromDeck());
    hand.add(this.gameBoard.drawResourceCardFromDeck());
    hand.add(this.gameBoard.drawResourceCardFromDeck());
    return hand;
  }

  /**
   * @return the number of connected players
   */
  public Integer getPlayersCount() {
    return this.players.size();
  }

  /**
   * @return the number of maximum players
   */
  public Integer getMaxPlayers() {
    return this.maxPlayers;
  }

  public Integer getPlayersSpotsLeft() {
    return this.getMaxPlayers() - this.getPlayersCount();
  }

  public Chat getChat() {
    return chat;
  }
}
