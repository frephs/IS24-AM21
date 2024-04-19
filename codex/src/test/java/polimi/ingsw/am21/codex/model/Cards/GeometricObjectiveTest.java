package polimi.ingsw.am21.codex.model.Cards;

import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Objectives.GeometricObjective;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static polimi.ingsw.am21.codex.model.Cards.CornerPosition.BOTTOM_LEFT;

class GeometricObjectiveTest {

  @Test
  void getEvaluator() {
    Map<AdjacentPosition, ResourceType> geometry = new HashMap<>();
    geometry.put(BOTTOM_LEFT, ResourceType.FUNGI);
    GeometricObjective geometricObjective = new GeometricObjective(geometry);

    PlayerBoard pb = new PlayerBoard(
      new ArrayList<>(),
      new PlayableCard(123, null, null),
      new ObjectiveCard(123, 123, null)
    );

    //evaluator should return 0
    assertEquals(geometricObjective.getEvaluator().apply(pb, 123), 0);
  }
}