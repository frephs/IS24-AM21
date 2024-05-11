package polimi.ingsw.am21.codex.view.TUI.utils.commons;

public enum Color {
  RESET("\u001B[0m", null, null, null, null),

  // Regular Colors
  BLACK("\u001B[30m", "\033[1;30m", "\033[4;30m", "\033[40m", "\033[0;90m"),
  RED("\u001B[31m", "\033[1;31m", "\033[4;31m", "\033[41m", "\033[0;91m"),
  GREEN("\u001B[32m", "\033[1;32m", "\033[4;32m", "\033[42m", "\033[0;92m"),
  YELLOW("\u001B[33m", "\033[1;33m", "\033[4;33m", "\033[43m", "\033[0;93m"),
  BLUE("\u001B[34m", "\033[1;34m", "\033[4;34m", "\033[44m", "\033[0;94m"),
  PURPLE("\u001B[35m", "\033[1;35m", "\033[4;35m", "\033[45m", "\033[0;95m"),
  CYAN("\u001B[36m", "\033[1;36m", "\033[4;36m", "\033[46m", "\033[0;96m"),
  WHITE("\u001B[37m", "\033[1;37m", "\033[4;37m", "\033[47m", "\033[0;97m"),

  GRAY(
    "\u001B[90m",
    "\u001B[1;90m",
    "\u001B[4;90m",
    "\u001B[40m\u001B[90m",
    "\u001B[37;1m"
  );

  public final String normal, bold, underlined, background, bright;

  Color(
    String normal,
    String bold,
    String underlined,
    String background,
    String bright
  ) {
    this.normal = normal;
    this.bold = bold;
    this.underlined = underlined;
    this.background = background;
    this.bright = bright;
  }

  public static String colorize(String string, Color color, ColorStyle style) {
    return (color.getCode(style) + string + Color.RESET.normal);
  }

  public String getCode(ColorStyle style) {
    return switch (style) {
      case NORMAL -> this.normal;
      case BOLD -> this.bold;
      case UNDERLINED -> this.underlined;
      case BACKGROUND -> this.background;
      case BRIGHT -> this.bright;
    };
  }
}
