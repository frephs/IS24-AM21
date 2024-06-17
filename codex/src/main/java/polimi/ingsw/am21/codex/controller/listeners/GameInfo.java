package polimi.ingsw.am21.codex.controller.listeners;

import java.io.Serializable;
import java.util.*;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardIndexPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class GameInfo implements Serializable {

  public static class GameInfoUser implements Serializable {

    private final String nickname;
    private final TokenColor tokenColor;
    private final UUID socketID;
    private final GameController.UserGameContext.ConnectionStatus connectionStatus;
    Map<Position, Integer> playedCards;
    List<Integer> handIDs;
    Integer points;
    Integer secretObjectiveCard;
    Set<Position> availableSpots;
    Set<Position> forbiddenSpots;
    Integer index;

    public GameInfoUser(
      String nickname,
      TokenColor tokenColor,
      UUID socketID,
      GameController.UserGameContext.ConnectionStatus connectionStatus,
      Map<Position, Integer> playedCards,
      List<Integer> handIDs,
      Integer points,
      Integer secretObjectiveCard,
      Set<Position> availableSpots,
      Set<Position> forbiddenSpots,
      Integer index
    ) {
      this.nickname = nickname;
      this.tokenColor = tokenColor;
      this.socketID = socketID;
      this.connectionStatus = connectionStatus;
      this.playedCards = playedCards;
      this.handIDs = handIDs;
      this.points = points;
      this.secretObjectiveCard = secretObjectiveCard;
      this.availableSpots = availableSpots;
      this.forbiddenSpots = forbiddenSpots;
      this.index = index;
    }

    public String getNickname() {
      return nickname;
    }

    public TokenColor getTokenColor() {
      return this.tokenColor;
    }

    public UUID getSocketID() {
      return this.socketID;
    }

    public GameController.UserGameContext.ConnectionStatus getConnectionStatus() {
      return connectionStatus;
    }

    public Map<Position, Integer> getPlayedCards() {
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
  }

  private final String gameId;
  private final List<GameInfoUser> users;
  private final Integer currentUser;
  private final Integer remainingRounds;
  private final CardIndexPair objectiveCards;
  private final CardIndexPair resourceCards;
  private final CardIndexPair goldCards;

  public GameInfo(
    String gameId,
    List<GameInfoUser> users,
    Integer currentUser,
    Integer remainingRounds,
    CardIndexPair objectiveCards,
    CardIndexPair resourceCards,
    CardIndexPair goldCards
  ) {
    this.gameId = gameId;
    this.users = users;
    this.currentUser = currentUser;
    this.remainingRounds = remainingRounds;
    this.objectiveCards = objectiveCards;
    this.resourceCards = resourceCards;
    this.goldCards = goldCards;
  }

  public GameInfo(
    String gameId,
    List<GameInfoUser> users,
    Integer currentUser,
    Integer remainingRounds,
    CardPair<ObjectiveCard> objectiveCards,
    CardPair<PlayableCard> resourceCards,
    CardPair<PlayableCard> goldCards
  ) {
    this(
      gameId,
      users,
      currentUser,
      remainingRounds,
      CardIndexPair.fromCardPair(objectiveCards),
      CardIndexPair.fromCardPair(resourceCards),
      CardIndexPair.fromCardPair(goldCards)
    );
  }

  public String getGameId() {
    return gameId;
  }

  public List<GameInfoUser> getUsers() {
    return users;
  }

  public Integer getCurrentUserIndex() {
    return currentUser;
  }

  public GameInfoUser getCurrentUser() {
    return users.get(currentUser);
  }

  public CardIndexPair getObjectiveCards() {
    return objectiveCards;
  }

  public CardIndexPair getResourceCards() {
    return resourceCards;
  }

  public CardIndexPair getGoldCards() {
    return goldCards;
  }

  public Optional<Integer> getRemainingRounds() {
    return Optional.ofNullable(remainingRounds);
  }
}
