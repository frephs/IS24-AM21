package polimi.ingsw.am21.codex.model.Cards;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.model.PlayerBoard;

public class GeometricObjective extends Objective {

  private final Map<AdjacentPosition, ResourceType> geometry;

  public GeometricObjective(Map<AdjacentPosition, ResourceType> geometry) {
    this.geometry = geometry;
  }

  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    return (playerBoard, points) ->
      (Integer) playerBoard
        .getPlayedCards()
        .entrySet()
        .stream()
        // let's get all the cards which match which the center one (which is always present)
        .filter(card ->
          //FIXME: better way which doesn't use Optional.of
          Optional.of(geometry.get(EdgePosition.CENTER)) ==
          card.getValue().getKingdom())
        // let's see if for every card the objective applies
        .map(card ->
          geometry
            .entrySet()
            .stream()
            //.filter(adjacentGeometry -> adjacentGeometry.getKey() != EdgePosition.CENTER) not really useful excluding this value
            .map(adjacentPosition -> {
              Position adjacentPlayedCardPosition = card
                .getKey()
                .computeAdjacentPosition(adjacentPosition.getKey());
              return (
                playerBoard
                  .getPlayedCards()
                  .containsKey(adjacentPlayedCardPosition) &&
                playerBoard
                    .getPlayedCards()
                    .get(adjacentPlayedCardPosition)
                    .getKingdom() ==
                  Optional.of(adjacentPosition.getValue())
              );
            })
            .reduce((a, b) -> a && b))
        .map(Optional::get)
        .filter(Boolean::booleanValue)
        .mapToInt(element -> 1)
        .sum();
  }
}
