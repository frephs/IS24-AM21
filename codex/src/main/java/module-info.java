module polimi.ingsw.am21.codex {
  requires javafx.controls;
  requires javafx.fxml;
  requires com.google.gson;
  requires org.json;
  requires org.controlsfx.controls;
  requires com.almasb.fxgl.all;
  requires java.rmi;
  requires annotations;
  requires org.apache.commons.lang3;

  opens polimi.ingsw.am21.codex to javafx.fxml, javafx.graphics;
  opens polimi.ingsw.am21.codex.view.GUI to javafx.fxml, javafx.graphics;
  opens polimi.ingsw.am21.codex.view.GUI.utils to javafx.fxml;

  opens polimi.ingsw.am21.codex.model to com.google.gson;
  exports polimi.ingsw.am21.codex;
  exports polimi.ingsw.am21.codex.view.GUI;
  exports polimi.ingsw.am21.codex.connection.server.RMI;
  exports polimi.ingsw.am21.codex.connection;
  exports polimi.ingsw.am21.codex.client.localModel;
  opens polimi.ingsw.am21.codex.connection to javafx.fxml;
  exports polimi.ingsw.am21.codex.client.localModel.remote;
  exports polimi.ingsw.am21.codex.controller.listeners;
  exports polimi.ingsw.am21.codex.controller.messages;
  exports polimi.ingsw.am21.codex.controller.messages.viewUpdate.game;
  exports polimi.ingsw.am21.codex.model;
  exports polimi.ingsw.am21.codex.view;
  exports polimi.ingsw.am21.codex.view.TUI.utils.commons;
  exports polimi.ingsw.am21.codex.model.GameBoard;
  exports polimi.ingsw.am21.codex.model.Cards;
  exports polimi.ingsw.am21.codex.model.Cards.Playable;
  exports polimi.ingsw.am21.codex.model.Cards.Commons;
  exports polimi.ingsw.am21.codex.model.Player;
  exports polimi.ingsw.am21.codex.connection.client;
  exports polimi.ingsw.am21.codex.model.Chat;
  exports polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
}
