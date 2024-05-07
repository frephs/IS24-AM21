package polimi.ingsw.am21.codex.model.Cards.Playable;

import java.util.Map;
import java.util.function.BiFunction;
import org.apache.commons.lang3.StringUtils;
import polimi.ingsw.am21.codex.cli.CliPrintable;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

public class ResourceCardFrontSide
  extends PlayableFrontSide
  implements CliPrintable {

  /**
   * The points the card should attribute to the player
   */
  protected final int points;

  /**
   * Constructor
   * @param points The points the card should attribute to the player
   */
  public ResourceCardFrontSide(int points) {
    super();
    this.points = points;
  }

  @Override
  public BiFunction<PlayerBoard, Integer, Integer> getEvaluator() {
    return ((playerBoard, integer) -> points);
  }

  /*
   * -----------------
   * TUI METHODS
   * -----------------
   * */

  @Override
  public String cardToAscii(Map<Integer, String> cardStringMap) {
    if (!cardStringMap.containsKey(5) && points > 0) {
      cardStringMap.put(5, StringUtils.center(String.valueOf(points), 5, ' '));
    }
    return super.cardToAscii(cardStringMap);
  }

  @Override
  public String cardToString() {
    return "";
  }
}
