package polimi.ingsw.am21.codex.model.Cards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import polimi.ingsw.am21.codex.model.PlayerBoard;

public class GoldCardFrontSide extends ResourceCardFrontSide {

  /**
   * The resources required to place the card
   */
  private final List<ResourceType> placementCondition;
  /**
   * The type of condition required for the card to attribute points to the
   * player, if any is present
   */
  private final Optional<PointConditionType> pointCondition;
  /**
   * The object to be counted when the point condition is of type 'OBJECT'
   */
  private final Optional<ObjectType> pointConditionObject;

  /**
   * Constructor
   *
   * @param points               The points the card should attribute to the
   *                             player (if conditions are met)
   * @param placementCondition   The resources required to place the card
   * @param pointCondition       The type of condition required for the card to
   *                             attribute points to the player (use null
   *                             otherwise)
   * @param pointConditionObject The object to count when the point condition is
   *                             of type 'OBJECT' (use null otherwise)
   */
  public GoldCardFrontSide(
    int points,
    List<ResourceType> placementCondition,
    PointConditionType pointCondition,
    ObjectType pointConditionObject
  ) {
    super(points);
    this.placementCondition = new ArrayList<>(placementCondition);
    this.pointCondition = Optional.ofNullable(pointCondition);
    this.pointConditionObject = Optional.ofNullable(pointConditionObject);
  }

  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    Function<
      PointConditionType,
      BiFunction<PlayerBoard, Integer, Integer>
    > mapper = type -> {
      if (type == PointConditionType.CORNERS) return (
        (playerBoard, coveredCorners) -> coveredCorners * points
      );

      return (
        (playerBoard, coveredCorners) ->
          pointConditionObject
            .map(obj -> playerBoard.getObjects().get(obj))
            .orElse(0)
      );
    };

    return this.pointCondition.map(mapper).orElse(
        (playerBoard, coveredCorners) -> points
      );
  }

  @Override
  public Function<PlayerBoard, Boolean> getPlaceabilityChecker() {
    return playerBoard -> {
      // Collect resources by type
      Map<ResourceType, Integer> placementResources = placementCondition
        .stream()
        .collect(
          Collectors.groupingBy(
            resource -> resource,
            Collectors.summingInt(element -> 1)
          )
        );

      // Return whether all conditions are satisfied
      return placementResources
        .entrySet()
        .stream()
        .map(
          resource ->
            resource.getValue() <=
            playerBoard.getResources().getOrDefault(resource.getKey(), 0)
        )
        .reduce(true, (a, b) -> a && b);
    };
  }
}
