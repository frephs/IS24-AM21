package polimi.ingsw.am21.codex.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.*;
import polimi.ingsw.am21.codex.model.GameBoard.GameBoard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerBoardTest {
  GameBoard gameBoard;


  @BeforeEach
  void initTest() {
    /*JSONArray cardJson = new JSONArray("polimi/ingsw/am21/codex/model/Cards/Resources/cards.json");
    gameBoard = GameBoard.fromJSON(cardJson);*/
  }

  @Test
  void mapUpdateTest() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    PlayableFrontSide cf =  new ResourceCardFrontSide(12);
    PlayableBackSide cb =  new PlayableBackSide(List.of());
    cb.setCorner(CornerPosition.BOTTOM_LEFT, ResourceType.ANIMAL_KINGDOM);
    PlayableCard card = new PlayableCard(12, cf,cb);

    ObjectiveCard objectiveCard = new ObjectiveCard(12,12, new ConcreteObjective());

    PlayerBoard pb = new PlayerBoard(List.of(card,card,card),
      card,
      objectiveCard
      );

    // let's make private methods public
    Method updateMap_1 = pb.getClass().getDeclaredMethod(
      "updateMap", ResourceType.class, int.class
    );

    Method updateMap_2 = pb.getClass().getDeclaredMethod(
      "updateMap", ObjectType.class, int.class
    );

    Method updateResourcesAndObjectsMaps = pb.getClass().getDeclaredMethod(
        "updateResourcesAndObjectsMaps", Corner.class, int.class
      );

    updateMap_1.setAccessible(true);
    updateMap_2.setAccessible(true);
    updateResourcesAndObjectsMaps.setAccessible(true);


    card.setPlayedSideType(CardSideType.BACK);
    Corner corner = card.getPlayedSide().get().getCorners().get(CornerPosition.BOTTOM_LEFT);


    // let's call this private methods
    updateResourcesAndObjectsMaps.invoke(pb,   corner,+1);

    updateMap_1.invoke(pb, ResourceType.PLANT_KINGDOM, +1);
    updateMap_1.invoke(pb, ResourceType.PLANT_KINGDOM, +1);
    updateMap_2.invoke(pb, ObjectType.INKWELL,+3);

    int a = pb.getResources().get(ResourceType.PLANT_KINGDOM);
    int b = pb.getObjects().get(ObjectType.INKWELL);
    int c = pb.getResources().get(ResourceType.ANIMAL_KINGDOM);

    assertEquals(a,2);
    assertEquals(b,3);
    assertEquals(c,1);
  }


  @Test
  void testEquals(){
    Object o = (Object) ResourceType.ANIMAL_KINGDOM;
    Optional<Object> oo =  Optional.of(o);
    ResourceType r = ResourceType.ANIMAL_KINGDOM;
    assert(r.equals(oo.get()));
  }

  @Test
  void getPlaceableCardSides() {
    /*PlayableCard card = new PlayableCard() ;
    do{
      try{
        card = gameBoard.drawGoldCardFromDeck();
      }catch(EmptyDeckException ignored){}
    }while(
      card.get
    );*/
  }

  @Test
  void drawCard() {

  }

  @Test
  void placeCard() {


  }
}