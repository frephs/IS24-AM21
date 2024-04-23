package polimi.ingsw.am21.codex.model.Cards;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CardBuilderTest {

  @Test
  void setPoints() {
    assertThrows(
      WrongCardTypeException.class,
      () -> new CardBuilder(123, CardType.STARTER).setPoints(123)
    );

    assertDoesNotThrow(
      () ->
        List.of(CardType.GOLD, CardType.RESOURCE, CardType.OBJECTIVE).forEach(
          type -> new CardBuilder(123, type).setPoints(123)
        )
    );

    assertInstanceOf(
      CardBuilder.class,
      new CardBuilder(123, CardType.OBJECTIVE).setPoints(123)
    );
  }

  @Test
  void setObjectiveType() {
    List.of(CardType.GOLD, CardType.RESOURCE, CardType.STARTER).forEach(
      type ->
        assertThrows(
          WrongCardTypeException.class,
          () -> new CardBuilder(123, type).setObjectiveType(null)
        )
    );

    assertInstanceOf(
      CardBuilder.class,
      new CardBuilder(123, CardType.OBJECTIVE).setObjectiveType(
        ObjectiveType.COUNTING
      )
    );
  }

  @Test
  void setObjectiveGeometry() {
    List.of(CardType.GOLD, CardType.RESOURCE, CardType.STARTER).forEach(
      type ->
        assertThrows(
          WrongCardTypeException.class,
          () -> new CardBuilder(123, type).setObjectiveGeometry(null)
        )
    );

    assertThrows(
      ConflictingParameterException.class,
      () ->
        new CardBuilder(123, CardType.OBJECTIVE).setObjectiveType(
          ObjectiveType.COUNTING
        )
    );

    assertInstanceOf(
      CardBuilder.class,
      new CardBuilder(123, CardType.OBJECTIVE)
        .setObjectiveType(ObjectiveType.GEOMETRIC)
        .setObjectiveGeometry(
          Map.of(CornerPosition.BOTTOM_LEFT, ResourceType.INSECT)
        )
    );
  }

  @Test
  void setObjectiveResources() {
    List.of(CardType.GOLD, CardType.RESOURCE, CardType.STARTER).forEach(
      type ->
        assertThrows(
          WrongCardTypeException.class,
          () -> new CardBuilder(123, type).setObjectiveResources(null)
        )
    );

    assertThrows(
      ConflictingParameterException.class,
      () ->
        new CardBuilder(123, CardType.OBJECTIVE).setObjectiveType(
          ObjectiveType.GEOMETRIC
        )
    );

    assertInstanceOf(
      CardBuilder.class,
      new CardBuilder(123, CardType.OBJECTIVE)
        .setObjectiveType(ObjectiveType.COUNTING)
        .setObjectiveResources(new HashMap<>())
    );
  }

  @Test
  void setObjectiveObjects() {
    List.of(CardType.GOLD, CardType.RESOURCE, CardType.STARTER).forEach(
      type ->
        assertThrows(
          WrongCardTypeException.class,
          () -> new CardBuilder(123, type).setObjectiveObjects(null)
        )
    );

    assertThrows(
      ConflictingParameterException.class,
      () ->
        new CardBuilder(123, CardType.OBJECTIVE).setObjectiveType(
          ObjectiveType.GEOMETRIC
        )
    );

    assertInstanceOf(
      CardBuilder.class,
      new CardBuilder(123, CardType.OBJECTIVE)
        .setObjectiveType(ObjectiveType.COUNTING)
        .setObjectiveObjects(new HashMap<>())
    );
  }

  @Test
  void setBackPermanentResources() {
    assertThrows(
      WrongCardTypeException.class,
      () ->
        new CardBuilder(123, CardType.OBJECTIVE).setBackPermanentResources(null)
    );

    List.of(CardType.GOLD, CardType.RESOURCE, CardType.STARTER).forEach(
      type ->
        assertDoesNotThrow(() -> new CardBuilder(123, type).setPoints(123))
    );

    assertInstanceOf(
      CardBuilder.class,
      new CardBuilder(123, CardType.RESOURCE).setBackPermanentResources(
        List.of(ResourceType.FUNGI)
      )
    );
  }

  @Test
  void setPlacementCondition() {
    List.of(CardType.OBJECTIVE, CardType.RESOURCE, CardType.STARTER).forEach(
      type ->
        assertThrows(
          WrongCardTypeException.class,
          () -> new CardBuilder(123, type).setPlacementCondition(null)
        )
    );

    assertInstanceOf(
      CardBuilder.class,
      new CardBuilder(123, CardType.GOLD).setPlacementCondition(
        List.of(ResourceType.FUNGI)
      )
    );
  }

  @Test
  void setPointCondition() {
    List.of(CardType.OBJECTIVE, CardType.RESOURCE, CardType.STARTER).forEach(
      type ->
        assertThrows(
          WrongCardTypeException.class,
          () -> new CardBuilder(123, type).setPointCondition(null)
        )
    );

    assertInstanceOf(
      CardBuilder.class,
      new CardBuilder(123, CardType.GOLD).setPointCondition(
        PointConditionType.CORNERS
      )
    );
  }

  @Test
  void setPointConditionObject() {
    List.of(CardType.OBJECTIVE, CardType.RESOURCE, CardType.STARTER).forEach(
      type ->
        assertThrows(
          WrongCardTypeException.class,
          () -> new CardBuilder(123, type).setPointConditionObject(null)
        )
    );

    assertThrows(
      ConflictingParameterException.class,
      () ->
        new CardBuilder(123, CardType.GOLD)
          .setPointCondition(PointConditionType.CORNERS)
          .setPointConditionObject(null)
    );

    assertInstanceOf(
      CardBuilder.class,
      new CardBuilder(123, CardType.GOLD)
        .setPointCondition(PointConditionType.OBJECTS)
        .setPointConditionObject(ObjectType.MANUSCRIPT)
    );
  }

  @Test
  void setCorners() {
    assertThrows(
      WrongCardTypeException.class,
      () -> new CardBuilder(123, CardType.OBJECTIVE).setCorners(null, null)
    );

    // TODO: FIX
    //List.of(CardType.GOLD, CardType.RESOURCE, CardType.STARTER).forEach(
    //  type ->
    //    assertDoesNotThrow(
    //      () -> new CardBuilder(123, type).setCorners(null, null)
    //    )
    //);

    assertInstanceOf(
      CardBuilder.class,
      new CardBuilder(123, CardType.RESOURCE).setCorners(
        CardSideType.FRONT,
        new HashMap<>()
      )
    );
  }

  @Test
  void build() {
    CardBuilder builder;

    // region Objective
    builder = new CardBuilder(123, CardType.OBJECTIVE);
    // missing points
    assertThrows(MissingParametersException.class, builder::build);

    builder = builder.setPoints(123);
    // missing objectiveType
    assertThrows(MissingParametersException.class, builder::build);

    builder = builder.setObjectiveType(ObjectiveType.GEOMETRIC);
    // missing objectiveGeometry
    assertThrows(MissingParametersException.class, builder::build);

    builder = builder.setObjectiveType(ObjectiveType.COUNTING);
    // missing objectiveResources
    assertThrows(MissingParametersException.class, builder::build);

    builder = builder.setObjectiveResources(new HashMap<>());
    // missing objectiveObjects
    assertThrows(MissingParametersException.class, builder::build);

    builder = builder
      .setObjectiveResources(null)
      .setObjectiveObjects(new HashMap<>());
    // missing objectiveResources
    assertThrows(MissingParametersException.class, builder::build);

    assertDoesNotThrow(() -> {
      new CardBuilder(123, CardType.OBJECTIVE)
        .setPoints(123)
        .setObjectiveType(ObjectiveType.GEOMETRIC)
        .setObjectiveGeometry(
          Map.of(EdgePosition.BOTTOM, ResourceType.FUNGI)
        )
        .build();
      new CardBuilder(123, CardType.OBJECTIVE)
        .setPoints(123)
        .setObjectiveType(ObjectiveType.COUNTING)
        .setObjectiveResources(new HashMap<>())
        .setObjectiveObjects(new HashMap<>())
        .build();
    });
    // endregion

    // region Starter
    builder = new CardBuilder(123, CardType.STARTER);
    // missing backPermanentResources
    assertThrows(MissingParametersException.class, builder::build);

    assertDoesNotThrow(() -> {
      new CardBuilder(123, CardType.STARTER)
        .setBackPermanentResources(
          List.of(
            ResourceType.ANIMAL,
            ResourceType.PLANT,
            ResourceType.INSECT
          )
        )
        .build();
    });
    // endregion

    // region Resource
    builder = new CardBuilder(123, CardType.RESOURCE);
    // missing backPermanentResources
    assertThrows(MissingParametersException.class, builder::build);

    builder = builder.setBackPermanentResources(
      List.of(ResourceType.FUNGI, ResourceType.INSECT)
    );
    // missing points
    assertThrows(MissingParametersException.class, builder::build);

    assertDoesNotThrow(() -> {
      new CardBuilder(123, CardType.RESOURCE)
        .setBackPermanentResources(List.of(ResourceType.PLANT))
        .setPoints(123)
        .build();
    });
    //endregion

    // region Gold
    builder = new CardBuilder(123, CardType.GOLD);
    // missing backPermanentResources
    assertThrows(MissingParametersException.class, builder::build);

    builder = builder.setBackPermanentResources(
      List.of(ResourceType.FUNGI, ResourceType.INSECT)
    );
    // missing points
    assertThrows(MissingParametersException.class, builder::build);

    builder = builder.setPoints(123);
    // missing placementCondition
    assertThrows(MissingParametersException.class, builder::build);

    assertDoesNotThrow(() -> {
      new CardBuilder(123, CardType.GOLD)
        .setBackPermanentResources(List.of(ResourceType.PLANT))
        .setPoints(123)
        .setPlacementCondition(List.of(ResourceType.ANIMAL))
        .build();
    });
    // endregion
  }
}
