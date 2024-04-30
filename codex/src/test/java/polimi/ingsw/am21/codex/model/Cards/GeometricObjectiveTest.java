package polimi.ingsw.am21.codex.model.Cards;

import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Objectives.GeometricObjective;
import polimi.ingsw.am21.codex.model.Cards.Objectives.Objective;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.model.PlayerBoardTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static polimi.ingsw.am21.codex.model.Cards.CornerPosition.BOTTOM_LEFT;
import static polimi.ingsw.am21.codex.model.Cards.CornerPosition.TOP_LEFT;

class GeometricObjectiveTest {

  @Test
  void getEvaluator() {
    Map<AdjacentPosition, ResourceType> geometry = new HashMap<>();

    // test diagonal geometry
    geometry.put(CornerPosition.TOP_LEFT, ResourceType.PLANT);
    geometry.put(EdgePosition.CENTER, ResourceType.PLANT);
    geometry.put(CornerPosition.BOTTOM_RIGHT, ResourceType.PLANT);

    GeometricObjective geometricObjective = new GeometricObjective(geometry);

    PlayerBoardTest pbt = new PlayerBoardTest();
    pbt.externalSetup();

    PlayerBoard pb = new PlayerBoard(
      List.of(pbt.resourceCard,pbt.resourceCard, pbt.resourceCard),
      pbt.resourceCard,
      new ObjectiveCard(123, 3, new GeometricObjective(geometry))
    );

    assertEquals(pbt.resourceCard.getKingdom().orElseThrow(), ResourceType.PLANT);

    pb.placeCard(pbt.resourceCard,CardSideType.BACK, new Position(-1,0));
    pb.placeCard(pbt.resourceCard,CardSideType.BACK, new Position(1,0));
    pb.placeCard(pbt.resourceCard,CardSideType.BACK, new Position(2,0));

    // introducing an element of disturb
    pb.getHand().add(pbt.resourceCard);
    pb.placeCard(pbt.resourceCard,CardSideType.BACK, new Position(2,1));

    pb.getHand().add(pbt.resourceCard);
    pb.placeCard(pbt.resourceCard,CardSideType.BACK, new Position(3,0));

    assertEquals(9, pb.getObjectiveCard().getEvaluator().apply(pb));

    // testing "disconnected" cards geometry.
    geometry = new HashMap<>();
    geometry.put(TOP_LEFT, ResourceType.PLANT);
    geometry.put(EdgePosition.CENTER, ResourceType.PLANT);
    geometry.put(EdgePosition.BOTTOM, ResourceType.PLANT);

    assertEquals(pbt.resourceCard.getKingdom().orElseThrow(), ResourceType.PLANT);

    pb = new PlayerBoard(
      List.of(pbt.resourceCard,pbt.resourceCard, pbt.resourceCard),
      pbt.resourceCard,
      new ObjectiveCard(123, 2, new GeometricObjective(geometry))
    );

    pb.placeCard(pbt.resourceCard,CardSideType.BACK, new Position(-1,0));
    pb.placeCard(pbt.resourceCard,CardSideType.BACK, new Position(1,0));
    pb.placeCard(pbt.resourceCard,CardSideType.BACK, new Position(1,-1));

    assertEquals(2, pb.getObjectiveCard().getEvaluator().apply(pb));

  }
}