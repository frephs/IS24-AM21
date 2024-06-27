package polimi.ingsw.am21.codex.view.TUI.utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;

public class CliPlayerBoard {

  public static final int SECTION_COLS = 21;
  public static final int SECTION_ROWS = 21;

  public static final List<String> BOX_CHARACTERS = List.of(
    "─",
    "│",
    "┌",
    "┬",
    "┐",
    "├",
    "┤",
    "└",
    "┴",
    "┘",
    " ",
    "┼"
  );

  private static final String BORDER =
    ("""
      ╭┄┄┄┄┄┄┄┄┄┄┄┄┄╮
      ┆             ┆
      ┆             ┆
      ┆             ┆
      ╰┄┄┄┄┄┄┄┄┄┄┄┄┄╯
      """).trim();

  public static void drawPlayerBoard(
    List<
      Pair<Position, Pair<PlayableCard, CardSideType>>
    > playedCardsByPosition,
    Set<Position> avaiableSpots,
    int verticalOffset,
    int horizontalOffset
  ) {
    AtomicReference<String> result = new AtomicReference<>(
      (" ".repeat(SECTION_COLS * CLIGridPosition.WIDTH_OFFSET) + "\n").repeat(
          SECTION_ROWS * CLIGridPosition.HEIGHT_OFFEST
        )
    );

    avaiableSpots.forEach(modelPosition -> {
      CLIGridPosition viewPosition = new CLIGridPosition(modelPosition);
      result.set(
        CliPlayerBoard.multilineOverwrite(
          result.get(),
          (BORDER),
          viewPosition.getStringRow(verticalOffset),
          viewPosition.getStringColumn(horizontalOffset)
        )
      );
    });

    playedCardsByPosition.forEach(entry -> {
      Position modelPosition = entry.getKey();
      PlayableCard card = entry.getValue().getKey();
      CardSideType cardSide = entry.getValue().getValue();

      final String[] ASCIISide = {
        card.getSide(cardSide).cardToAscii(new HashMap<>()),
      };
      Color.getAllModifiers()
        .forEach(modifier -> ASCIISide[0] = ASCIISide[0].replace(modifier, ""));

      CLIGridPosition viewPosition = new CLIGridPosition(modelPosition);

      result.set(
        CliPlayerBoard.multilineOverwrite(
          result.get(),
          ASCIISide[0],
          viewPosition.getStringRow(verticalOffset),
          viewPosition.getStringColumn(horizontalOffset)
        )
      );
    });

    System.out.println(colorizeLater(result.get()));
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
      String replacementLine = replacementLines[l].trim();

      StringBuilder mergedReplacement = new StringBuilder();

      for (int i = 0; i < replacementLine.length(); i++) {
        String a = currLine.substring(column + i, column + i + 1);
        String b = replacementLine.substring(i, i + 1);

        if (
          BOX_CHARACTERS.contains(a) && BOX_CHARACTERS.contains(b)
        ) mergedReplacement.append(CliPlayerBoard.addBoxCharacters(a, b));
        else mergedReplacement.append(b);
      }

      originalLines[line + l] = currLine.substring(0, column) +
      mergedReplacement +
      currLine.substring(column + replacementLine.length());
    }

    return (String.join("\n", originalLines));
  }

  public static String colorizeLater(String string) {
    return string
      .replaceAll(
        "F",
        CliUtils.colorize("F", ResourceType.FUNGI.getColor(), ColorStyle.NORMAL)
      )
      .replaceAll(
        "I",
        CliUtils.colorize(
          "I",
          ResourceType.INSECT.getColor(),
          ColorStyle.NORMAL
        )
      )
      .replaceAll(
        "P",
        CliUtils.colorize("P", ResourceType.PLANT.getColor(), ColorStyle.NORMAL)
      )
      .replaceAll(
        "A",
        CliUtils.colorize(
          "A",
          ResourceType.ANIMAL.getColor(),
          ColorStyle.NORMAL
        )
      )
      .replaceAll(
        "([╭╮╰╯┄┆])",
        CliUtils.colorize("$1", Color.GREEN, ColorStyle.NORMAL)
      );
  }

  public static String addBoxCharacters(String a, String b) {
    // ─ │ ┌ ┬ ┐ ├ ┤ └ ┴ ┘
    // ┼
    if (a.equals("┼") || b.equals("┼")) return "┼";
    return switch (a) {
      case "─" -> switch (b) {
        case "─", " " -> "─";
        case "│", "┤", "├" -> "┼";
        case "┌", "┐", "┬" -> "┬";
        case "└", "┘", "┴" -> "┴";
        default -> throw new IllegalStateException("Unexpected value: " + b);
      };
      case "│" -> switch (b) {
        case "─", "┬", "┴" -> "┼";
        case "│", " " -> "│";
        case "┌", "├", "└" -> "├";
        case "┐", "┘", "┤" -> "┤";
        default -> throw new IllegalStateException("Unexpected value: " + b);
      };
      case "┌" -> switch (b) {
        case "─", "┬", "┐" -> "┬";
        case "┌", " " -> "┌";
        case "│", "└", "├" -> "├";
        case "┴", "┘", "┤" -> "┼";
        default -> throw new IllegalStateException("Unexpected value: " + b);
      };
      case "┬" -> switch (b) {
        case "─", "┌", "┐", "┬", " " -> "┬";
        case "│", "└", "┴", "┘", "┤", "├" -> "┼";
        default -> throw new IllegalStateException("Unexpected value: " + b);
      };
      case "┐" -> switch (b) {
        case "─", "┬", "┐", "┌", " " -> "┬";
        case "┘", "│", "┤" -> "┤";
        case "├", "└", "┴" -> "┼";
        default -> throw new IllegalStateException("Unexpected value: " + b);
      };
      case "├" -> switch (b) {
        case "└", "┌", "│", "├", " " -> "├";
        case "─", "┬", "┐", "┴", "┘", "┤" -> "┼";
        default -> throw new IllegalStateException("Unexpected value: " + b);
      };
      case "┤" -> switch (b) {
        case "┐", "┘", "│", "┤", " " -> "┤";
        case "─", "┌", "┬", "└", "├", "┴" -> "┼";
        default -> throw new IllegalStateException("Unexpected value: " + b);
      };
      case "└" -> switch (b) {
        case "└", " " -> "└";
        case "┌", "│", "├" -> "├";
        case "─", "┘", "┴" -> "┴";
        case "┬", "┐", "┤" -> "┼";
        default -> throw new IllegalStateException("Unexpected value: " + b);
      };
      case "┴" -> switch (b) {
        case "└", "┘", "─", "┴", " " -> "┴";
        case "┌", "┐", "┬", "┤", "│", "├" -> "┼";
        default -> throw new IllegalStateException("Unexpected value: " + b);
      };
      case "┘" -> switch (b) {
        case "┘", " " -> "┘";
        case "└", "─", "┴" -> "┴";
        case "┌", "┬", "├" -> "┼";
        case "┤", "┐", "│" -> "┤";
        default -> throw new IllegalStateException("Unexpected value: " + b);
      };
      case " " -> b;
      default -> throw new IllegalStateException("Unexpected value: " + a);
    };
  }
}
