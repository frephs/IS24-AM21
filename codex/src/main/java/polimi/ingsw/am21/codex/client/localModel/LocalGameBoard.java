package polimi.ingsw.am21.codex.client.localModel;

import java.util.ArrayList;
import java.util.List;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;

public class LocalGameBoard {

  private final String gameId;

  private CardPair<Card> goldCards;
  private CardPair<Card> resourceCards;

  private Card secretObjective;

  /**
   * Number of players the game contains
   * */
  private final int playerNumber;

  private String currentPlayer;

  /**
   * Index of the player associated with the client
   */
  private int myPlayerIndex;
  private int playerIndex;

  public LocalGameBoard(String gameId, int players) {
    this.gameId = gameId;
    this.playerNumber = players;
    this.players = new ArrayList<>(players);
  }

  public LocalPlayer getCurrentPlayer() {
    return players
      .stream()
      .filter(player -> player.getNickname().equals(currentPlayer))
      .findFirst()
      .orElse(null);
  }

  public void setCurrentPlayer(String currentPlayer) {
    this.currentPlayer = currentPlayer;
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
