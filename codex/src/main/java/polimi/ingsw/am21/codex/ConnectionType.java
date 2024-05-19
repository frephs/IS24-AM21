package polimi.ingsw.am21.codex;

public enum ConnectionType {
  RMI,
  TCP;

  public Integer getDefaultPort() {
    return switch (this) {
      case TCP -> 2002;
      case RMI -> 2024;
      default -> null;
    };
  }
}
