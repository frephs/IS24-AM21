package polimi.ingsw.am21.codex.client.localModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Chat.Chat;

/**
 * Class that represents the local game board
 * It is included in the local model container class
 * It is used to store the information about the current game the player is in for view drawing purposes only.
 * It includes the current status of the current game the player's in,  such as
 * <ul>
 * <li> the common game board</li>
 * <li> the players information including their personal board</li>
 * <li> information about the game status, such as the current player index</li>
 * <li> the chat</li>
 * <li> the remaining rounds</li>
 * <li> the index of player associated with the client</li>
 * <li> the secret objective of the player associated with the client</li>
 * </ul>
 * @see LocalModelContainer
 * @see polimi.ingsw.am21.codex.client.localModel.LocalPlayer
 * */
public class LocalGameBoard {

  /**
   * The unique identifier of the game the player joined
   */
  private final String gameId;

  /**
   * The common gold card pair available in the gameboard
   */
  private CardPair<Card> goldCards;

  /**
   * The resource cards in the game
   */
  private CardPair<Card> resourceCards;

  /**
   * The objective cards in the game
   */
  private CardPair<Card> objectiveCards;

  /**
   * The top card of the resource deck
   */
  private PlayableCard resourceDeckTopCard;

  /**
   * The top card of the gold deck
   */
  private PlayableCard goldDeckTopCard;

  /**
   * The secret objective of the player
   */
  private Card secretObjective;

  /**
   * The chat of the game
   */
  private final Chat chat = new Chat();

  /**
   * The index of the current player
   */
  private Integer currentPlayerIndex;

  /**
   * The number of remaining rounds in the game
   */
  private Integer remainingRounds;

  /**
   * The index of the player associated with the client
   */
  private Integer playerIndex;
  private Boolean halted;

  /**
   * Constructor for the LocalGameBoard class
   * @param gameId The unique identifier of the game
   * @param maxPlayers The maximum number of players in the game
   */
  public LocalGameBoard(String gameId, Integer maxPlayers) {
    this.gameId = gameId;
    this.players = new ArrayList<>(maxPlayers);
  }

  /**
   * Gets the player that is currently playing their turn.
   */
  public LocalPlayer getCurrentPlayer() {
    return players.get(currentPlayerIndex);
  }

  public void setCurrentPlayerIndex(Integer currentPlayerIndex) {
    this.currentPlayerIndex = currentPlayerIndex;
  }

  public Optional<LocalPlayer> getPlayerByNickname(String nickname) {
    return players
      .stream()
      .filter(player -> player.getNickname().equals(nickname))
      .findFirst();
  }

  /**
   * The list of local players in the game
   */
  private final List<LocalPlayer> players;

  public String getGameId() {
    return this.gameId;
  }

  /**
   * Gets the local player associated with the client
   */
  public LocalPlayer getPlayer() {
    return players.get(playerIndex);
  }

  /**
   * Gets the nickname of the local player associated with the client
   * @return
   */
  public String getPlayerNickname() {
    return getPlayer().getNickname();
  }

  /**
   * Sets the index of the local player associated with the client, referring to
   * the list of players in the game
   */
  public void setPlayerIndex(int playerIndex) {
    this.playerIndex = playerIndex;
  }

  public Card getSecretObjective() {
    return secretObjective;
  }

  public void setSecretObjective(Card secretObjective) {
    this.secretObjective = secretObjective;
  }

  public CardPair<Card> getResourceCards() {
    return resourceCards;
  }

  public void setResourceCards(CardPair<Card> resourceCards) {
    this.resourceCards = resourceCards;
  }

  public CardPair<Card> getObjectiveCards() {
    return objectiveCards;
  }

  public void setObjectiveCards(CardPair<Card> objectiveCards) {
    this.objectiveCards = objectiveCards;
  }

  public CardPair<Card> getGoldCards() {
    return goldCards;
  }

  public void setGoldCards(CardPair<Card> goldCards) {
    this.goldCards = goldCards;
  }

  public List<LocalPlayer> getPlayers() {
    return players;
  }

  public void setRemainingRounds(Integer remainingRounds) {
    this.remainingRounds = remainingRounds;
  }

  public int getRemainingRounds() {
    return remainingRounds;
  }

  public Chat getChat() {
    return chat;
  }

  /**
   * Gets the player that is supposed to play next (assuming the game is not ending)
   */
  public LocalPlayer getNextPlayer() {
    return players.get((currentPlayerIndex + 1) % players.size());
  }

  public PlayableCard getResourceDeckTopCard() {
    return resourceDeckTopCard;
  }

  public void setResourceDeckTopCard(PlayableCard resourceDeckTopCard) {
    this.resourceDeckTopCard = resourceDeckTopCard;
  }

  public PlayableCard getGoldDeckTopCard() {
    return goldDeckTopCard;
  }

  public void setGoldDeckTopCard(PlayableCard goldDeckTopCard) {
    this.goldDeckTopCard = goldDeckTopCard;
  }

  public void gameHalted() {
    this.halted = true;
  }

  public void gameResumed() {
    this.halted = false;
  }

  public Boolean isHalted() {
    return this.halted;
  }
}
