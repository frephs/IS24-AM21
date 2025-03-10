package polimi.ingsw.am21.codex.model;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.json.JSONArray;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException;
import polimi.ingsw.am21.codex.controller.exceptions.NotEnoughPlayersConnectedException;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Chat.Chat;
import polimi.ingsw.am21.codex.model.GameBoard.*;
import polimi.ingsw.am21.codex.model.Lobby.Lobby;
import polimi.ingsw.am21.codex.model.Player.Player;
import polimi.ingsw.am21.codex.model.Player.PlayerState;
import polimi.ingsw.am21.codex.model.exceptions.*;

public class Game {

  static final int WINNING_POINTS = 20;
  private final List<Player> players;
  private Set<Integer> disconnectedPlayers;
  private final GameBoard gameBoard;
  private final Lobby lobby;
  private GameState state;
  private Integer remainingRounds = null;
  Integer currentPlayer;
  private final Integer maxPlayers;
  private final Chat chat = new Chat();

  public Game(int players) {
    this(players, new GameBoard(new CardsLoader()));
  }

  public Game(int players, JSONArray cards) {
    this(players, new GameBoard(new CardsLoader(cards)));
  }

  private Game(int players, GameBoard gameBoard) {
    this.state = GameState.GAME_INIT;
    this.lobby = new Lobby(players);
    this.gameBoard = gameBoard;
    this.players = new ArrayList<>();
    this.maxPlayers = players;
    this.disconnectedPlayers = new HashSet<>();
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
    this.currentPlayer = 0;
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
   * @throws PlayerNotFoundGameException If the player is not found.
   */
  public PlayerState getPlayerState(String nickname)
    throws PlayerNotFoundGameException {
    int i = 0;
    while (
      i < players.size() && !players.get(i).getNickname().equals(nickname)
    ) {
      i++;
    }

    if (i >= players.size()) throw new PlayerNotFoundGameException(nickname);

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
  public Player getPlayer(String nickname) throws PlayerNotFoundGameException {
    return this.players.stream()
      .filter(player -> Objects.equals(player.getNickname(), nickname))
      .findFirst()
      .orElseThrow(() -> new PlayerNotFoundGameException(nickname));
  }

  /**
   * Gets the players in the game.
   */
  public List<Player> getPlayers() {
    return this.players;
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

  public Boolean isGameHalted() {
    return getPlayersCount() - disconnectedPlayers.size() < 2;
  }

  private void checkGameHalted() throws NotEnoughPlayersConnectedException {
    if (isGameHalted()) throw new NotEnoughPlayersConnectedException();
  }

  /**
   * Draws the player card and runs nextTurnExecute();
   *
   * @param drawingSource      Where we are drawing the card rom
   * @param deckType           The type of deck to draw from.
   * @param nextTurnEventAfterDraw Callback to be called after the cards are
   *                               drawn: the first parameter is the id of the card drawn
   *                               by the player, the second is from the one drawn for
   *                               the card pair (if any, null otherwise).
   * @param nextTurnWithoutDraw  Callback to be called if the player cannot draw a card
   *                             (e.g. because the deck is empty).
   * @param remainingRoundsChange Callback triggered if the number of remaining rounds changes (so when the next (or current) round  will be the last one
   * @throws GameOverException If the game is over.
   */
  public void nextTurn(
    DrawingCardSource drawingSource,
    DrawingDeckType deckType,
    BiConsumer<Integer, Integer> nextTurnEventAfterDraw,
    Runnable nextTurnWithoutDraw,
    Consumer<Integer> remainingRoundsChange
  ) throws InvalidActionException {
    checkGameHalted();
    AtomicReference<Integer> pairCardId = new AtomicReference<>();

    if (this.state == GameState.GAME_OVER) {
      throw new GameOverException();
    }
    if (this.remainingRounds != null) {
      throw new InvalidNextTurnCallException();
    }

    Runnable nextTurnEvent = nextTurnWithoutDraw;

    try {
      PlayableCard playerCard =
        this.gameBoard.drawCard(
            drawingSource,
            deckType,
            replacementCard -> pairCardId.set(replacementCard.getId())
          );

      this.players.get(this.currentPlayer).drawCard(playerCard);
      nextTurnEvent = () ->
        nextTurnEventAfterDraw.accept(playerCard.getId(), pairCardId.get());
    } catch (EmptyDeckException e) {
      this.remainingRounds = 2;
      remainingRoundsChange.accept(this.remainingRounds);
      this.nextTurnExecute(nextTurnEvent, remainingRoundsChange);

      throw e;
    }
    this.nextTurnExecute(nextTurnEvent, remainingRoundsChange);
  }

  /**
   * runs nextTurnExecute()
   *
   * @param remainingRoundsChange Callback triggered if the number of remaining rounds changes (so when the next (or current) round  will be the last one
   * @throws GameOverException If the game is over.
   */
  public void nextTurn(
    Runnable nextTurnWithoutDraw,
    Consumer<Integer> remainingRoundsChange
  ) throws InvalidActionException {
    checkGameHalted();
    if (this.state == GameState.GAME_OVER) {
      throw new GameOverException();
    }
    if (this.remainingRounds == null) {
      throw new InvalidNextTurnCallException();
    }

    this.nextTurnExecute(nextTurnWithoutDraw, remainingRoundsChange);
  }

  private void handleGameOver() throws GameOverException {
    this.state = GameState.GAME_OVER;
    for (Player player : players) {
      player.evaluateSecretObjective();
      CardPair<ObjectiveCard> objectiveCards = gameBoard.getObjectiveCards();
      player.evaluate(objectiveCards.getFirst());
      player.evaluate(objectiveCards.getSecond());
    }
    throw new GameOverException();
  }

  private int getPlayerIndex(String player) throws PlayerNotFoundGameException {
    int playerIndex = getPlayers()
      .stream()
      .map(Player::getNickname)
      .toList()
      .indexOf(player);
    if (playerIndex == -1) throw new PlayerNotFoundGameException(player);
    return playerIndex;
  }

  public void playerDisconnected(String player)
    throws PlayerNotFoundGameException {
    disconnectedPlayers.add(getPlayerIndex(player));
  }

  public void playerReconnected(String player)
    throws PlayerNotFoundGameException {
    disconnectedPlayers.remove(getPlayerIndex(player));
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
   * @param remainingRoundsChange Callback triggered if the number of remaining rounds changes (so when the next (or current) round  will be the last one
   *
   * @throws GameOverException            If the game is already over.
   * @throws InvalidNextTurnCallException if nextTurn is called without the
   *                                      drawing deck information, and it is
   *                                      not the last round
   */
  private void nextTurnExecute(
    Runnable nextTurnEvent,
    Consumer<Integer> remainingRoundsChange
  ) throws InvalidActionException {
    if (this.state == GameState.GAME_OVER) throw new GameOverException();
    if (
      this.players.get(currentPlayer).getPoints() >= Game.WINNING_POINTS
    ) handleGameOver();
    this.getCurrentPlayer().resetCardPlaced();
    do {
      checkGameHalted();
      currentPlayer = (currentPlayer + 1) % players.size();
      boolean remainingRoundsChanged = false;
      if (this.currentPlayer == 0) {
        if (this.remainingRounds != null) {
          this.remainingRounds--;
          remainingRoundsChanged = true;
          if (this.remainingRounds == 0) handleGameOver();
        }
      }
      if (remainingRoundsChanged) remainingRoundsChange.accept(
        this.remainingRounds
      );
    } while (disconnectedPlayers.contains(currentPlayer));

    if (nextTurnEvent != null) nextTurnEvent.run();
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
    return this.remainingRounds != null && this.remainingRounds == 1;
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

  public CardPair<ObjectiveCard> getObjectiveCards() {
    return this.gameBoard.getObjectiveCards();
  }

  public CardPair<PlayableCard> getResourceCards() {
    return this.gameBoard.getResourceCards();
  }

  public CardPair<PlayableCard> getGoldCards() {
    return this.gameBoard.getGoldCards();
  }

  public Chat getChat() {
    return chat;
  }

  public GameBoard getGameBoard() {
    return gameBoard;
  }
}
