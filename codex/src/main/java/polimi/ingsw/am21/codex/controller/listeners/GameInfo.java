package polimi.ingsw.am21.codex.controller.listeners;

import java.io.Serializable;
import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardIdPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class GameInfo implements Serializable {

  /**
   * Represents a player in a game
   */
  public static class GameInfoUser implements Serializable {

    /**
     * The nickname of the player
     */
    private final String nickname;
    /**
     * The token color of the player
     */
    private final TokenColor tokenColor;
    /**
     * The connection ID of the player
     */
    private final UUID connectionID;
    /**
     * The connection status of the player
     */
    private final GameController.UserGameContext.ConnectionStatus connectionStatus;
    /**
     * A map where the keys are the position of the cards and the values are pairs
     * containing the card ID and the side that has been played
     */
    private final Map<Position, Pair<Integer, CardSideType>> playedCards;
    /**
     * The IDs of the cards in the player's hand
     */
    private final List<Integer> handIDs;
    /**
     * The number of points the player has
     */
    private final Integer points;
    /**
     * The ID of the player's secret objective card
     */
    private final Integer secretObjectiveCard;
    /**
     * The positions in which the player can currently play a card
     */
    private final Set<Position> availableSpots;
    /**
     * The positions in which the player cannot currently play a card because they
     * are locked by a non-available corner in one of the neighboring cards
     */
    private final Set<Position> forbiddenSpots;
    /**
     * The internal index of the player, compared to the list of all players
     */
    private final Integer index;
    /**
     * The counts of the resources of the player
     */
    private final Map<ResourceType, Integer> resources;
    /**
     * The counts of the objects of the player
     */
    private final Map<ObjectType, Integer> objects;

    public GameInfoUser(
      String nickname,
      TokenColor tokenColor,
      UUID connectionID,
      GameController.UserGameContext.ConnectionStatus connectionStatus,
      Map<Position, Pair<Integer, CardSideType>> playedCards,
      List<Integer> handIDs,
      Integer points,
      Integer secretObjectiveCard,
      Set<Position> availableSpots,
      Set<Position> forbiddenSpots,
      Integer index,
      Map<ResourceType, Integer> resources,
      Map<ObjectType, Integer> objects
    ) {
      this.nickname = nickname;
      this.tokenColor = tokenColor;
      this.connectionID = connectionID;
      this.connectionStatus = connectionStatus;
      this.playedCards = playedCards;
      this.handIDs = handIDs;
      this.points = points;
      this.secretObjectiveCard = secretObjectiveCard;
      this.availableSpots = availableSpots;
      this.forbiddenSpots = forbiddenSpots;
      this.index = index;
      this.resources = resources;
      this.objects = objects;
    }

    public String getNickname() {
      return nickname;
    }

    public TokenColor getTokenColor() {
      return this.tokenColor;
    }

    public UUID getConnectionID() {
      return this.connectionID;
    }

    public GameController.UserGameContext.ConnectionStatus getConnectionStatus() {
      return connectionStatus;
    }

    public Map<Position, Pair<Integer, CardSideType>> getPlayedCards() {
      return playedCards;
    }

    public List<Integer> getHandIDs() {
      return handIDs;
    }

    public Integer getPoints() {
      return points;
    }

    public Optional<Integer> getSecretObjectiveCard() {
      return Optional.ofNullable(secretObjectiveCard);
    }

    public Set<Position> getAvailableSpots() {
      return availableSpots;
    }

    public Set<Position> getForbiddenSpots() {
      return forbiddenSpots;
    }

    public Integer getIndex() {
      return index;
    }

    public Map<ResourceType, Integer> getResources() {
      return resources;
    }

    public Map<ObjectType, Integer> getObjects() {
      return objects;
    }
  }

  /**
   * The ID of the game
   */
  private final String gameId;
  /**
   * The list of players in the game
   */
  private final List<GameInfoUser> users;
  /**
   * The index of the player who is currently playing
   */
  private final Integer currentUserIndex;
  /**
   * The number of rounds remaining in the game
   */
  private final Integer remainingRounds;
  /**
   * The IDs of the objective cards on the player board
   */
  private final CardIdPair objectiveCards;
  /**
   * The IDs of the resource cards on the player board
   */
  private final CardIdPair resourceCards;
  /**
   * The IDs of the gold cards on the player board
   */
  private final CardIdPair goldCards;
  /**
   * The ID of the resource card on the top of the deck
   */
  private final Optional<Integer> resourceDeckTopCardId;
  /**
   * The ID of the gold card on the top of the deck
   */
  private final Optional<Integer> goldDeckTopCardId;

  public GameInfo(
    String gameId,
    List<GameInfoUser> users,
    Integer currentUserIndex,
    Integer remainingRounds,
    CardIdPair objectiveCards,
    CardIdPair resourceCards,
    CardIdPair goldCards,
    Optional<Integer> resourceDeckTopCardId,
    Optional<Integer> goldDeckTopCardId
  ) {
    this.gameId = gameId;
    this.users = users;
    this.currentUserIndex = currentUserIndex;
    this.remainingRounds = remainingRounds;
    this.objectiveCards = objectiveCards;
    this.resourceCards = resourceCards;
    this.goldCards = goldCards;
    this.resourceDeckTopCardId = resourceDeckTopCardId;
    this.goldDeckTopCardId = goldDeckTopCardId;
  }

  public GameInfo(
    String gameId,
    List<GameInfoUser> users,
    Integer currentUserIndex,
    Integer remainingRounds,
    CardPair<ObjectiveCard> objectiveCards,
    CardPair<PlayableCard> resourceCards,
    CardPair<PlayableCard> goldCards,
    Optional<Integer> resourceDeckTopCardId,
    Optional<Integer> goldDeckTopCardId
  ) {
    this(
      gameId,
      users,
      currentUserIndex,
      remainingRounds,
      CardIdPair.fromCardPair(objectiveCards),
      CardIdPair.fromCardPair(resourceCards),
      CardIdPair.fromCardPair(goldCards),
      resourceDeckTopCardId,
      goldDeckTopCardId
    );
  }

  public String getGameId() {
    return gameId;
  }

  public List<GameInfoUser> getUsers() {
    return users;
  }

  public Integer getCurrentUserIndex() {
    return currentUserIndex;
  }

  public GameInfoUser getCurrentUser() {
    return users.get(currentUserIndex);
  }

  public CardIdPair getObjectiveCards() {
    return objectiveCards;
  }

  public CardIdPair getResourceCards() {
    return resourceCards;
  }

  public CardIdPair getGoldCards() {
    return goldCards;
  }

  public Optional<Integer> getRemainingRounds() {
    return Optional.ofNullable(remainingRounds);
  }

  public Optional<Integer> getResourceDeckTopCardId() {
    return resourceDeckTopCardId;
  }

  public Optional<Integer> getGoldDeckTopCardId() {
    return goldDeckTopCardId;
  }
}
