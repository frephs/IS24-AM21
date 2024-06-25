package polimi.ingsw.am21.codex;

import javafx.util.Pair;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.controller.listeners.FullUserGameContext;
import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.TUI.utils.CliUtils;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class DummyView extends Cli {

  final String id;
  Cli.Options options;

  public DummyView(String id) {
    super();
    this.id = id;
    new Cli.Options(true);
  }

  @Override
  public void setClient(ClientConnectionHandler client) {}

  @Override
  public void printUpdate(String string) {
    System.out.println(
      "\r" +
      "[ " +
      id +
      " ] " +
      string +
      " ".repeat(string.length() <= 100 ? 100 - string.length() : 0)
    );
  }

  @Override
  public void displayException(Exception e) {
    System.out.println("[" + id + " ]");
    super.displayException(e);
  }
}
