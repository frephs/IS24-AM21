package polimi.ingsw.am21.codex.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Objectives.PointConditionType;
import polimi.ingsw.am21.codex.model.Cards.Playable.*;
import polimi.ingsw.am21.codex.model.GameBoard.GameBoard;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerBoardTest {
  GameBoard gameBoard;
  PlayerBoard pb;
  Method updateMap_1;
  Method updateMap_2;
  Method updateResourcesAndObjectsMaps;

  PlayableCard card;
  PlayableBackSide cb;
  PlayableFrontSide cf;
  ObjectiveCard objectiveCard;

  PlayerBoardTest() throws NoSuchMethodException{
    PlayableFrontSide cf =  new GoldCardFrontSide(12,
      List.of(
        ResourceType.FUNGI, ResourceType.FUNGI
      ),
      PointConditionType.OBJECTS,
      ObjectType.QUILL
    );
    PlayableBackSide cb =  new PlayableBackSide(List.of());
    cf.setCorner(CornerPosition.BOTTOM_LEFT, Optional.of(ResourceType.ANIMAL));
    cb.setCorner(CornerPosition.BOTTOM_LEFT, Optional.of(ResourceType.ANIMAL));
    cb.setCorner(CornerPosition.TOP_RIGHT, Optional.of(ResourceType.FUNGI));
    PlayableCard card = new PlayableCard(12, cf,cb);

    ObjectiveCard objectiveCard = new ObjectiveCard(12,12, new ConcreteObjective());

    pb = new PlayerBoard(List.of(card,card,card),
      card,
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


  @BeforeEach
  void initTest() {
    /*JSONArray cardJson = new JSONArray("polimi/ingsw/am21/codex/model/Cards/Resources/cards.json");
    gameBoard = GameBoard.fromJSON(cardJson);*/
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

    assertEquals(a,2);
    assertEquals(b,3);
    assertEquals(c,1);
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

    IllegalPlacingPositionException e = assertThrows(IllegalPlacingPositionException.class,
      () -> pb.placeCard(card, CardSideType.FRONT,new Position(1,1))
    );


  }
}