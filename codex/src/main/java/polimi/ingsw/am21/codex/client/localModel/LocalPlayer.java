package polimi.ingsw.am21.codex.client.localModel;

import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class LocalPlayer {

  private final UUID socketID;
  private String nickname;
  private TokenColor token;
  private int points;
  private List<Card> hand;

  private GameController.UserGameContext.ConnectionStatus connectionStatus;

  private Card objectiveCard;

  // this is only present if the player is the current player ( these are the 2 objectiva cards )
  private CardPair<ObjectiveCard> objectiveCards;

  private final Map<ResourceType, Integer> resources = new EnumMap<>(
    ResourceType.class
  );
  private final Map<ObjectType, Integer> objects = new EnumMap<>(
    ObjectType.class
  );

  private Set<Position> availableSpots;
  private Set<Position> forbiddenSpots;

  private final Map<Position, Pair<Card, CardSideType>> playedCards =
    new HashMap<>();

  public LocalPlayer(UUID socketID) {
    this.socketID = socketID;

    Arrays.stream(ResourceType.values()).forEach(
      resourceType -> resources.put(resourceType, 0)
    );

    Arrays.stream(ObjectType.values()).forEach(
      objectType -> objects.put(objectType, 0)
    );
  }

  public UUID getSocketID() {
    return socketID;
  }

  public void addPlayedCards(Card card, CardSideType side, Position position) {
    this.playedCards.put(position, new Pair<>(card, side));
  }

  public void addResource(ResourceType resourceType, Integer amount) {
    this.resources.put(resourceType, amount);
  }

  public void addObjects(ObjectType objectType, Integer amount) {
    this.objects.put(objectType, amount);
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public TokenColor getToken() {
    return token;
  }

  public void setToken(TokenColor token) {
    this.token = token;
  }

  public int getPoints() {
    return points;
  }

  public void setPoints(int points) {
    this.points = points;
  }

  public List<Card> getHand() {
    return hand;
  }

  public void setHand(List<Card> hand) {
    this.hand = hand;
  }

  public Card getObjectiveCard() {
    return objectiveCard;
  }

  public void setObjectiveCard(Card objectiveCard) {
    this.objectiveCard = objectiveCard;
  }

  public Map<ResourceType, Integer> getResources() {
    return resources;
  }

  public Map<ObjectType, Integer> getObjects() {
    return objects;
  }

  public Set<Position> getAvailableSpots() {
    return availableSpots;
  }

  public void setAvailableSpots(Set<Position> availableSpots) {
    this.availableSpots = availableSpots;
  }

  public Set<Position> getForbiddenSpots() {
    return forbiddenSpots;
  }

  public void setForbiddenSpots(Set<Position> forbiddenSpots) {
    this.forbiddenSpots = forbiddenSpots;
  }

  public Map<Position, Pair<Card, CardSideType>> getPlayedCards() {
    return playedCards;
  }

  public CardPair<ObjectiveCard> getObjectiveCards() {
    return objectiveCards;
  }

  public void setObjectiveCards(CardPair<ObjectiveCard> objectiveCards) {
    this.objectiveCards = objectiveCards;
  }

  public GameController.UserGameContext.ConnectionStatus getConnectionStatus() {
    return connectionStatus;
  }

  public void setConnectionStatus(
    GameController.UserGameContext.ConnectionStatus connectionStatus
  ) {
    this.connectionStatus = connectionStatus;
  }
}
