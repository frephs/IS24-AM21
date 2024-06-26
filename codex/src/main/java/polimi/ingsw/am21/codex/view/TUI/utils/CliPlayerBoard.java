package polimi.ingsw.am21.codex.view.TUI.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;

public class CliPlayerBoard {

  public static final int SECTION_COLS = 11;
  public static final int SECTION_ROWS = 11;

  private static final String BORDER =
    ("""
      ┌───┬─────┬───┐
      │   │     │   │
      ├───┤     ├───┤
      │   │     │   │
      └───┴─────┴───┘
      """).trim();

  public static void drawPlayerBoard(
    List<
      Pair<Position, Pair<PlayableCard, CardSideType>>
    > playedCardsByPosition,
    Set<Position> avaiableSpots,
    Set<Position> forbiddenSpots,
    int verticalOffset,
    int horizontalOffset
  ) {
    AtomicReference<String> result = new AtomicReference<>(
      (" ".repeat(SECTION_COLS * CLIGridPosition.WIDTH_OFFSET) + "\n").repeat(
          SECTION_ROWS * CLIGridPosition.HEIGHT_OFFEST
        )
    );
    //
    //    avaiableSpots.forEach(modelPosition -> {
    //      String border = CliUtils.colorize(BORDER, Color.GREEN, ColorStyle.NORMAL);
    //      CLIGridPosition viewPosition = new CLIGridPosition(modelPosition);
    //      result.set(
    //        CliPlayerBoard.multilineOverwrite(
    //          result.get(),
    //          border,
    //          viewPosition.getStringRow(verticalOffset),
    //          viewPosition.getStringColumn(horizontalOffset)
    //        )
    //      );
    //    });
    //
    //    forbiddenSpots.forEach(modelPosition -> {
    //      String border = CliUtils.colorize(BORDER, Color.RED, ColorStyle.NORMAL);
    //      CLIGridPosition viewPosition = new CLIGridPosition(modelPosition);
    //      result.set(
    //        CliPlayerBoard.multilineOverwrite(
    //          result.get(),
    //          border,
    //          viewPosition.getStringRow(verticalOffset),
    //          viewPosition.getStringColumn(horizontalOffset)
    //        )
    //      );
    //    });

    playedCardsByPosition.forEach(entry -> {
      Position modelPosition = entry.getKey();
      PlayableCard card = entry.getValue().getKey();
      CardSideType cardSide = entry.getValue().getValue();

      String ASCIISide = card.getSide(cardSide).cardToAscii(new HashMap<>());

      CLIGridPosition viewPosition = new CLIGridPosition(modelPosition);

      result.set(
        CliPlayerBoard.multilineOverwrite(
          result.get(),
          ASCIISide,
          viewPosition.getStringRow(verticalOffset),
          viewPosition.getStringColumn(horizontalOffset)
        )
      );
      //      System.out.println(result.get());
    });

    System.out.println(result.get());
  }

  public static String multilineOverwrite(
    String original,
    String replacement,
    int line,
    int column
  ) {
    String[] originalLines = original.split("\n", -1);

    if (
      line < 0 ||
      line >= originalLines.length ||
      column < 0 ||
      column + CLIGridPosition.WIDTH_OFFSET >= originalLines[0].length()
    ) {
      // We're ignoring cards that are not visible
      return original;
    }

    String[] replacementLines = replacement.split("\n", -1);
    for (int l = 0; l < replacementLines.length; l++) {
      String currLine = originalLines[line + l];
      StringBuilder firstPart = new StringBuilder();
      StringBuilder secondPart;

      int modifiersOffset = Color.getAllModifiers()
        .stream()
        .map(
          modifier ->
            (currLine.split(Pattern.quote(modifier), -1).length - 1) *
            modifier.length()
        )
        .reduce(0, Integer::sum);
      int actualColumn = column + modifiersOffset;

      firstPart.append(currLine, 0, actualColumn);

      int virtualReplacementLength = 0;
      for (int j = 0; j < replacementLines[l].length(); j++) {
        if (
          !Color.getAllModifiers()
            .contains(replacementLines[l].substring(j, j + 1))
        ) virtualReplacementLength++;
      }

      int actualReplacedPartLength = 0;
      for (
        int virtualReplacedPartLength = 0;
        virtualReplacedPartLength < virtualReplacementLength;
        actualReplacedPartLength++
      ) {
        if (
          !Color.getAllModifiers()
            .contains(
              replacementLines[l].substring(
                  actualReplacedPartLength,
                  actualReplacedPartLength + 1
                )
            )
        ) virtualReplacedPartLength++;
      }

      secondPart = new StringBuilder(replacementLines[l]);
      secondPart.append(
        firstPart.length() + actualReplacedPartLength < currLine.length()
          ? currLine.substring(firstPart.length() + actualReplacedPartLength)
          : ""
      );

      originalLines[line + l] = firstPart.toString() + secondPart;
      //      originalLines[line + l] = originalLines[line + l].substring(0, column) +
      //      replacementLines[l] +
      //      originalLines[line + l].substring(column + 15);
    }

    return String.join("\n", originalLines);
  }
}
