package polimi.ingsw.am21.codex.client.localModel;

import java.util.ArrayList;
import java.util.List;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;

public class LocalGameBoard {

  private final String gameId;

  private CardPair<Card> goldCards;
  private CardPair<Card> resourceCards;
  private CardPair<Card> objectiveCards;

  private Card secretObjective;

  private Integer currentPlayerIndex;

  private Integer remainingRounds;
  private Integer maxPlayers;

  private Integer playerIndex;

  public LocalGameBoard(String gameId, Integer maxPlayers) {
    this.gameId = gameId;
    this.maxPlayers = maxPlayers;
    this.players = new ArrayList<>(maxPlayers);
  }

  public LocalPlayer getCurrentPlayer() {
    return players.get(currentPlayerIndex);
  }

  public void setCurrentPlayerIndex(Integer currentPlayerIndex) {
    this.currentPlayerIndex = currentPlayerIndex;
  }

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

  public String getPlayerNickname() {
    return getPlayer().getNickname();
  }

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
}
