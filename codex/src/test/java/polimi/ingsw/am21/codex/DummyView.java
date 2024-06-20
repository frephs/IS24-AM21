package polimi.ingsw.am21.codex;

import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.TUI.utils.CliUtils;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class DummyView implements View {

  final String id;

  Cli.Options options;

  public DummyView(String id) {
    super();
    this.id = id;
    this.options = new Cli.Options(true);
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
  public void connected() {}

  @Override
  public void displayException(Exception e) {
    CliUtils.colorize(
      "[" + id + "] " + e.getMessage(),
      NotificationType.ERROR.getColor(),
      ColorStyle.NORMAL
    );

    e.printStackTrace();
  }

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
  public void drawPlayerBoard(String nickname) {}

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
  public void drawChatMessage(ChatMessage message) {
    System.out.println(
      CliUtils.colorize(
        "[" +
        id +
        "] received a message from: " +
        message.getSender() +
        ": " +
        message.getContent(),
        NotificationType.UPDATE.getColor(),
        ColorStyle.NORMAL
      )
    );
  }

  @Override
  public void drawCommonObjectiveCards() {}

  @Override
  public void drawPlayerObjective() {}

  @Override
  public void drawCardDecks() {}

  @Override
  public void drawNicknameChoice() {}

  @Override
  public void drawStarterCardSides() {}
}
