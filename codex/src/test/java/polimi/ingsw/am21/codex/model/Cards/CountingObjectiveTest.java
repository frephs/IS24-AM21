package polimi.ingsw.am21.codex.model.Cards;

import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Objectives.CountingObjective;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CountingObjectiveTest {

  @Test
  void getEvaluator() {
    Map<ResourceType, Integer> resources = new HashMap<>();
    resources.put(ResourceType.PLANT, 3);
    Map<ObjectType, Integer> objects = new HashMap<>();
    CountingObjective countingObjective = new CountingObjective(resources, objects);

    PlayerBoard pb = new PlayerBoard(
      new ArrayList<>(),
      new PlayableCard(123, null, null),
      new ObjectiveCard(123, 123, null)
    );

    //evaluator should return 0
    assertEquals(countingObjective.getEvaluator().apply(pb, 123), 0);
  }
}