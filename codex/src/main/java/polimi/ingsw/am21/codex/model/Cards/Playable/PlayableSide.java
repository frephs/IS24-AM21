package polimi.ingsw.am21.codex.model.Cards.Playable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import polimi.ingsw.am21.codex.cli.CliCard;
import polimi.ingsw.am21.codex.cli.CliUtils;
import polimi.ingsw.am21.codex.cli.ColorStyle;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

// TODO investigate "Raw use of parameterized class 'Corner'" warning

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

  /*
   * -----------------
   * TUI METHODS
   * -----------------
   * */

  public String cardToAscii(Map<Integer, String> cardStringMap) {
    // corners
    corners.forEach((cornerPosition, corner) -> {
      corner
        .getContent()
        .ifPresentOrElse(
          content ->
            cardStringMap.put(
              cornerPosition.index,
              CliUtils.colorize(content, ColorStyle.BOLD, 1)
            ),
          () -> cardStringMap.put(cornerPosition.index, " ")
        );
    });
    //    for (CornerPosition cornerPosition : CornerPosition.values()) {
    //      if (!corners.containsKey(cornerPosition)) {
    //        cardStringMap.put(
    //          cornerPosition.index,
    //          CliUtils.colorize("X", Color.RED_BACKGROUND)
    //        );
    //      }
    //    }

    return CliCard.playableCardToAscii(cardStringMap);
  }
  //  @Override
  //  public String cardToString() {
  //
  //  }
}
