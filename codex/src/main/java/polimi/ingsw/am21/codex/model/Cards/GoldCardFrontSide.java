package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GoldCardFrontSide extends ResourceCardFrontSide {
    /**
     * The resources required to place the card
     */
    final List<ResourceType> placementCondition;
    /**
     * The type of condition required for the card to attribute points to the
     * player, if any is present
     */
    final Optional<PointConditionType> pointCondition;
    /**
     * The object to be counted when the point condition is of type 'OBJECT'
     */
    final Optional<ObjectType> pointConditionObject;

    /**
     * Constructor
     *
     * @param points               The points the card should attribute to the player (if conditions are met)
     * @param placementCondition   The resources required to place the card
     * @param pointCondition       The type of condition required for the card to
     *                             attribute points to the player (use null otherwise)
     * @param pointConditionObject The object to count when the point condition is
     *                             of type 'OBJECT' (use null otherwise)
     */
    public GoldCardFrontSide(int points,
                             List<ResourceType> placementCondition,
                             PointConditionType pointCondition,
                             ObjectType pointConditionObject) {
        super(points);
        this.placementCondition = new ArrayList<>(placementCondition);
        this.pointCondition = Optional.ofNullable(pointCondition);
        this.pointConditionObject = Optional.ofNullable(pointConditionObject);
    }

    @Override
    public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
        Function<PointConditionType, BiFunction<PlayerBoard, Integer, Integer>> mapper = type -> {
            if (type == PointConditionType.CORNERS)
                return ((playerBoard, coveredCorners) -> coveredCorners * points);

            // TODO should we find a better way to handle this, instead of just returning 0?
            // Can I define the BiFunction to throw an Exception?
            return (
                    (playerBoard, integer) ->
                            pointConditionObject
                                    .map(obj -> playerBoard.getObjects().get(obj))
                                    .orElse(0)
            );
        };

        return this.pointCondition.map(mapper).orElse((playerBoard, coveredCorners) -> points);
    }
}
