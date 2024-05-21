package polimi.ingsw.am21.codex.client.localModel;

import java.util.ArrayList;
import java.util.List;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;

public class LocalGameBoard {

  private final String gameId;

  private CardPair<Card> goldCards;
  private CardPair<Card> resourceCards;

  private Card secretObjective;

  private int playerNumber;

  private int currentPlayer;
  /**
   * Index of the player associated with the client
   */
  private int playerIndex;

  public LocalPlayer getCurrentPlayer() {
    return players.get(currentPlayer);
  }

  public int getCurrentPlayerIndex() {
    return currentPlayer;
  }

  public void setCurrentPlayer(int currentPlayer) {
    this.currentPlayer = currentPlayer;
  }

  private final List<LocalPlayer> players;

  public LocalGameBoard(String gameId, int players) {
    this.gameId = gameId;
    this.playerNumber = players;
    this.players = new ArrayList<>(players);
  }

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
    return players.get(playerIndex).getNickname();
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

  public CardPair<Card> getGoldCards() {
    return goldCards;
  }

  public void setGoldCards(CardPair<Card> goldCards) {
    this.goldCards = goldCards;
  }

  public List<LocalPlayer> getPlayers() {
    return players;
  }
}
