package polimi.ingsw.am21.codex.view.GUI.utils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import polimi.ingsw.am21.codex.view.GUI.Gui;
import polimi.ingsw.am21.codex.view.NotificationType;

public class NotificationLoader {

  private static Stage notificationStage;

  private Queue<NotificationLayout> notifications = new LinkedList<>();

  public NotificationLoader(Stage notificationStage) {
    NotificationLoader.notificationStage = notificationStage;

    notificationStage.setAlwaysOnTop(true);
    notificationStage.initStyle(StageStyle.TRANSPARENT);
    notificationStage.initModality(Modality.NONE);
  }

  public void addNotification(
    NotificationType notificationType,
    String message
  ) throws IOException {
    NotificationLayout notification = new NotificationLayout();

    notification.loadNotification();
    notification.setNotificationText(message);
    notification.setNotificationType(notificationType);
    notification.setDraggable(notificationStage);

    notifications.add(notification);

    if (!notificationStage.isShowing()) {
      processNotifications();
    }
  }

  public void processNotifications() throws IOException {
    if (!notifications.isEmpty()) {
      NotificationLayout notification = notifications.poll();

      Scene notificationScene = new Scene((Parent) notification.getParent());

      notificationScene.setFill(Color.TRANSPARENT);
      notificationStage.setScene(notificationScene);
      notificationStage.show();

      notification.startProgressBar(2000, true);

      // Close the notification after the duration and process the next one
      new Thread(() -> {
        try {
          Thread.sleep(2000); // Duration in milliseconds
        } catch (InterruptedException e) {
          Gui.getInstance().displayException(e);
        }
        Platform.runLater(() -> {
          notificationStage.close();
          try {
            processNotifications();
          } catch (IOException e) {
            Gui.getInstance().displayException(e);
          }
        });
      }).start();
    }
  }

  public static class NotificationLayout extends DraggableLayout {

    Parent notificationLayout;

    @FXML
    ProgressBar progressBar;

    @FXML
    Node notificationContainer;

    @FXML
    Text notificationText;

    public NotificationLayout() {}

    public Node getParent() {
      return notificationLayout;
    }

    private void loadNotification() throws IOException {
      FXMLLoader loader = new FXMLLoader(
        Gui.class.getResource("Notification.fxml")
      );

      loader.setController(this);
      notificationLayout = loader.load();
    }

    private void setNotificationText(String message) {
      notificationText.setText(message);
    }

    private void setNotificationType(NotificationType notificationType) {
      notificationContainer
        .getStyleClass()
        .add(notificationType.getStyleClass());
    }

    public void startProgressBar(int duration, boolean reverse) {
      Timeline timeline = new Timeline();

      for (int i = 0; i <= duration; i += 25) {
        final double progress = i / (double) duration;
        timeline
          .getKeyFrames()
          .add(
            new KeyFrame(
              Duration.millis(i),
              event ->
                progressBar.setProgress((reverse ? 1 - progress : progress))
            )
          );
      }

      timeline.play();
    }

    @Override
    public void setDraggable(Stage notificationStage) {
      setDraggable(notificationStage, notificationContainer);
    }
  }
}
