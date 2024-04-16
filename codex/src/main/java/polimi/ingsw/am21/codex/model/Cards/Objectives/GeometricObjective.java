package polimi.ingsw.am21.codex.model.Cards.Objectives;

import java.util.Map;
import java.util.function.BiFunction;

import polimi.ingsw.am21.codex.model.Cards.AdjacentPosition;
import polimi.ingsw.am21.codex.model.Cards.EdgePosition;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.PlayerBoard;

public class GeometricObjective extends Objective {

  /**
   * The map of the adjacent card you need to reach the objective
   */
  private final Map<AdjacentPosition, ResourceType> geometry;

  public GeometricObjective(Map<AdjacentPosition, ResourceType> geometry) {
    this.geometry = geometry;
  }

  /**
   * The function get the PlayerBoard and the points of the objective card you are evaluating
   * @return the point that you get from that objective card
   */
  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    return (playerBoard, points) ->
      playerBoard
        .getPlayedCards()
        .entrySet()
        .stream()
        // let's get all the cards which match which the center one (which is always present)
        .filter(
          card ->
            card
              .getValue()
              .getKingdom()
              .map(kingdom -> kingdom == geometry.get(EdgePosition.CENTER))
              .orElse(false)
        )
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
                    .getKingdom()
                    .orElse(null) ==
                  adjacentPosition.getValue()
              );
            })
            .reduce((a, b) -> a && b))
        .map(e -> e.orElse(false))
        .filter(Boolean::booleanValue)
        .mapToInt(element -> 1)
        .sum();
  }
}
