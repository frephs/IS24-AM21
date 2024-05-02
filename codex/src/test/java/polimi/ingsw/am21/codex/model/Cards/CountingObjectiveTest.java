package polimi.ingsw.am21.codex.model.Cards;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Objectives.CountingObjective;
import polimi.ingsw.am21.codex.model.Cards.Objectives.GeometricObjective;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableBackSide;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.ResourceCardFrontSide;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.model.PlayerBoardTest;

class CountingObjectiveTest {

  @Test
  void getEvaluator()
    throws IllegalCardSideChoiceException, IllegalPlacingPositionException {
    PlayerBoardTest pbt = new PlayerBoardTest();
    pbt.externalSetup();

    Map<ResourceType, Integer> resources = new HashMap<>();
    Map<ObjectType, Integer> objects = new HashMap<>();

    resources.put(ResourceType.PLANT, 1);
    resources.put(ResourceType.INSECT, 1);
    objects.put(ObjectType.QUILL, 1);

    CountingObjective countingObjective = new CountingObjective(
      resources,
      objects
    );

    PlayerBoard pb = new PlayerBoard(
      List.of(pbt.resourceCard, pbt.resourceCard, pbt.resourceCard),
      pbt.starterCard,
      new ObjectiveCard(123, 2, countingObjective)
    );

    pb.placeCard(pbt.resourceCard, CardSideType.BACK, new Position(-1, 0));
    pb.placeCard(pbt.resourceCard, CardSideType.BACK, new Position(1, 0));
    pb.placeCard(pbt.resourceCard, CardSideType.BACK, new Position(2, 0));

    pb.getHand().add(pbt.resourceCard);
    pb.placeCard(pbt.resourceCard, CardSideType.BACK, new Position(3, 0));

    pb.getHand().add(pbt.starterCard);
    pb.placeCard(pbt.starterCard, CardSideType.BACK, new Position(4, 0));

    assertEquals(2 * 2, pb.getObjectiveCard().getEvaluator().apply(pb));

    ResourceCardFrontSide quillSide = new ResourceCardFrontSide(1);
    quillSide.setCorner(
      CornerPosition.BOTTOM_LEFT,
      Optional.of(ObjectType.QUILL)
    );
    PlayableCard quillCard = new PlayableCard(
      123,
      quillSide,
      new PlayableBackSide(List.of())
    );

    pb.getHand().add(quillCard);
    pb.placeCard(quillCard, CardSideType.FRONT, new Position(5, 0));

    assertEquals(2 * 3, pb.getObjectiveCard().getEvaluator().apply(pb));
  }
}
