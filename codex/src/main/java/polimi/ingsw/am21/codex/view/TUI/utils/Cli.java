package polimi.ingsw.am21.codex.view.TUI.utils;

import java.util.*;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class Cli extends View {

  public static class Options {

    private final Boolean colored;

    public Options(Boolean colored) {
      this.colored = colored;
    }

    public Boolean isColored() {
      return colored;
    }
  }

  Cli.Options options;

  public Cli(Cli.Options options, LocalModelContainer localModel) {
    this.options = options;
  }

  public Boolean isColored() {
    return options.isColored();
  }

  @Override
  public void postNotification(
    NotificationType notificationType,
    String message
  ) {
    printUpdate(
      CliUtils.colorize(
        options,
        message,
        notificationType.getColor(),
        notificationType == NotificationType.ERROR
          ? ColorStyle.BOLD
          : ColorStyle.NORMAL
      )
    );
  }

  @Override
  public void postNotification(Notification notification) {
    postNotification(
      notification.getNotificationType(),
      notification.getMessage()
    );
  }

  @Override
  public void postNotification(
    NotificationType notificationType,
    String[] parts,
    Colorable colorable,
    int colorableIndex
  ) {
    ArrayList<String> result = new ArrayList<>();
    Arrays.stream(parts)
      .limit(2)
      .map(
        e ->
          CliUtils.colorize(
            options,
            e,
            notificationType.getColor(),
            ColorStyle.NORMAL
          )
      )
      .forEach(result::add);
    result.add(
      CliUtils.colorize(
        options,
        colorable,
        colorableIndex == 0 ? ColorStyle.BOLD : ColorStyle.NORMAL
      )
    );
    Arrays.stream(parts)
      .skip(2)
      .map(
        e ->
          CliUtils.colorize(
            options,
            e,
            notificationType.getColor(),
            ColorStyle.NORMAL
          )
      )
      .forEach(result::add);
    printUpdate(String.join("", result));
  }

  public void printPrompt() {
    System.out.print("\r> ");
  }

  public void printUpdate(String string) {
    System.out.println(
      "\r" +
      string +
      " ".repeat(string.length() <= 100 ? 100 - string.length() : 0)
    );
    printPrompt();
  }
}
