module polimi.ingsw.am21.codex {
  requires javafx.controls;
  requires javafx.fxml;
  requires org.json;
  requires org.controlsfx.controls;
  requires com.almasb.fxgl.all;
  requires java.rmi;
  requires annotations;
  requires org.apache.commons.lang3;

  opens polimi.ingsw.am21.codex to javafx.fxml;
  exports polimi.ingsw.am21.codex;
  exports polimi.ingsw.am21.codex.connection.server.RMI;
}
