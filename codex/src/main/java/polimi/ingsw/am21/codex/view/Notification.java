package polimi.ingsw.am21.codex.view;

public enum Notification {
  CONNECTION_ENSTABLISHED(
    NotificationType.RESPONSE,
    "Connection was enstablished"
  );

  public NotificationType notificationType;
  public String message;

  Notification(NotificationType notificationType, String message) {
    this.notificationType = notificationType;
    this.message = message;
  }
}
