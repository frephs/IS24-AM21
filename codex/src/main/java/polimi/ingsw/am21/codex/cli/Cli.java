package polimi.ingsw.am21.codex.cli;

public class Cli {

  private static final Cli instance = new Cli(true);
  Boolean colored;

  private Cli(Boolean colored) {
    this.colored = colored;
  }

  public static Cli getInstance() {
    return instance;
  }

  public Boolean isColored() {
    return colored;
  }

  public static void main(String[] args) {}
}
