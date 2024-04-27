package polimi.ingsw.am21.codex.model.Cards;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.PlayerBoard;

class PlayableBackSideTest {

  @Test
  void getPermanentResources() {
    List<ResourceType> resources = new ArrayList<>();
    resources.add(ResourceType.ANIMAL);

    PlayableBackSide a = new PlayableBackSide(resources);
    // Test that the resources are copied correctly (1 element)
    assertEquals(a.getPermanentResources(), resources);

    resources.add(ResourceType.FUNGI);
    // Test that the internal Set is independent of the given reference
    assertEquals(a.getPermanentResources().size(), resources.size() - 1);

    PlayableBackSide b = new PlayableBackSide((resources));
    // Test that the resources are copied correctly (2 elements)
    assertEquals(b.getPermanentResources(), resources);

    resources.add(ResourceType.INSECT);
    PlayableBackSide c = new PlayableBackSide((resources));
    // Test that the resources are copied correctly (3 elements)
    assertEquals(c.getPermanentResources(), resources);
  }

  @Test
  void getEvaluator() {
    List<ResourceType> resources = new ArrayList<>();
    resources.add(ResourceType.ANIMAL);
    PlayableBackSide a = new PlayableBackSide(resources);

    resources.add(ResourceType.PLANT);
    PlayableBackSide b = new PlayableBackSide(resources);

    PlayerBoard pb = new PlayerBoard(
      new ArrayList<>(),
      new PlayableCard(123, null, null),
      new ObjectiveCard(123, 123, null)
    );

    // evaluator should always return 0
    assertEquals(a.getEvaluator().apply(pb, 123), 0);
    assertEquals(b.getEvaluator().apply(pb, 123), 0);
  }
}
