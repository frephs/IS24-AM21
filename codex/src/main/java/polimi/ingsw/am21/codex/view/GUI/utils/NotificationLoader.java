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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import polimi.ingsw.am21.codex.view.GUI.Gui;
import polimi.ingsw.am21.codex.view.NotificationType;

public class NotificationLoader {

  private final Stage notificationStage;

  private Queue<NotificationLayout> notifications = new LinkedList<>();

  public NotificationLoader(Stage notificationStage) {
    this.notificationStage = notificationStage;

    notificationStage.setAlwaysOnTop(true);
    notificationStage.initStyle(StageStyle.TRANSPARENT);
    notificationStage.initModality(Modality.NONE);
  }

  public void addNotification(
    NotificationType notificationType,
    String message
  ) throws IOException {
    notifications.add(new NotificationLayout(notificationType, message));
    if (!notificationStage.isShowing()) {
      processNotifications();
    }
  }

  public void processNotifications() throws IOException {
    if (!notifications.isEmpty()) {
      NotificationLayout notification = notifications.poll();
      notification.loadNotification();

      Scene notificationScene = new Scene(
        (Parent) notification.getNotificationLayout()
      );

      notificationScene.setFill(Color.TRANSPARENT); // Make the scene background transparent
      notificationStage.setScene(notificationScene);
      notificationStage.show();

      notification.startProgressBar(2000, true);

      // Close the notification after the duration and process the next one
      new Thread(() -> {
        try {
          Thread.sleep(2000); // Duration in milliseconds
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        Platform.runLater(() -> {
          notificationStage.close();
          try {
            processNotifications();
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
      }).start();
    }
  }

  private class NotificationLayout {

    @FXML
    ProgressBar progressBar;

    @FXML
    Node notificationLayout;

    @FXML
    Text notificationText;

    private NotificationType notificationType;
    private String message;

    public NotificationLayout(
      NotificationType notificationType,
      String message
    ) {
      this.notificationType = notificationType;
      this.message = message;
    }

    public Node getNotificationLayout() {
      return notificationLayout;
    }

    public void loadNotification() throws IOException {
      FXMLLoader loader = new FXMLLoader(
        Gui.class.getResource("Notification.fxml")
      );

      loader.setController(this);
      notificationLayout = loader.load();

      notificationText = (Text) notificationLayout.lookup("#notification-text");
      progressBar = (ProgressBar) notificationLayout.lookup("#progress-bar");

      setNotificationText();
      setNotificationType();
    }

    private void setNotificationText() {
      notificationText = (Text) notificationLayout.lookup("#notification-text");
      notificationText.setText(message);
    }

    private void setNotificationType() {
      VBox notificationBox = (VBox) notificationLayout.lookup(".notification");
      notificationBox.getStyleClass().add(notificationType.getStyleClass());
    }

    public void startProgressBar(int duration, boolean reverse) {
      progressBar = (ProgressBar) notificationLayout.lookup("#progress-bar");

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
  }
}
