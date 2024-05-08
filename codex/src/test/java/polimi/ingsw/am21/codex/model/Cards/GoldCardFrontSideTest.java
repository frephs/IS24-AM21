package polimi.ingsw.am21.codex.model.Cards;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Objectives.PointConditionType;
import polimi.ingsw.am21.codex.model.Cards.Playable.*;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.model.PlayerBoardTest;

class GoldCardFrontSideTest {

  @Test
  void getEvaluator()
    throws IllegalCardSideChoiceException, IllegalPlacingPositionException {
    PlayerBoardTest pbt = new PlayerBoardTest();
    pbt.externalSetup();
    PlayerBoard pb = pbt.pb;

    GoldCardFrontSide sideWithoutCondition = new GoldCardFrontSide(
      123,
      List.of(ResourceType.PLANT, ResourceType.FUNGI),
      null,
      null
    );
    assertEquals(123, sideWithoutCondition.getEvaluator().apply(pb, 0));

    GoldCardFrontSide sideWithObjectCondition = new GoldCardFrontSide(
      123,
      List.of(ResourceType.PLANT, ResourceType.FUNGI),
      PointConditionType.OBJECTS,
      ObjectType.QUILL
    );

    ResourceCardFrontSide plantSide = new ResourceCardFrontSide(0);
    plantSide.setCorner(CornerPosition.TOP_LEFT, Optional.of(ObjectType.QUILL));
    PlayableCard plantRes = new PlayableCard(
      123,
      plantSide,
      new PlayableBackSide(List.of())
    );

    assertEquals(0, sideWithObjectCondition.getEvaluator().apply(pb, 0));
    pb.getHand().add(plantRes);
    pb.placeCard(plantRes, CardSideType.FRONT, new Position(0, 1));
    assertEquals(123, sideWithObjectCondition.getEvaluator().apply(pb, 0));
    pb.getHand().add(plantRes);
    pb.placeCard(plantRes, CardSideType.FRONT, new Position(1, 0));
    assertEquals(123 * 2, sideWithObjectCondition.getEvaluator().apply(pb, 0));

    GoldCardFrontSide sideWithCornerCondition = new GoldCardFrontSide(
      123,
      List.of(ResourceType.PLANT, ResourceType.FUNGI),
      PointConditionType.CORNERS,
      null
    );

    assertEquals(0, sideWithCornerCondition.getEvaluator().apply(pb, 0));
    assertEquals(123, sideWithCornerCondition.getEvaluator().apply(pb, 1));
    assertEquals(123 * 2, sideWithCornerCondition.getEvaluator().apply(pb, 2));
  }
}
