package polimi.ingsw.am21.codex.model.Cards.Playable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.view.TUI.utils.CliCard;
import polimi.ingsw.am21.codex.view.TUI.utils.CliUtils;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;

public class PlayableBackSide extends PlayableSide implements CliCard {

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
      cardStringMap.put(
        BEGIN + i,
        CliUtils.colorizeAndCenter(List.of(resource), 5, ' ', ColorStyle.BOLD)
      );
    }
    return super.cardToAscii(cardStringMap);
  }

  @Override
  public String cardToString() {
    return "";
  }
}
