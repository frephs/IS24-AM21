package polimi.ingsw.am21.codex.model.Cards.Objectives;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.TUI.utils.CliCard;
import polimi.ingsw.am21.codex.view.TUI.utils.CliUtils;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;

public class CountingObjective extends Objective implements CliCard {

  /**
   * The map of which resources and how many to complete the objective
   */
  private Map<ResourceType, Integer> resources;
  /**
   * The map of which object and how many to complete the objective
   */
  private Map<ObjectType, Integer> objects;

  public CountingObjective(
    Map<ResourceType, Integer> resources,
    Map<ObjectType, Integer> objects
  ) {
    this.resources = new HashMap<>(resources);
    this.objects = new HashMap<>(objects);
  }

  /**
   * The function get the PlayerBoard and the points of the objective card you are evaluating
   * @return the point that you get from that objective card
   */
  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    return (playerBoard, points) ->
      (resources
            .entrySet()
            .stream()
            .map(
              resource ->
                (playerBoard.getResources().get(resource.getKey()) /
                  resource.getValue())
            )
            .reduce(Integer::min)
            .orElse(0) *
          points +
        (objects
            .entrySet()
            .stream()
            .map(
              object ->
                (playerBoard.getObjects().get(object.getKey()) /
                  object.getValue())
            )
            .reduce(Integer::min)
            .orElse(0) *
          points));
  }

  @Override
  public String cardToString() {
    return (
      resources
        .entrySet()
        .stream()
        .filter(
          resourceTypeIntegerEntry -> resourceTypeIntegerEntry.getValue() != 0
        )
        .map(
          resourceTypeIntegerEntry ->
            resourceTypeIntegerEntry.getValue() +
            " " +
            CliUtils.colorize(
              resourceTypeIntegerEntry.getKey(),
              ColorStyle.UNDERLINED
            ) +
            " resource" +
            (resourceTypeIntegerEntry.getValue() > 1 ? "s" : "")
        )
        .collect(Collectors.joining(" ")) +
      objects
        .entrySet()
        .stream()
        .filter(
          objectTypeIntegerEntry -> objectTypeIntegerEntry.getValue() != 0
        )
        .map(
          objectTypeIntegerEntry ->
            objectTypeIntegerEntry.getValue() +
            " " +
            CliUtils.colorize(
              objectTypeIntegerEntry.getKey(),
              ColorStyle.UNDERLINED
            ) +
            " object" +
            (objectTypeIntegerEntry.getValue() > 1 ? "s" : "")
        )
        .collect(
          Collectors.collectingAndThen(Collectors.joining(", "), s -> s)
        ) +
      " collected"
    );
  }

  @Override
  public String cardToAscii(Map<Integer, String> cardStringMap) {
    //TODO add cardToString implementation
    return "";
  }
}
