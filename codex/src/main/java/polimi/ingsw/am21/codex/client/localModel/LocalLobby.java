package polimi.ingsw.am21.codex.client.localModel;

import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class LocalLobby {

  private final Set<TokenColor> availableTokens;

  private CardPair<Card> availableObjectives;

  /**
   * Players of the lobby you chose.
   * */
  private final Map<UUID, LocalPlayer> players = new HashMap<>();

  /**
   * The gameId of the lobby you chose.
   * */
  private final String gameId;

  LocalLobby(String gameId, Set<TokenColor> availableTokens) {
    this.gameId = gameId;
    this.availableTokens = availableTokens;
  }

  public String getGameId() {
    return gameId;
  }

  public Set<TokenColor> getAvailableTokens() {
    return availableTokens;
  }

  public CardPair<Card> getAvailableObjectives() {
    return availableObjectives;
  }

  public void setAvailableObjectives(Card first, Card second) {
    this.availableObjectives = new CardPair<>(first, second);
  }

  public Map<UUID, LocalPlayer> getPlayers() {
    return players;
  }
}
