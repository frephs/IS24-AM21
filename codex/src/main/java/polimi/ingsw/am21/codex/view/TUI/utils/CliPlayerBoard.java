package polimi.ingsw.am21.codex.view.TUI.utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;

public class CliPlayerBoard {

  /**
   * The number of grid columns to show in a playerboard section
   */
  public static final int SECTION_COLS = 15;
  /**
   * The number of grid rows to show in a playerboard section
   */
  public static final int SECTION_ROWS = 15;

  /**
   * The list of box characters used to draw cards
   */
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

  /**
   * A sample border for available spots
   */
  private static final String AVAILABLE_SPOTS_BORDER =
    ("""
      ╭┄┄┄┄┄┄┄┄┄┄┄┄┄╮
      ┆             ┆
      ┆             ┆
      ┆             ┆
      ╰┄┄┄┄┄┄┄┄┄┄┄┄┄╯
      """).trim();

  /**
   * Builds a string representing a playerboard
   * @param playedCardsByOrder The list of cards that the player has placed
   * @param availableSpots The set of available spots for the player to place a card
   * @param verticalOffset The vertical offset of the playerboard (> 0 => down, < 0 => up)
   * @param horizontalOffset The horizontal offset of the playerboard (> 0 => right, < 0 => left)
   * @return The colorized string representing the playerboard
   */
  public static String drawPlayerBoard(
    List<Pair<Position, Pair<PlayableCard, CardSideType>>> playedCardsByOrder,
    Set<Position> availableSpots,
    int verticalOffset,
    int horizontalOffset
  ) {
    // Initialize the result string
    AtomicReference<String> result = new AtomicReference<>(
      (" ".repeat(SECTION_COLS * CLIGridPosition.WIDTH_OFFSET) + "\n").repeat(
          SECTION_ROWS * CLIGridPosition.HEIGHT_OFFEST
        )
    );

    // For each available spot, ...
    availableSpots.forEach(modelPosition -> {
      CLIGridPosition viewPosition = new CLIGridPosition(modelPosition);

      // ... generate a "placeholder" card that includes the spot coordinates, ...
      String spot = CliPlayerBoard.multilineOverwrite(
        AVAILABLE_SPOTS_BORDER,
        "┆" +
        StringUtils.center(
          modelPosition.getX() + ";" + modelPosition.getY(),
          AVAILABLE_SPOTS_BORDER.split("\n")[2].length() - 2
        ) +
        "┆",
        2,
        0
      );

      //... then insert it in the result string
      result.set(
        CliPlayerBoard.multilineOverwrite(
          result.get(),
          (spot),
          viewPosition.getStringRow(verticalOffset),
          viewPosition.getStringColumn(horizontalOffset)
        )
      );
    });

    // For each card placed by the player, ...
    playedCardsByOrder.forEach(entry -> {
      Position modelPosition = entry.getKey();
      PlayableCard card = entry.getValue().getKey();
      CardSideType cardSide = entry.getValue().getValue();

      // Generate the ASCII representation of the card
      final String[] ASCIISide = {
        card.getSide(cardSide).cardToAscii(new HashMap<>()),
      };
      // Remove all color modifiers
      Color.getAllModifiers()
        .forEach(modifier -> ASCIISide[0] = ASCIISide[0].replace(modifier, ""));

      CLIGridPosition viewPosition = new CLIGridPosition(modelPosition);

      // Draw the card in the result string
      result.set(
        CliPlayerBoard.multilineOverwrite(
          result.get(),
          ASCIISide[0],
          viewPosition.getStringRow(verticalOffset),
          viewPosition.getStringColumn(horizontalOffset)
        )
      );
    });

    // Return the colorized string
    return (colorizeLater(result.get()));
  }

  /**
   * Replaces a part of a string with another string, merging box characters when possible
   * @param original The original "result" string
   * @param replacement The replacement string (can be multiline)
   * @param line The line to position the top-left corner of the replacement string at
   * @param column The column to position the top-left corner of the replacement string at
   */
  public static String multilineOverwrite(
    String original,
    String replacement,
    int line,
    int column
  ) {
    String[] lines = original.split("\n", -1);
    String[] replacementLines = replacement.split("\n", -1);

    if (
      line < 0 ||
      line >= lines.length ||
      column < 0 ||
      column + CLIGridPosition.WIDTH_OFFSET >= lines[0].length()
    ) {
      // We're ignoring cards that are not visible
      return original;
    }

    // For each line of the replacement string, ...
    for (int l = 0; l < replacementLines.length; l++) {
      // Get the current lines
      String currLine = lines[line + l];
      String replacementLine = replacementLines[l].trim();

      StringBuilder mergedReplacement = new StringBuilder();

      // For each character of the replacement string, ...
      for (int i = 0; i < replacementLine.length(); i++) {
        // Get the merging characters
        String a = currLine.substring(column + i, column + i + 1);
        String b = replacementLine.substring(i, i + 1);

        // If the characters are box characters, merge them
        if (
          BOX_CHARACTERS.contains(a) && BOX_CHARACTERS.contains(b)
        ) mergedReplacement.append(CliPlayerBoard.addBoxCharacters(a, b));
        // Otherwise, prefer the replacement character
        else mergedReplacement.append(b);
      }

      // Replace the current line with the merged replacement string
      lines[line + l] = currLine.substring(0, column) +
      mergedReplacement +
      currLine.substring(column + replacementLine.length());
    }

    // Re-join all the lines
    return (String.join("\n", lines));
  }

  /**
   * Re-applies color modifiers to a playerboard string, based on the characters
   * @param string The playerboard string to colorize
   */
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
        "([╭╮╰╯┄┆])", // This matches available spots
        CliUtils.colorize("$1", Color.GREEN, ColorStyle.NORMAL)
      );
  }

  /**
   * Merges two box characters into one
   * @throws IllegalStateException If any of the two characters is not a box character
   */
  public static String addBoxCharacters(String a, String b) {
    // "Native" symbols: ─ │ ┌ ┬ ┐ ├ ┤ └ ┴ ┘
    // "Added" symbols: ┼

    // This is pretty tedious, but I didn't find any better alternative
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
