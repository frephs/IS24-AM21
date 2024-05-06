package polimi.ingsw.am21.codex.cli;

public enum Color {
  RESET("\u001B[0m"),

  // Regular Colors
  BLACK("\u001B[30m"),
  RED("\u001B[31m"),
  GREEN("\u001B[32m"),
  YELLOW("\u001B[33m"),
  BLUE("\u001B[34m"),
  PURPLE("\u001B[35m"),
  CYAN("\u001B[36m"),
  WHITE("\u001B[37m"),
  GRAY("\u001B[90m"),

  // Bold
  BLACK_BOLD("\033[1;30m"),
  RED_BOLD("\033[1;31m"),
  GREEN_BOLD("\033[1;32m"),
  YELLOW_BOLD("\033[1;33m"),
  BLUE_BOLD("\033[1;34m"),
  PURPLE_BOLD("\033[1;35m"),
  CYAN_BOLD("\033[1;36m"),
  WHITE_BOLD("\033[1;37m"),
  GRAY_BOLD("\u001B[1;90m"),

  // Underline
  BLACK_UNDERLINED("\033[4;30m"),
  RED_UNDERLINED("\033[4;31m"),
  GREEN_UNDERLINED("\033[4;32m"),
  YELLOW_UNDERLINED("\033[4;33m"),
  BLUE_UNDERLINED("\033[4;34m"),
  PURPLE_UNDERLINED("\033[4;35m"),
  CYAN_UNDERLINED("\033[4;36m"),
  WHITE_UNDERLINED("\033[4;37m"),
  GRAY_UNDERLINED("\u001B[4;90m"),

  // Background
  BLACK_BACKGROUND("\033[40m"),
  RED_BACKGROUND("\033[41m"),
  GREEN_BACKGROUND("\033[42m"),
  YELLOW_BACKGROUND("\033[43m"),
  BLUE_BACKGROUND("\033[44m"),
  PURPLE_BACKGROUND("\033[45m"),
  CYAN_BACKGROUND("\033[46m"),
  WHITE_BACKGROUND("\033[47m"),
  GRAY_BACKGROUND("\u001B[40m\u001B[90m"),

  // High Intensity
  BLACK_BRIGHT("\033[0;90m"),
  RED_BRIGHT("\033[0;91m"),
  GREEN_BRIGHT("\033[0;92m"),
  YELLOW_BRIGHT("\033[0;93m"),
  BLUE_BRIGHT("\033[0;94m"),
  PURPLE_BRIGHT("\033[0;95m"),
  CYAN_BRIGHT("\033[0;96m"),
  WHITE_BRIGHT("\033[0;97m"),
  BRIGHT_GRAY("\u001B[37;1m");

  public final String code;

  Color(String code) {
    this.code = code;
  }

  static String colorize(String string, Color color) {
    return color.code + string + Color.RESET.code;
  }
}
