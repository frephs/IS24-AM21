package polimi.ingsw.am21.codex.view.GUI;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import polimi.ingsw.am21.codex.client.localModel.GameEntry;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class Gui extends Application implements View {

  private final LocalModelContainer localModelContainer =
    new LocalModelContainer(this);

  @FXML
  Text windowTitle;

  Scene scene;

  @Override
  public void start(Stage primaryStage) {
    try {
      Parent root = FXMLLoader.load(Gui.class.getResource("WindowScene.fxml"));

      scene = new Scene(root, 800, 600);
      primaryStage.setTitle("Codex Naturalis");

      primaryStage.setScene(scene);
      primaryStage.show();

      // TEST
      drawAvailableGames(
        List.of(
          new GameEntry("Game 1", 2, 4),
          new GameEntry("Game 2", 2, 4),
          new GameEntry("Game 3", 2, 4),
          new GameEntry("Game 4", 2, 4)
        )
      );
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void postNotification(
    NotificationType notificationType,
    String message
  ) {}

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
  public void displayException(Exception e) {}

  private static Node loadGameEntry(GameEntry game) throws IOException {
    Node gameEntry = FXMLLoader.load(
      Gui.class.getResource("LobbyMenuGameEntry.fxml")
    );

    // Set the game details
    ((Text) gameEntry.lookup("#game-entry-id")).setText(game.getGameId());
    ((Text) gameEntry.lookup("#game-entry-players")).setText(
        game.getCurrentPlayers() + "/" + game.getMaxPlayers()
      );

    gameEntry.setOnMouseClicked((MouseEvent event) -> {});

    return gameEntry;
  }

  @Override
  public void drawAvailableGames(List<GameEntry> games) {
    windowTitle = (Text) scene.lookup("#window-title");
    windowTitle.setText("Codex");

    // load the lobby menu
    Node content = null;
    try {
      content = FXMLLoader.load(Gui.class.getResource("LobbyMenu.fxml"));
    } catch (IOException e) {
      displayException(e);
      throw new RuntimeException(e);
    }
    ((Pane) scene.lookup("#content")).getChildren().add(content);

    // clear the grid
    ((GridPane) scene.lookup("#game-entry-container")).getChildren().clear();

    for (int i = 0; i < games.size(); i++) {
      GameEntry game = games.get(i);
      try {
        Node gameEntry = loadGameEntry(game);
        int columnIndex = i % 3;
        int rowIndex = i / 3;
        ((GridPane) scene.lookup("#game-entry-container")).add(
            gameEntry,
            columnIndex,
            rowIndex
          );
      } catch (IOException e) {
        displayException(e);
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void drawAvailableTokenColors(Set<TokenColor> tokenColors) {}

  @Override
  public void drawLobby(Map<UUID, LocalPlayer> players) {}

  @Override
  public void drawLeaderBoard(List<LocalPlayer> players) {}

  @Override
  public void drawPlayerBoards(List<LocalPlayer> players) {}

  @Override
  public void drawPlayerBoard(LocalPlayer player) {}

  @Override
  public void drawCardDrawn(DrawingDeckType deck, Card card) {}

  @Override
  public void drawCardDrawn(DrawingDeckType deck) {}

  @Override
  public void drawCardPlacement(
    Card card,
    CardSideType side,
    Position position
  ) {}

  @Override
  public void drawGame(List<LocalPlayer> players) {}

  @Override
  public void drawGameOver(List<LocalPlayer> players) {}

  @Override
  public void drawCard(Card card) {}

  @Override
  public void drawHand(List<Card> hand) {}

  @Override
  public void drawPairs(
    CardPair<Card> resourceCards,
    CardPair<Card> goldCards
  ) {}

  @Override
  public void drawObjectiveCardChoice(CardPair<Card> cardPair) {}

  @Override
  public void drawStarterCardSides(Card cardId) {}

  @Override
  public void drawWinner(String nickname) {}

  @Override
  public void drawChatMessage(ChatMessage message) {}
}
