package polimi.ingsw.am21.codex.client.localModel;

import java.util.EnumMap;
import java.util.Map;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public class LocalPlayer {

  public String nickname;
  public int points;

  public TokenColor token;

  public EnumMap<ResourceType, Integer> resources;
  public EnumMap<ObjectType, Integer> objects;
  public Map<Position, Card> playedCards;
}
