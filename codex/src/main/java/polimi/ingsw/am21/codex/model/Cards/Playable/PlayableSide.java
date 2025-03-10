package polimi.ingsw.am21.codex.model.Cards.Playable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import polimi.ingsw.am21.codex.model.Cards.Corner;
import polimi.ingsw.am21.codex.model.Cards.CornerContentType;
import polimi.ingsw.am21.codex.model.Cards.CornerPosition;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.TUI.utils.CliCard;
import polimi.ingsw.am21.codex.view.TUI.utils.CliUtils;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;

public abstract class PlayableSide implements CliCard {

  /**
   * The Map of the CornerPosition and the corner on the side of the card
   */
  private final Map<CornerPosition, Corner<CornerContentType>> corners;

  public PlayableSide() {
    this.corners = new HashMap<>();
  }

  public Map<CornerPosition, Corner<CornerContentType>> getCorners() {
    return corners;
  }

  public void setCorner(
    CornerPosition position,
    Optional<CornerContentType> content
  ) {
    if (content.isEmpty()) {
      corners.put(position, new Corner<>());
    } else {
      corners.put(position, new Corner<>(content.get()));
    }
  }

  /**
   * Generates a function that should be called to get the points that should be
   * attributed to a player when they place a card on this side. The returned
   * function requires a PlayerBoard an Integer representing the number of
   * corners
   * the card is covering.
   */
  public abstract BiFunction<PlayerBoard, Integer, Integer> getEvaluator();

  /**
   * Generates a function that should be called to get whether a side is
   * placeable or not
   * given a certain PlayerBoard.
   */
  public Function<PlayerBoard, Boolean> getPlaceabilityChecker() {
    return playerBoard -> true;
  }

  @Override
  public String cardToAscii(HashMap<Integer, String> cardStringMap) {
    // corners
    corners.forEach(
      (cornerPosition, corner) ->
        corner
          .getContent()
          .ifPresentOrElse(
            content ->
              cardStringMap.put(
                cornerPosition.getIndex(),
                CliUtils.colorize(content, ColorStyle.BOLD, 1)
              ),
            () -> cardStringMap.put(cornerPosition.getIndex(), " ")
          )
    );

    return CliCard.playableCardToAscii(cardStringMap);
  }
}
