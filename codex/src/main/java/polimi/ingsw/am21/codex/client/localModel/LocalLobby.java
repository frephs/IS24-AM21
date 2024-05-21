package polimi.ingsw.am21.codex.client.localModel;

import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class LocalLobby {

  private final Set<TokenColor> availableTokens = new HashSet<>(4);
  private final Set<String> availableGames = new HashSet<>();

  private Map<String, Integer> playerSlots = new HashMap();
  private Map<String, Integer> maxPlayerSlots = new HashMap();

  private CardPair<Card> availableObjectives;

  private final List<UUID> players = new LinkedList<>();
  private final Map<UUID, String> nicknames = new HashMap<>();
  private final Map<UUID, TokenColor> tokens = new HashMap<>();

  private String gameId;

  LocalLobby() {}

  public Map<String, Integer> getMaxPlayerSlots() {
    return maxPlayerSlots;
  }

  public Map<String, Integer> getPlayerSlots() {
    return playerSlots;
  }

  public Map<UUID, TokenColor> getTokens() {
    return tokens;
  }

  public Map<UUID, String> getNicknames() {
    return nicknames;
  }

  public String getGameId() {
    return gameId;
  }

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  public Set<String> getAvailableGames() {
    return availableGames;
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

  public List<UUID> getPlayers() {
    return players;
  }
}
