package polimi.ingsw.am21.codex.client.localModel;

import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class LocalLobby {

  private final Set<TokenColor> availableTokens = new HashSet<>(4);
  private final Set<String> availableGames = new HashSet<>();

  private final Map<String, Integer> playersPerGame = new HashMap();

  private CardPair<Card> availableObjectives;

  private final List<UUID> players = new LinkedList<>();
  private final Map<UUID, String> nicknames = new HashMap<>();
  private final Map<UUID, TokenColor> tokens = new HashMap<>();
  private String gameId;

  LocalLobby(String gameId) {
    this.gameId = gameId;
  }

  public Map<String, Integer> getPlayersPerGame() {
    return playersPerGame;
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
