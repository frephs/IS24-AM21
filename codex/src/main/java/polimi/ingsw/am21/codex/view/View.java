package polimi.ingsw.am21.codex.view;

public interface View {
  void postNotication(NotificationType notificationType, String message);

  void postNotication(Notification notification);
}
