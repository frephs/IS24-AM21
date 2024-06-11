package polimi.ingsw.am21.codex.client.localModel;

import java.util.*;
import java.util.stream.Collectors;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class LocalLobby {

  private CardPair<Card> availableObjectives;

  private Integer starterCardId;

  /**
   * Players of the lobby you chose.
   * */
  private final Map<UUID, LocalPlayer> players = new HashMap<>();

  /**
   * The gameId of the lobby you chose.
   * */
  private final String gameId;

  LocalLobby(String gameId) {
    this.gameId = gameId;
  }

  public String getGameId() {
    return gameId;
  }

  public Set<TokenColor> getAvailableTokens() {
    return Arrays.stream(TokenColor.values())
      .filter(
        tokenColor ->
          players
            .values()
            .stream()
            .noneMatch(player -> tokenColor.equals(player.getToken()))
      )
      .collect(Collectors.toSet());
  }

  public CardPair<Card> getAvailableObjectives() {
    return availableObjectives;
  }

  public void setAvailableObjectives(Card first, Card second) {
    this.availableObjectives = new CardPair<>(first, second);
  }

  public Integer getStarterCardId() {
    return starterCardId;
  }

  public void setStarterCardId(Integer starterCardId) {
    this.starterCardId = starterCardId;
  }

  public Map<UUID, LocalPlayer> getPlayers() {
    return players;
  }
}
