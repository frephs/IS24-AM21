package polimi.ingsw.am21.codex.model.Cards;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Playable.ResourceCardFrontSide;
import polimi.ingsw.am21.codex.model.PlayerBoardTest;

class ResourceCardFrontSideTest {

  @Test
  void getEvaluator() {
    PlayerBoardTest pbt = new PlayerBoardTest();
    pbt.externalSetup();

    List<Integer> testIntegers = List.of(0, 123, Integer.MAX_VALUE);

    testIntegers.forEach(points -> {
      ResourceCardFrontSide a = new ResourceCardFrontSide(points);

      assertEquals(points, a.getEvaluator().apply(pbt.pb, 789));
    });
  }
}
