package polimi.ingsw.am21.codex.client.localModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;

public class LocalGameBoard {

  CardsLoader cardsLoader = new CardsLoader();
  private final String gameId;

  private CardPair<Card> goldCards;
  private CardPair<Card> resourceCards;

  private Card secretObjective;
  private List<Card> hand;

  private String playerNickname;

  private final Map<String, LocalPlayer> players = new HashMap<>();

  LocalGameBoard(String gameId) {
    this.gameId = gameId;
  }

  public String getGameId() {
    return this.gameId;
  }

  public String getPlayerNickname() {
    return playerNickname;
  }

  public void setPlayerIndex(String playerNickname) {
    this.playerNickname = playerNickname;
  }

  public List<Card> getHand() {
    return hand;
  }

  public void setHand(List<Card> hand) {
    this.hand = hand;
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

  public CardPair<Card> getGoldCards() {
    return goldCards;
  }

  public void setGoldCards(CardPair<Card> goldCards) {
    this.goldCards = goldCards;
  }

  public Map<String, LocalPlayer> getPlayers() {
    return players;
  }
}
