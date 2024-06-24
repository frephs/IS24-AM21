package polimi.ingsw.am21.codex.client.localModel;

import java.util.*;
import java.util.stream.Collectors;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

/**
 * Class that represents the local lobby.
 * It is included in the local model container class
 * Once a player decides to join a game from the game menu, a local lobby  associated to that game is created.
 * The lobby keeps track of the players that joined the game, the available tokens, the available objectives and the starter card.
 * @see LocalModelContainer
 * */
public class LocalLobby {

  /**
   * The pair of available objective cards the player can choose from during their process of joining the game
   * */
  private CardPair<Card> availableObjectives;

  /**
   * The available starter card the player has to choose the side to play of during the process of joining the game.
   * */
  private Card starterCard;

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

  public Card getStarterCard() {
    return starterCard;
  }

  public void setStarterCard(Card starterCard) {
    this.starterCard = starterCard;
  }

  public Map<UUID, LocalPlayer> getPlayers() {
    return players;
  }
}
