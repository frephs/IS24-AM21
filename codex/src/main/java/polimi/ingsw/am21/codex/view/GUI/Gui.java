package polimi.ingsw.am21.codex.view.GUI;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import polimi.ingsw.am21.codex.client.localModel.GameEntry;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.GUI.utils.GuiElement;
import polimi.ingsw.am21.codex.view.GUI.utils.NotificationLoader;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class Gui extends Application implements View {

  private ClientConnectionHandler client;

  public Gui(ClientConnectionHandler client) {
    this.client = client;

    this.start(new Stage());
  }

  public void testLobby() {
    this.drawAvailableGames(
        List.of(
          new GameEntry("Game 1", 2, 4),
          new GameEntry("Game 2", 2, 4),
          new GameEntry("Game 3", 2, 4),
          new GameEntry("Game 4", 2, 4)
        )
      );

    this.postNotification(NotificationType.CONFIRM, "Attento");
    this.postNotification(NotificationType.WARNING, "Attento");
    this.postNotification(NotificationType.ERROR, "Attento");
    this.postNotification(NotificationType.UPDATE, "Attento");
    this.postNotification(NotificationType.RESPONSE, "Attento");

    this.drawAvailableTokenColors(
        Arrays.stream(TokenColor.values()).collect(Collectors.toSet())
      );

    CardsLoader cards = new CardsLoader();

    Card cardFromId = cards.getCardFromId(96);
    this.drawObjectiveCardChoice(
        new CardPair<>(cardFromId, cards.getCardFromId(101))
      );
    this.drawStarterCardSides(cards.getCardFromId(32));
    // Add assertions here
  }

  public Gui() {}

  private NotificationLoader notificationLoader;

  @FXML
  Text windowTitle;

  Scene scene;

  @Override
  public void start(Stage primaryStage) {
    try {
      Parent root = FXMLLoader.load(
        Objects.requireNonNull(Gui.class.getResource("WindowScene.fxml"))
      );
      scene = new Scene(root, 800, 600);
      notificationLoader = new NotificationLoader(new Stage());
      primaryStage.setTitle("Codex Naturalis");
      primaryStage.setScene(scene);
      primaryStage.setMaximized(true);
      primaryStage.show();
      testLobby();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Helper function: sets the #content container to the fxml template provided
   * @param fxmlPath the path of the template to load in the #contant container
   * */
  public void loadSceneFXML(String fxmlPath) {
    // load the lobby menu
    Node content;
    try {
      content = FXMLLoader.load(
        Objects.requireNonNull(Gui.class.getResource(fxmlPath))
      );
    } catch (IOException e) {
      displayException(e);
      throw new RuntimeException(e);
    }
    //    ((Pane) scene.lookup("#content")).getChildren().clear();
    ((Pane) scene.lookup("#content")).getChildren().add(content);
  }

  private ImageView loadImage(String path) {
    return new ImageView(
      new Image(
        Objects.requireNonNull(
          getClass().getResource(GuiElement.getBasePath() + path)
        ).toExternalForm()
      )
    );
  }

  /**
   * @return an Image view element containing the GuiElement provided
   */
  @FXML
  private ImageView loadImage(GuiElement element) {
    return loadImage(element.getImagePath());
  }

  /**
   *
   * */
  @FXML
  private ImageView loadCardImage(Card card, CardSideType side) {
    return switch (side) {
      case FRONT -> loadImage(card.getImagePath(CardSideType.FRONT));
      case BACK -> loadImage(card.getImagePath(CardSideType.BACK));
    };
  }

  @FXML
  private static HBox wrapAndBorder(ImageView imageView) {
    HBox image = new HBox(imageView);
    HBox.setMargin(image, new Insets(0, 10, 10, 10));
    image.setPadding(new Insets(10, 10, 10, 10));
    image.getStyleClass().add("bordered");
    return image;
  }

  @Override
  public void postNotification(
    NotificationType notificationType,
    String message
  ) {
    try {
      notificationLoader.addNotification(notificationType, message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
  public void displayException(Exception e) {}

  private static Node loadGameEntry(GameEntry game) throws IOException {
    Node gameEntry = FXMLLoader.load(
      Objects.requireNonNull(Gui.class.getResource("LobbyMenuGameEntry.fxml"))
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
    loadSceneFXML("LobbyMenu.fxml");

    ((Text) scene.lookup("#window-title")).setText("Menu");

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
  public void drawAvailableTokenColors(Set<TokenColor> tokenColors) {
    loadSceneFXML("LobbyToken.fxml");

    ((Text) scene.lookup("#window-title")).setText("Lobby");

    Node tokenContainer = scene.lookup("#token-container");
    ((HBox) tokenContainer).getChildren().clear();

    ((HBox) tokenContainer).getChildren()
      .addAll(
        Arrays.stream(TokenColor.values())
          .map(tokenColor -> {
            ImageView imageView = loadImage(tokenColor);

            imageView.setStyle("-fx-cursor: hand");

            imageView.setFitHeight(60);
            imageView.setFitWidth(60);

            //TODO ADD events

            return wrapAndBorder(imageView);
          })
          .toList()
      );

    ((HBox) tokenContainer).setAlignment(Pos.CENTER);
  }

  @Override
  public void drawLobby(Map<UUID, LocalPlayer> players) {
    // TODO
  }

  @Override
  public void drawLeaderBoard(List<LocalPlayer> players) {
    // TODO
  }

  @Override
  public void drawPlayerBoards(List<LocalPlayer> players) {
    // TODO
  }

  @Override
  public void drawPlayerBoard(LocalPlayer player) {
    // TODO
  }

  @Override
  public void drawCardDrawn(DrawingDeckType deck, Card card) {
    // TODO
  }

  @Override
  public void drawCardDrawn(DrawingDeckType deck) {
    // TODO
  }

  @Override
  public void drawCardPlacement(
    Card card,
    CardSideType side,
    Position position
  ) {}

  @Override
  public void drawGame(List<LocalPlayer> players) {
    // TODO
  }

  @Override
  public void drawGameOver(List<LocalPlayer> players) {
    // TODO
  }

  @Override
  public void drawCard(Card card) {
    // TODO
  }

  @Override
  public void drawHand(List<Card> hand) {
    // TODO
  }

  @Override
  public void drawPairs(
    CardPair<Card> resourceCards,
    CardPair<Card> goldCards
  ) {}

  @Override
  public void drawObjectiveCardChoice(CardPair<Card> cardPair) {
    loadSceneFXML("LobbyChooseObjective.fxml");

    ((Text) scene.lookup("#window-title")).setText("Lobby");

    ImageView first = loadImage(cardPair.getFirst());
    ImageView second = loadImage(cardPair.getSecond());

    first.setPreserveRatio(true);
    second.setPreserveRatio(true);

    first.setFitWidth(150);
    second.setFitWidth(150);

    first.setStyle("-fx-cursor: hand");
    second.setStyle("-fx-cursor: hand");

    Node objectiveContainer = scene.lookup("#objective-container");
    ((HBox) objectiveContainer).getChildren().add(wrapAndBorder(first));
    ((HBox) objectiveContainer).getChildren().add(wrapAndBorder(second));
  }

  @Override
  public void drawStarterCardSides(Card cardId) {
    loadSceneFXML("LobbyChooseStarterCardSide.fxml");

    ((Text) scene.lookup("#window-title")).setText("Lobby");

    ImageView front = loadCardImage(cardId, CardSideType.FRONT);
    ImageView back = loadCardImage(cardId, CardSideType.BACK);

    front.setPreserveRatio(true);
    back.setPreserveRatio(true);

    front.setFitWidth(150);
    back.setFitWidth(150);

    front.setStyle("-fx-cursor: hand");
    back.setStyle("-fx-cursor: hand");

    Node starterCardSidesContainer = scene.lookup("#starter-side-container");
    ((HBox) starterCardSidesContainer).getChildren().add(wrapAndBorder(front));
    ((HBox) starterCardSidesContainer).getChildren().add(wrapAndBorder(back));
  }

  @Override
  public void drawWinner(String nickname) {
    // TODO
  }

  @Override
  public void drawChatMessage(ChatMessage message) {
    // TODO
  }
}
