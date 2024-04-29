package polimi.ingsw.am21.codex.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Objectives.PointConditionType;
import polimi.ingsw.am21.codex.model.Cards.Playable.*;
import polimi.ingsw.am21.codex.model.GameBoard.GameBoard;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PlayerBoardTest {
  GameBoard gameBoard;
  PlayerBoard pb;
  Method updateMap_1;
  Method updateMap_2;
  Method updateResourcesAndObjectsMaps;

  PlayableCard card;
  PlayableCard starterCard;
  PlayableCard goldCard;

  PlayableBackSide cb;
  PlayableFrontSide cf;
  ObjectiveCard objectiveCard;


  @BeforeEach
  void initTest() throws NoSuchMethodException {
    PlayableFrontSide cf =  new GoldCardFrontSide(12,
      List.of(
        ResourceType.FUNGI, ResourceType.FUNGI
      ),
      PointConditionType.OBJECTS,
      ObjectType.QUILL
    );

    PlayableBackSide cb =  new PlayableBackSide(List.of());
    PlayableBackSide cb_2 =  new PlayableBackSide(List.of());

    cb_2.setCorner(CornerPosition.BOTTOM_RIGHT, Optional.of(ResourceType.ANIMAL));
    cb_2.setCorner(CornerPosition.TOP_RIGHT, Optional.of(ResourceType.FUNGI));
    cb_2.setCorner(CornerPosition.TOP_LEFT, Optional.empty());

    cf.setCorner(CornerPosition.BOTTOM_LEFT, Optional.of(ResourceType.ANIMAL));

    cb.setCorner(CornerPosition.BOTTOM_LEFT, Optional.of(ResourceType.ANIMAL));
    cb.setCorner(CornerPosition.TOP_RIGHT, Optional.of(ResourceType.FUNGI));

    card = new PlayableCard(12, cf,cb);
    starterCard = new PlayableCard(12, cf,cb_2);
    starterCard.setPlayedSideType(CardSideType.BACK);
    goldCard = new PlayableCard(22,cf,cb);
    ObjectiveCard objectiveCard = new ObjectiveCard(12,12, new ConcreteObjective());

    pb = new PlayerBoard(
      List.of(card,card,card),
      starterCard,
      objectiveCard
    );

    // let's make private methods public

    updateMap_1 = pb.getClass().getDeclaredMethod(
      "updateMap", ResourceType.class, int.class
    );

    updateMap_2 = pb.getClass().getDeclaredMethod(
      "updateMap", ObjectType.class, int.class
    );

    updateResourcesAndObjectsMaps = pb.getClass().getDeclaredMethod(
      "updateResourcesAndObjectsMaps", Corner.class, int.class
    );

    updateMap_1.setAccessible(true);
    updateMap_2.setAccessible(true);
    updateResourcesAndObjectsMaps.setAccessible(true);
  }

  @Test
  void mapUpdateTest() throws IllegalAccessException, InvocationTargetException {
    card.setPlayedSideType(CardSideType.BACK);
    Corner corner = card.getPlayedSide().get().getCorners().get(CornerPosition.BOTTOM_LEFT);

    // let's call this private methods
    updateResourcesAndObjectsMaps.invoke(pb,   corner,+1);

    updateMap_1.invoke(pb, ResourceType.PLANT, +1);
    updateMap_1.invoke(pb, ResourceType.PLANT, +1);
    updateMap_2.invoke(pb, ObjectType.INKWELL,+3);

    int a = pb.getResources().get(ResourceType.PLANT);
    int b = pb.getObjects().get(ObjectType.INKWELL);
    int c = pb.getResources().get(ResourceType.ANIMAL);
    int d = pb.getResources().get(ResourceType.FUNGI);

    assertEquals(a,2);
    assertEquals(b,3);
    assertEquals(c,2);
    assertEquals(d,1);
  }


  @Test
  void testEquals(){
    Object o = (Object) ResourceType.ANIMAL;
    Optional<Object> oo =  Optional.of(o);
    ResourceType r = ResourceType.ANIMAL;
    assert(r.equals(oo.get()));
  }

  @Test
  void getPlaceableCardSides() {
    assertEquals(pb.getPlaceableCardSides().size(), 3);
  }

  @Test
  void drawCard() {

  }

  @Test
  void placeCard() {
    // testing if exception is thrown if not enough resources are on the playerboard
    assertThrows(IllegalCardSideChoiceException.class,
      () ->pb.placeCard(card, CardSideType.FRONT,new Position().computeAdjacentPosition(CornerPosition.TOP_RIGHT))
    );

    // testing if exception is thrown if a card is placed on a disabled corner
    assertThrows(IllegalPlacingPositionException.class,
      () -> pb.placeCard(card, CardSideType.BACK, new Position().computeAdjacentPosition(CornerPosition.BOTTOM_LEFT))
    );


    // testing a card is correctly added to the playedCards set
    pb.placeCard(card, CardSideType.BACK, new Position(-1,1));
    assertEquals(
      pb.getPlayedCards().get(new Position(-1,1)).getPlayedSide(), card.getPlayedSide()
    );

    // testing resources have been correctly updated
    assertEquals(
      pb.getResources().get(ResourceType.FUNGI), 2
    );

    assertEquals(
      pb.getResources().get(ResourceType.ANIMAL), 2
    );


    pb.placeCard(card, CardSideType.BACK, new Position(0,2));

    assertEquals(
      pb.getResources().get(ResourceType.FUNGI), 2
    );

    assertEquals(
      pb.getResources().get(ResourceType.ANIMAL), 3
    );


    //testing if an exception is thrown when you try placing a card in an occupied positions
    assertThrows(
      IllegalPlacingPositionException.class,
      () -> pb.placeCard(card, CardSideType.BACK, new Position())
    );

    //testing if an exception is thrown when you try placing a card in positions which have one disabled angle but one available
    assertThrows(
      IllegalPlacingPositionException.class,
      () -> pb.placeCard(card, CardSideType.BACK, new Position (1,1))
    );

    //testing ig the playerboard removes all the resources a card covers.
    pb.placeCard(card, CardSideType.BACK, new Position(1,-1));

    // you must have a card in your hand to place it's side
    pb.getHand().add(starterCard);
    pb.placeCard(starterCard, CardSideType.BACK, new Position(2,0));

    assertEquals(
      pb.getResources().get(ResourceType.ANIMAL), 4
    );
    assertEquals(
      pb.getResources().get(ResourceType.FUNGI), 3
    );

    // testing if a card with a placing condition can be placed after the condition is met

    // remember not to try to place the same card with a different side it fucks up everything
    pb.getHand().add(goldCard);
    pb.placeCard(goldCard, CardSideType.FRONT, new Position(3,-1));

    // testing if a card with a pointCondition corretly sets it's covered corners
    assertEquals(
      pb.getPlayedCards().get(new Position(3,-1)).getCoveredCorners(),1
    );


  }
}