package polimi.ingsw.am21.codex.model.Cards;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.StarterCardFrontSide;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.model.PlayerBoardTest;

class StarterCardFrontSideTest {

  @Test
  void getEvaluator() {
    PlayerBoardTest pbt = new PlayerBoardTest();
    pbt.externalSetup();

    StarterCardFrontSide a = new StarterCardFrontSide();
    // evaluator should always return 0
    assertEquals(0, a.getEvaluator().apply(pbt.pb, 123));
  }
}
