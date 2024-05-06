package polimi.ingsw.am21.codex.model.Cards.Playable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.cli.CliUtils;
import polimi.ingsw.am21.codex.cli.PrintableCard;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

public class PlayableBackSide extends PlayableSide implements PrintableCard {

  /**
   * The list of permanent resources on the side
   */
  private final List<ResourceType> permanentResources;

  /**
   * Constructor
   * @param permanentResources The list of permanent resources on the side
   */
  public PlayableBackSide(List<ResourceType> permanentResources) {
    super();
    this.permanentResources = new ArrayList<>(permanentResources);
  }

  public List<ResourceType> getPermanentResources() {
    return permanentResources;
  }

  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    return ((playerBoard, coveredCorners) -> 0);
  }

  /*
   * -----------------
   * TUI METHODS
   * -----------------
   * */

  @Override
  public String cardToAscii(Map<Integer, String> cardStringMap) {
    final int BEGIN = 4;
    for (int i = 0; i < permanentResources.size(); i++) {
      ResourceType resource = permanentResources.get(i);
      cardStringMap.put(BEGIN + i, CliUtils.colorizeString(resource, 1));
    }
    return super.cardToAscii(cardStringMap);
  }

  @Override
  public String cardToString() {
    return "";
  }
}
