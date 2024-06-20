package polimi.ingsw.am21.codex.model.Cards.Objectives;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import polimi.ingsw.am21.codex.model.Cards.AdjacentPosition;
import polimi.ingsw.am21.codex.model.Cards.EdgePosition;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.TUI.utils.CliCard;
import polimi.ingsw.am21.codex.view.TUI.utils.CliUtils;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;

public class GeometricObjective extends Objective implements CliCard {

  /**
   * The map of the adjacent card you need to reach the objective
   */
  private final Map<AdjacentPosition, ResourceType> geometry;

  public GeometricObjective(Map<AdjacentPosition, ResourceType> geometry) {
    this.geometry = new HashMap<>(geometry);
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
        .sum() *
      points;
  }

  @Override
  public String cardToString() {
    return (
      "group of cards positioned as following: \n" +
      geometry
        .entrySet()
        .stream()
        .map(
          adjacentPositionResourceTypeEntry ->
            adjacentPositionResourceTypeEntry
              .getKey()
              .toString()
              .replaceAll("_", " ")
              .toLowerCase() +
            ": " +
            CliUtils.colorize(
              adjacentPositionResourceTypeEntry.getValue(),
              ColorStyle.UNDERLINED
            )
        )
        .collect(Collectors.collectingAndThen(Collectors.joining(", "), s -> s))
    );
  }

  @Override
  public String cardToAscii(HashMap<Integer, String> cardStringMap) {
    return cardToString();
  }
}
