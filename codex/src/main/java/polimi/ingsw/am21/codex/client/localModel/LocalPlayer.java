package polimi.ingsw.am21.codex.client.localModel;

import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class LocalPlayer {

  private String nickname;
  private final TokenColor token;
  private int points;
  private List<Card> hand;

  private final Map<ResourceType, Integer> resources = new EnumMap<>(
    ResourceType.class
  );
  private final Map<ObjectType, Integer> objects = new EnumMap<>(
    ObjectType.class
  );

  private Map<Position, Pair<Card, CardSideType>> playedCards;

  public LocalPlayer(TokenColor token) {
    points = 0;
    this.token = token;

    Arrays.stream(ResourceType.values()).forEach(
      resourceType -> resources.put(resourceType, 0)
    );

    Arrays.stream(ObjectType.values()).forEach(
      objectType -> objects.put(objectType, 0)
    );
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }
  public void setHand(List<Card> hand) {
    this.hand = hand;
  }

  public List<Card> getHand() {
    return hand;
  }

  public String getNickname() {
    return nickname;
  }

  public TokenColor getToken() {
    return token;
  }

  public void setPoints(int points) {
    this.points = points;
  }

  public int getPoints() {
    return points;
  }

  public void addPlayedCards(Card card, CardSideType side, Position position) {
    this.playedCards.put(position, new Pair<>(card, side));
  }

  public Map<Position, Pair<Card, CardSideType>> getPlayedCards() {
    return this.playedCards;
  }

  public void addResource(ResourceType resourceType, Integer amount) {
    this.resources.put(resourceType, amount);
  }

  public Map<ResourceType, Integer> getResources() {
    return this.resources;
  }

  public void addObjects(ObjectType objectType, Integer amount) {
    this.objects.put(objectType, amount);
  }

  public Map<ObjectType, Integer> getObjects() {
    return this.objects;
  }
}
