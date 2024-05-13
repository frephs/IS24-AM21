package polimi.ingsw.am21.codex.client;

import java.util.Optional;

public class Command {

  String command;
  String description;
  boolean toList;
  Optional<Command> requiredCommmand;

  public Command(
    String command,
    String description,
    boolean toBeListed,
    Command requiredCommmand
  ) {
    this.command = command;
    this.description = description;
    this.toList = toBeListed;
    this.requiredCommmand = Optional.ofNullable(requiredCommmand);
  }

  public Command(String command, String description, boolean toBeListed) {
    this(command, description, toBeListed, null);
  }
}
