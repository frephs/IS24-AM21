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
  public LocalModelContainer getLocalModel() {
    return null;
  }

  @Override
  public void postNotification(
    NotificationType notificationType,
    String message
  ) {
    System.out.println(
      CliUtils.colorize(
        "[" + id + "] " + message,
        notificationType.getColor(),
        ColorStyle.NORMAL
      )
    );
  }

  @Override
  public void postNotification(Notification notification) {}

  @Override
  public void postNotification(
    NotificationType notificationType,
    String[] messages,
    Colorable colorable,
    int colorableIndex
  ) {}

  @Override
  public void listGames() {}

  @Override
  public void drawAvailableGames() {}

  @Override
  public void drawLobby() {}

  @Override
  public void drawGameBoard() {}

  @Override
  public void drawLeaderBoard() {}

  @Override
  public void drawPlayerBoards() {}

  @Override
  public void drawPlayerBoard(
    String nickname,
    int verticalOffset,
    int horizontalOffset
  ) {}

  @Override
  public void drawGame() {}

  @Override
  public void drawGameOver() {}

  @Override
  public void drawCard(Card card) {}

  @Override
  public void drawHand() {}

  @Override
  public void drawPairs() {}

  @Override
  public void drawAvailableTokenColors() {}

  @Override
  public void drawObjectiveCardChoice() {}

  @Override
  public void drawChatMessage(ChatMessage message) {}
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
