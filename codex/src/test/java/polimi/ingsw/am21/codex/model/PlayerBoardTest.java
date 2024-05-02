package polimi.ingsw.am21.codex.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
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

public class PlayerBoardTest {

  GameBoard gameBoard;
  public PlayerBoard pb;
  Method updateMap_1;
  Method updateMap_2;
  Method updateResourcesAndObjectsMaps;

  public PlayableCard card;
  public PlayableCard starterCard;
  public PlayableCard goldCard;
  public PlayableCard resourceCard;

  public PlayableBackSide cb;
  public PlayableFrontSide cf;
  public ObjectiveCard objectiveCard;

  public void externalSetup() {
    try {
      initTest();
    } catch (NoSuchMethodException ignored) {} catch (
      IllegalCardSideChoiceException | IllegalPlacingPositionException e
    ) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  void initTest()
    throws NoSuchMethodException, IllegalCardSideChoiceException, IllegalPlacingPositionException {
    // dummy card front side
    PlayableFrontSide cf = new GoldCardFrontSide(
      12,
      List.of(ResourceType.FUNGI, ResourceType.FUNGI),
      PointConditionType.OBJECTS,
      ObjectType.QUILL
    );

    cf.setCorner(CornerPosition.BOTTOM_LEFT, Optional.of(ResourceType.ANIMAL));

    // dummy cards back sides
    PlayableBackSide cb = new PlayableBackSide(List.of());
    PlayableBackSide cb_2 = new PlayableBackSide(List.of(ResourceType.INSECT));
    PlayableBackSide cb_3 = new PlayableBackSide(List.of(ResourceType.PLANT));

    cb.setCorner(CornerPosition.BOTTOM_LEFT, Optional.of(ResourceType.ANIMAL));
    cb.setCorner(CornerPosition.TOP_RIGHT, Optional.of(ResourceType.FUNGI));

    cb_2.setCorner(
      CornerPosition.BOTTOM_RIGHT,
      Optional.of(ResourceType.ANIMAL)
    );
    cb_2.setCorner(CornerPosition.TOP_RIGHT, Optional.of(ResourceType.FUNGI));
    cb_2.setCorner(CornerPosition.TOP_LEFT, Optional.empty());

    cb_3.setCorner(CornerPosition.BOTTOM_LEFT, Optional.empty());
    cb_3.setCorner(CornerPosition.TOP_LEFT, Optional.empty());
    cb_3.setCorner(CornerPosition.TOP_RIGHT, Optional.empty());
    cb_3.setCorner(CornerPosition.BOTTOM_RIGHT, Optional.empty());

    //dummy cards
    card = new PlayableCard(1, cf, cb);

    starterCard = new PlayableCard(2, cf, cb_2);
    starterCard.setPlayedSideType(CardSideType.BACK);
    resourceCard = new PlayableCard(4, cf, cb_3, ResourceType.PLANT);
    resourceCard.setPlayedSideType(CardSideType.BACK);

    goldCard = new PlayableCard(3, cf, cb);
    objectiveCard = new ObjectiveCard(4, 12, new ConcreteObjective());

    // dummy playerBoard
    pb = new PlayerBoard(List.of(card, card, card), starterCard, objectiveCard);

    // let's make private methods public to test them

    updateMap_1 = pb
      .getClass()
      .getDeclaredMethod("updateMap", ResourceType.class, int.class);

    updateMap_2 = pb
      .getClass()
      .getDeclaredMethod("updateMap", ObjectType.class, int.class);

    updateResourcesAndObjectsMaps = pb
      .getClass()
      .getDeclaredMethod(
        "updateResourcesAndObjectsMaps",
        Corner.class,
        int.class
      );

    updateMap_1.setAccessible(true);
    updateMap_2.setAccessible(true);
    updateResourcesAndObjectsMaps.setAccessible(true);
  }

  @Test
  void mapUpdateTest()
    throws IllegalAccessException, InvocationTargetException {
    card.setPlayedSideType(CardSideType.BACK);
    Corner corner = card
      .getPlayedSide()
      .get()
      .getCorners()
      .get(CornerPosition.BOTTOM_LEFT);

    // let's call this private methods
    updateResourcesAndObjectsMaps.invoke(pb, corner, +1);

    updateMap_1.invoke(pb, ResourceType.PLANT, +1);
    updateMap_1.invoke(pb, ResourceType.PLANT, +1);
    updateMap_2.invoke(pb, ObjectType.INKWELL, +3);

    int a = pb.getResources().get(ResourceType.PLANT);
    int b = pb.getObjects().get(ObjectType.INKWELL);
    int c = pb.getResources().get(ResourceType.ANIMAL);
    int d = pb.getResources().get(ResourceType.FUNGI);

    assertEquals(a, 2);
    assertEquals(b, 3);
    assertEquals(c, 2);
    assertEquals(d, 1);
  }

  @Test
  void testEquals() {
    Object o = (Object) ResourceType.ANIMAL;
    Optional<Object> oo = Optional.of(o);
    ResourceType r = ResourceType.ANIMAL;
    assertEquals(r, oo.get());
  }

  @Test
  void getPlaceableCardSides() {
    assertEquals(3, pb.getPlaceableCardSides().size());
  }

  @Test
  void drawCard() {}

  @Test
  void placeCard()
    throws IllegalCardSideChoiceException, IllegalPlacingPositionException {
    // testing if exception is thrown if not enough resources are on the playerboard
    assertThrows(
      IllegalCardSideChoiceException.class,
      () ->
        pb.placeCard(
          card,
          CardSideType.FRONT,
          new Position().computeAdjacentPosition(CornerPosition.TOP_RIGHT)
        )
    );

    // testing if exception is thrown if a card is placed on a disabled corner
    assertThrows(
      IllegalPlacingPositionException.class,
      () ->
        pb.placeCard(
          card,
          CardSideType.BACK,
          new Position().computeAdjacentPosition(CornerPosition.BOTTOM_LEFT)
        )
    );

    // testing a card is correctly added to the playedCards set
    pb.placeCard(card, CardSideType.BACK, new Position(-1, 0));
    assertEquals(
      pb.getPlayedCards().get(new Position(-1, 0)).getPlayedSide(),
      card.getPlayedSide()
    );

    // testing resources have been correctly updated
    assertEquals(2, pb.getResources().get(ResourceType.FUNGI));

    assertEquals(2, pb.getResources().get(ResourceType.ANIMAL));

    pb.placeCard(card, CardSideType.BACK, new Position(-1, 1));

    assertEquals(2, pb.getResources().get(ResourceType.FUNGI));

    assertEquals(3, pb.getResources().get(ResourceType.ANIMAL));

    //testing if an exception is thrown when you try placing a card in an occupied positions
    assertThrows(
      IllegalPlacingPositionException.class,
      () -> pb.placeCard(card, CardSideType.BACK, new Position())
    );

    //testing if an exception is thrown when you try placing a card in positions which have one disabled angle but one available
    assertThrows(
      IllegalPlacingPositionException.class,
      () -> pb.placeCard(card, CardSideType.BACK, new Position(0, 1))
    );

    //testing ig the playerboard removes all the resources a card covers.
    pb.placeCard(card, CardSideType.BACK, new Position(1, -0));

    // you must have a card in your hand to place it's side
    pb.getHand().add(starterCard);
    pb.placeCard(starterCard, CardSideType.BACK, new Position(1, 1));

    assertEquals(4, pb.getResources().get(ResourceType.ANIMAL));
    assertEquals(3, pb.getResources().get(ResourceType.FUNGI));

    // testing if a card with a placing condition can be placed after the condition is met

    // remember not to try to place the same card with a different side it fucks up everything
    pb.getHand().add(goldCard);
    pb.placeCard(goldCard, CardSideType.FRONT, new Position(2, 1));

    // testing if a card with a pointCondition corretly sets it's covered corners
    assertEquals(
      1,
      pb.getPlayedCards().get(new Position(2, 1)).getCoveredCorners()
    );

    // testing if the backPermanentResources are correctly added to the resources
    // (starterCard is added 2 times: on PlayerBoard initialization and later in testing)
    assertEquals(2, pb.getResources().get(ResourceType.INSECT));
  }
}
