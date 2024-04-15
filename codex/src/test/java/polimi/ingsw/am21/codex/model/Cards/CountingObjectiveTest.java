package polimi.ingsw.am21.codex.model.Cards;

import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class CountingObjectiveTest {

  @Test
  void getEvaluator() {
    HashMap<ResourceType, Integer> resources = new HashMap<>();
    resources.put(ResourceType.PLANT_KINGDOM, 3);
    CountingObjective countingObjective = new CountingObjective(resources, null);

    PlayerBoard pb = new PlayerBoard(
      new ArrayList<>(),
      new PlayableCard(123, null, null),
      new ObjectiveCard(123, 123, null)
    );

    //evaluator should return 0
    assertEquals(countingObjective.getEvaluator().apply(pb, 123), 0);
  }
}