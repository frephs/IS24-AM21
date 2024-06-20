package polimi.ingsw.am21.codex.connection;

public enum ConnectionType {
  RMI,
  TCP;

  public int getDefaultPort() {
    return switch (this) {
      case TCP -> 2002;
      case RMI -> 2024;
    };
  }
}
