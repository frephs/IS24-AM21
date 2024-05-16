package polimi.ingsw.am21.codex.client.localModel;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class LocalPlayer {

  private String nickname;
  private int points;

  private TokenColor token;

  private EnumMap<ResourceType, Integer> resources;
  private EnumMap<ObjectType, Integer> objects;
  private Map<Position, Card> playedCards;

  public LocalPlayer(String nickname, TokenColor token) {
    this.nickname = nickname;
    points = 0;
    this.token = token;
  }

  public String getNickname() {
    return nickname;
  }

  public TokenColor getToken() {
    return token;
  }

  public void addPoints(int points) {
    this.points = this.points + points;
  }

  public int getPoints() {
    return points;
  }

  public void addPlayedCards(Card card, Position position) {
    this.playedCards.put(position, card);
  }

  public Map<Position, Card> getPlayedCards() {
    return this.playedCards;
  }

  public void addResource(ResourceType resourceType, Integer amount) {
    this.resources.put(resourceType, amount);
  }

  public EnumMap<ResourceType, Integer> getResources() {
    return this.resources;
  }

  public void addObjects(ObjectType objectType, Integer amount) {
    this.objects.put(objectType, amount);
  }

  public EnumMap<ObjectType, Integer> getObjects() {
    return this.objects;
  }
}
