package polimi.ingsw.am21.codex.cli;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;

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

  public static <T extends Colorable> String colorizeString(
    T colorable,
    int length
  ) {
    if (Cli.getInstance().isColored()) {
      return (
        colorable.getColor().code +
        colorable.toString().substring(0, length) +
        Color.RESET.code
      );
    } else {
      return colorable.toString();
    }
  }

  public static <T extends Colorable> String colorizeString(T colorable) {
    return colorizeString(colorable, colorable.toString().length());
  }

  public static <T extends Colorable> String colorizeAndCenter(
    List<T> colorables,
    int length,
    char padChar
  ) {
    StringBuilder sb = new StringBuilder();
    if (Cli.getInstance().isColored()) {
      AtomicInteger ansiSize = new AtomicInteger();
      colorables.forEach(colorable -> {
        ansiSize.addAndGet(
          colorable.getColor().code.length() + Color.RESET.code.length()
        );
        sb.append(colorizeString(colorable, 1));
      });
      return StringUtils.center(sb.toString(), length + ansiSize.get(), ' ');
    }
    colorables.forEach(colorable -> sb.append(colorizeString(colorable, 1)));
    return StringUtils.center(colorables.toString(), length, padChar);
  }

  public static int getColorableLength(Colorable colorable, int colorableSize) {
    if (Cli.getInstance().isColored()) {
      return colorizeString(colorable, colorableSize).length();
    }
    return colorable.toString().length();
  }
}
