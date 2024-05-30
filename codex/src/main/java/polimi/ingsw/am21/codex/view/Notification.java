package polimi.ingsw.am21.codex.view;

public enum Notification {
  CONNECTION_ESTABLISHED(
    NotificationType.RESPONSE,
    "Connection was established"
  ),
  CONNECTION_FAILED(NotificationType.ERROR, "Connection failure"),
  MESSAGE_NOT_SENT(NotificationType.ERROR, "The message was not sent"),

  UNKNOWN_MESSAGE(NotificationType.ERROR, "Unknown message"),
  UNKNOWN_RESPONSE(NotificationType.ERROR, "Unknown response to your request"),
  ALREADY_WAITING(
    NotificationType.WARNING,
    "You are already waiting for a response"
  );

  public NotificationType notificationType;
  public String message;

  Notification(NotificationType notificationType, String message) {
    this.notificationType = notificationType;
    this.message = message;
  }
}
