package polimi.ingsw.am21.codex.view.TUI.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;

public class CliUtils {

  /**
   * @param s1 the first string to be returned
   * @param s2 the second string to be returned
   *
   * @return the concatenated lines of the two given strings: it has a number
   * of lines equal to the minimum between the number of lines of s1 and s2.
   * The rest are discarded.
   * */
  public static String joinMinLines(String s1, String s2) {
    String[] lines1 = s1.split("\n");
    String[] lines2 = s2.split("\n");

    int min_lines = Math.min(lines1.length, lines2.length);

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < min_lines; i++) {
      sb.append(lines1[i]);
      sb.append(lines2[i]);
      sb.append("\n");
    }

    return sb.toString();
  }

  /**
   * Colorizes a string with the given color and style, automatically resetting
   * the formatting afterward
   * @param colorable The colorable to colorize
   * @param style The style to use
   * @param length The length to truncate the string to
   */
  public static <T extends Colorable> String colorize(
    T colorable,
    ColorStyle style,
    int length
  ) {
    if (Cli.Options.isColored()) {
      return (
        colorable.getColor().getCode(style) +
        colorable.toString().substring(0, length) +
        Color.RESET.normal
      );
    } else {
      return colorable.toString();
    }
  }

  /**
   * Colorizes a string with the given color and style, automatically resetting
   * the formatting afterward
   * @param colorable The colorable to colorize
   * @param style The style to use
   */
  public static <T extends Colorable> String colorize(
    T colorable,
    ColorStyle style
  ) {
    return colorize(colorable, style, colorable.toString().length());
  }

  /**
   * Colorizes a string with the given color and style, automatically resetting
   * the formatting afterward
   * @param string The string to colorize
   * @param color The color to use
   * @param style The style to use
   */
  public static String colorize(String string, Color color, ColorStyle style) {
    if (Cli.Options.isColored()) {
      return Color.colorize(string, color, style);
    }
    return string;
  }

  /**
   * Colorizes a string with the given color and style, automatically resetting
   * the formatting afterward
   * @param colorables The list of colorable elements to colorize
   * @param style The style to use
   * @param length The length to truncate the string to
   * @param padChar The character to pad the string with
   */
  public static <T extends Colorable> String colorizeAndCenter(
    List<T> colorables,
    int length,
    char padChar,
    ColorStyle style
  ) {
    StringBuilder sb = new StringBuilder();
    if (Cli.Options.isColored()) {
      AtomicInteger ansiSize = new AtomicInteger();
      colorables.forEach(colorable -> {
        ansiSize.addAndGet(
          colorable.getColor().getCode(style).length() +
          Color.RESET.normal.length()
        );
        sb.append(colorize(colorable, style, 1));
      });
      return StringUtils.center(sb.toString(), length + ansiSize.get(), ' ');
    }
    colorables.forEach(colorable -> sb.append(colorize(colorable, style, 1)));
    return StringUtils.center(colorables.toString(), length, padChar);
  }

  /**
   * Gets the length of a colorable element when colorized with the given style
   * @param colorable The colorable element
   * @param colorableSize The size of the colorable element
   * @param style The style to use
   */
  public static int getColorableLength(
    Colorable colorable,
    int colorableSize,
    ColorStyle style
  ) {
    if (Cli.Options.isColored()) {
      return colorize(colorable, style, colorableSize).length();
    }
    return colorable.toString().length();
  }

  /**
   * Gets a table with the given headers and columns
   * @param headers The headers of the table
   * @param columns The columns of the table
   */
  @SafeVarargs
  public static String getTable(
    String[] headers,
    ArrayList<String>... columns
  ) {
    StringBuilder sb = new StringBuilder();

    int[] maxWidths = new int[columns.length];
    for (int i = 0; i < columns.length; i++) {
      columns[i].addFirst(headers[i]);
      for (String value : columns[i]) {
        maxWidths[i] = Math.max(maxWidths[i], value.length()) + 2;
      }
    }

    for (int i = 0; i < columns.length; i++) {
      sb.append(
        String.format("%-" + (maxWidths[i] + 2) + "s", "[" + headers[i] + "]")
      );
    }
    sb.append("\n");

    int numRows = Arrays.stream(columns).mapToInt(List::size).max().orElse(0);

    // Append table data
    for (int row = 1; row < numRows; row++) {
      for (int col = 0; col < columns.length; col++) {
        if (row < columns[col].size()) {
          sb.append(
            String.format(
              "%-" + (maxWidths[col] + 2) + "s",
              columns[col].get(row)
            )
          );
        } else {
          sb.append(String.format("%-" + (maxWidths[col] + 2) + "s", "")); // Append empty string if no value
        }
      }
      sb.append("\n");
    }

    return sb.toString();
  }
}
