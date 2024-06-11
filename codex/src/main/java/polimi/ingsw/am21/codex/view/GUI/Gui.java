package polimi.ingsw.am21.codex.view.GUI;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import polimi.ingsw.am21.codex.client.localModel.GameEntry;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.GUI.utils.ExceptionLoader;
import polimi.ingsw.am21.codex.view.GUI.utils.GuiElement;
import polimi.ingsw.am21.codex.view.GUI.utils.GuiUtils;
import polimi.ingsw.am21.codex.view.GUI.utils.NotificationLoader;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class Gui extends Application implements View {

  private static Gui gui;

  @FXML
  private Text windowTitle;

  @FXML
  private Pane content;

  private static ClientConnectionHandler client;
  private boolean isReady = false;

  public Gui() {
    gui = this;
  }

  public static Gui getInstance() {
    return gui;
  }

  public void setClient(ClientConnectionHandler client) {
    Gui.client = client;
  }

  public void testLobby() {
    //    this.postNotification(NotificationType.CONFIRM, "Attento");
    //    this.postNotification(NotificationType.WARNING, "Attento");
    //    this.postNotification(NotificationType.ERROR, "Attento");
    //    this.postNotification(NotificationType.UPDATE, "Attento");
    //    this.postNotification(NotificationType.RESPONSE, "Attento");

    //    displayException(new RuntimeException("Test exception"));

    //    this.drawAvailableGames(
    //        new ArrayList<>(
    //          Collections.nCopies(
    //            52,
    //            new GameEntry(UUID.randomUUID().toString().substring(0, 3), 2, 4)
    //          )
    //        )
    //      );

    LocalPlayer p1 = new LocalPlayer(UUID.randomUUID());
    LocalPlayer p2 = new LocalPlayer(UUID.randomUUID());
    LocalPlayer p3 = new LocalPlayer(UUID.randomUUID());
    LocalPlayer p4 = new LocalPlayer(UUID.randomUUID());

    p1.setNickname("Player 1");
    p2.setNickname("Player 2");
    p3.setNickname("Player 3");

    p1.setToken(TokenColor.RED);
    p2.setToken(TokenColor.BLUE);
    p4.setToken(TokenColor.YELLOW);

    this.drawAvailableTokenColors(
        Arrays.stream(TokenColor.values()).collect(Collectors.toSet())
      );

    this.drawLobby(
        Map.of(
          UUID.randomUUID(),
          p1,
          UUID.randomUUID(),
          p2,
          UUID.randomUUID(),
          p3,
          UUID.randomUUID(),
          p4
        )
      );
    //    CardsLoader cards = new CardsLoader();
    //
    //    Card cardFromId = cards.getCardFromId(96);
    //    this.drawObjectiveCardChoice(
    //        new CardPair<>(cardFromId, cards.getCardFromId(101))
    //      );
    //    this.drawStarterCardSides(cards.getCardFromId(32));
  }

  public void testGame() {
    LocalPlayer p1 = new LocalPlayer(UUID.randomUUID());
    LocalPlayer p2 = new LocalPlayer(UUID.randomUUID());
    LocalPlayer p3 = new LocalPlayer(UUID.randomUUID());
    LocalPlayer p4 = new LocalPlayer(UUID.randomUUID());

    p1.setNickname("Player 1");
    p2.setNickname("Player 2");
    p3.setNickname("Player 3");
    p4.setNickname("Player 4");

    p1.setToken(TokenColor.RED);
    p2.setToken(TokenColor.BLUE);
    p3.setToken(TokenColor.GREEN);
    p4.setToken(TokenColor.YELLOW);

    p1.setPoints(10);
    p2.setPoints(40);
    p3.setPoints(30);
    p4.setPoints(20);

    p1.addResource(ResourceType.ANIMAL, 2);
    p2.addResource(ResourceType.INSECT, 2);
    p3.addResource(ResourceType.PLANT, 2);
    p4.addResource(ResourceType.FUNGI, 2);

    this.drawGame(List.of(p1, p2, p3, p4));
    this.drawLeaderBoard(List.of(p1, p2, p3, p4));

    CardsLoader cards = new CardsLoader();
    this.drawPairs(
        new CardPair<>(cards.getCardFromId(7), cards.getCardFromId(17)),
        new CardPair<>(cards.getCardFromId(66), cards.getCardFromId(80))
      );

    this.drawCardDecks(
        (PlayableCard) cards.getCardFromId(1),
        (PlayableCard) cards.getCardFromId(41)
      );
    this.drawComonObjectiveCards(
        new CardPair<>(cards.getCardFromId(90), cards.getCardFromId(91))
      );
  }

  private static Stage primaryStage;
  private static NotificationLoader notificationLoader;
  private static ExceptionLoader exceptionLoader;
  private static Scene scene;

  @Override
  public void start(Stage primaryStage) {
    Gui.primaryStage = primaryStage;

    try {
      Parent root = FXMLLoader.load(
        Objects.requireNonNull(Gui.class.getResource("LoadingWindow.fxml"))
      );

      Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

      scene = new Scene(root);

      primaryStage.setX((screenBounds.getWidth() - 400) / 2);
      primaryStage.setY((screenBounds.getHeight() - 400) / 2);

      notificationLoader = new NotificationLoader(new Stage());
      exceptionLoader = new ExceptionLoader(new Stage());

      primaryStage.setTitle("Codex Naturalis");
      primaryStage.setScene(scene);
      primaryStage.setMaximized(true);

      primaryStage.show();
      //drawAvailableGames(new ArrayList<>());
      //testLobby();
      //testGame();

    } catch (IOException e) {
      Cli.getInstance().displayException(e);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Helper function: sets the #content container to the fxml template provided
   * @param fxmlPath the path of the template to load in the #contant container
   * */
  public void loadSceneFXML(String fxmlPath, String containerId) {
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

    ((Pane) scene.lookup(containerId)).getChildren().clear();
    ((Pane) scene.lookup(containerId)).getChildren().add(content);
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
    if (element != null) return loadImage(element.getImagePath());
    else return new ImageView();
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
    image.setAlignment(Pos.CENTER);
    return image;
  }

  @Override
  public void postNotification(
    NotificationType notificationType,
    String message
  ) {
    Platform.runLater(() -> {
      try {
        notificationLoader.addNotification(notificationType, message);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public void postNotification(Notification notification) {
    postNotification(
      notification.getNotificationType(),
      notification.getMessage()
    );
  }

  @Override
  public void postNotification(
    NotificationType notificationType,
    String[] messages,
    Colorable colorable,
    int colorableIndex
  ) {}

  @Override
  public void displayException(Exception exception) {
    try {
      exceptionLoader.loadException(exception);
      Cli.getInstance().displayException(exception);
    } catch (IOException e) {
      Cli.getInstance().displayException(e);
    }
  }

  private static Node loadGameEntry(GameEntry game) throws IOException {
    Node gameEntry = FXMLLoader.load(
      Objects.requireNonNull(Gui.class.getResource("LobbyMenuGameEntry.fxml"))
    );

    // Set the game details
    ((Text) gameEntry.lookup("#game-entry-id")).setText(game.getGameId());
    ((Text) gameEntry.lookup("#game-entry-players")).setText(
        game.getCurrentPlayers() + "/" + game.getMaxPlayers()
      );

    gameEntry.setOnMouseClicked(
      (MouseEvent event) -> client.connectToGame(game.getGameId())
    );

    return gameEntry;
  }

  @Override
  public void drawAvailableGames(List<GameEntry> games) {
    Platform.runLater(() -> {
      Parent root = null;
      try {
        root = FXMLLoader.load(
          Objects.requireNonNull(Gui.class.getResource("WindowScene.fxml"))
        );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

      scene = new Scene(
        root,
        screenBounds.getWidth(),
        screenBounds.getHeight()
      );

      primaryStage.setScene(scene);

      loadSceneFXML("LobbyMenu.fxml", "#content");
      ((Text) scene.lookup("#window-title")).setText("Menu");
    });

    Platform.runLater(() -> {
      GridPane gameEntryContainer = (GridPane) ((ScrollPane) scene.lookup(
          "#game-entry-scroll"
        )).getContent()
        .lookup("#game-entry-container");

      if (gameEntryContainer != null) {
        gameEntryContainer.getChildren().clear();
      } else {
        System.out.println("game-entry-container not found in the scene.");
        return;
      }

      gameEntryContainer.getChildren().clear();

      scene
        .lookup("#create-game-button")
        .setOnMouseClicked(
          (MouseEvent event) ->
            client.createGame(
              ((TextField) scene.lookup("#game-id-input")).getText(),
              ((ChoiceBox<?>) scene.lookup("#player-number-input")).getValue()
                  .toString()
                  .isEmpty()
                ? 2
                : Integer.parseInt(
                  ((ChoiceBox<?>) scene.lookup(
                      "#player-number-input"
                    )).getValue()
                    .toString()
                )
            )
        );

      for (int i = 0; i < games.size(); i++) {
        GameEntry game = games.get(i);
        try {
          Node gameEntry = loadGameEntry(game);
          int columnIndex = i % 3;
          int rowIndex = i / 3;
          if (rowIndex > 0 && columnIndex == 0) {
            gameEntryContainer.addRow(rowIndex, gameEntry);
          } else {
            gameEntryContainer.add(gameEntry, columnIndex, rowIndex);
          }
        } catch (IOException e) {
          displayException(e);
          throw new RuntimeException(e);
        }
      }
    });
  }

  @Override
  public void drawAvailableTokenColors(Set<TokenColor> tokenColors) {
    Platform.runLater(() -> {
      loadSceneFXML("LobbyToken.fxml", "#content");

      ((Text) scene.lookup("#window-title")).setText(
          "Lobby of the game " //+ client.getGameId() //TODO add game name
        );

      Node tokenContainer = scene.lookup("#token-container");
      ((HBox) tokenContainer).getChildren().clear();

      ((HBox) tokenContainer).getChildren()
        .addAll(
          tokenColors
            .stream()
            .map(tokenColor -> {
              ImageView tokenColorImage = loadImage(tokenColor);

              tokenColorImage.setStyle("-fx-cursor: hand");

              tokenColorImage.setFitHeight(60);
              tokenColorImage.setFitWidth(60);

              tokenColorImage.setOnMouseClicked((MouseEvent event) -> {
                client.lobbySetToken(tokenColor);
                //TODO change scene as callback of message
              });

              return wrapAndBorder(tokenColorImage);
            })
            .toList()
        );

      ((HBox) tokenContainer).setAlignment(Pos.CENTER);
    });
  }

  @Override
  public void drawLobby(Map<UUID, LocalPlayer> players) {
    Platform.runLater(() -> {
      loadSceneFXML("LobbyPlayers.fxml", "#side-content");
      GridPane playerGrid =
        ((GridPane) scene.lookup("#lobby-player-container"));
      for (int i = 0; i < players.size(); i++) {
        LocalPlayer player = (LocalPlayer) players.values().toArray()[i];

        ImageView token = loadImage(player.getToken());
        token.setPreserveRatio(true);
        token.setFitHeight(25);

        HBox tokenContainer = new HBox(token);
        tokenContainer.alignmentProperty().set(Pos.CENTER_RIGHT);
        //TODO fix alignment
        playerGrid.add(token, 0, i);

        Label nickname = new Label(
          (player.getNickname()) != null
            ? player.getNickname()
            : players.keySet().toArray()[i].toString()
        );
        nickname.alignmentProperty().setValue(Pos.CENTER);
        //TODO fix aligment
        playerGrid.add(nickname, 1, i);
      }
    });
  }

  @Override
  public void drawLeaderBoard(List<LocalPlayer> players) {
    GridPane container = (GridPane) scene.lookup("#leaderboard-grid");
    container.getChildren().clear();

    List<LocalPlayer> sortedPlayers = players
      .stream()
      .sorted((p1, p2) -> p2.getPoints() - p1.getPoints())
      .toList();
    for (int i = 0; i < sortedPlayers.size(); i++) {
      LocalPlayer player = sortedPlayers.get(i);

      Label nickname = new Label(player.getNickname());
      nickname.getStyleClass().add("leaderboard-entry");
      container.add(nickname, 0, i);

      Label points = new Label(String.valueOf(player.getPoints()));
      points.getStyleClass().add("leaderboard-entry");
      container.add(points, 1, i);

      HBox resources = new HBox();
      resources.getStyleClass().add("leaderboard-entry");
      player
        .getResources()
        .forEach((resource, amount) -> {
          // Base container for each entry
          HBox entryContainer = new HBox();
          entryContainer.getStyleClass().add("leaderboard-entry");

          // Resource icon
          ImageView imageView = loadImage(resource);
          imageView.setPreserveRatio(true);
          imageView.setFitHeight(25);
          entryContainer.getChildren().add(imageView);

          // Amount label
          Label label = new Label(amount.toString());
          label.getStyleClass().add(GuiUtils.getColorClass(resource));
          label.setMaxWidth(Double.MAX_VALUE);
          entryContainer.getChildren().add(label);

          resources.getChildren().add(entryContainer);
          // TODO fix alignment
        });
      container.add(resources, 2, i);

      HBox objects = new HBox();
      objects.getStyleClass().add("leaderboard-entry");
      player
        .getObjects()
        .forEach((object, amount) -> {
          // Base container for each entry
          HBox entryContainer = new HBox();
          entryContainer.getStyleClass().add("leaderboard-entry");

          // Object icon
          ImageView imageView = loadImage(object);
          imageView.setPreserveRatio(true);
          imageView.setFitHeight(25);
          entryContainer.getChildren().add(imageView);

          // Amount label
          Label label = new Label(amount.toString());
          label.setMaxWidth(Double.MAX_VALUE);
          entryContainer.getChildren().add(label);

          objects.getChildren().add(entryContainer);
          // TODO fix alignment
        });
      container.add(objects, 3, i);
    }
  }

  @Override
  public void drawNicknameChoice() {
    Platform.runLater(() -> {
      loadSceneFXML("LobbyNickname.fxml", "#content");
      ((Button) scene.lookup("#nickname-submit-button")).setOnMouseClicked(
          (MouseEvent event) -> {
            String nickname =
              ((TextField) (scene.lookup("#nickname-input"))).getText();
            client.lobbySetNickname(nickname);
            client.getObjectiveCards();
          }
        );
    });
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
    loadSceneFXML("GameBoard.fxml", "#content");
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
  ) {
    GridPane resourceCardContainer = (GridPane) scene.lookup(
      "#resource-card-pair"
    );
    GridPane goldCardContainer = (GridPane) scene.lookup("#gold-card-pair");

    resourceCardContainer.getChildren().clear();
    goldCardContainer.getChildren().clear();

    // TODO how do we want to display two sides?
    List<ImageView> images = List.of(
      loadCardImage(resourceCards.getFirst(), CardSideType.FRONT),
      loadCardImage(resourceCards.getSecond(), CardSideType.FRONT),
      loadCardImage(goldCards.getFirst(), CardSideType.FRONT),
      loadCardImage(goldCards.getSecond(), CardSideType.FRONT)
    );

    images.forEach(image -> {
      image.setPreserveRatio(true);
      image.setFitWidth(150);
      image.setStyle("-fx-cursor: hand");
    });

    resourceCardContainer.add(wrapAndBorder(images.get(0)), 0, 0);
    resourceCardContainer.add(wrapAndBorder(images.get(1)), 1, 0);

    goldCardContainer.add(wrapAndBorder(images.get(2)), 0, 0);
    goldCardContainer.add(wrapAndBorder(images.get(3)), 1, 0);
  }

  @Override
  public void drawObjectiveCardChoice(CardPair<Card> cardPair) {
    Platform.runLater(() -> {
      loadSceneFXML("LobbyChooseObjective.fxml", "#content");

      ((Text) scene.lookup("#window-title")).setText("Lobby");

      ImageView first = loadImage(cardPair.getFirst());
      ImageView second = loadImage(cardPair.getSecond());

      first.setOnMouseClicked((MouseEvent event) -> {
        client.lobbyChooseObjectiveCard(true);
        client.getStarterCard();
      });

      second.setOnMouseClicked((MouseEvent event) -> {
        client.lobbyChooseObjectiveCard(false);
        client.getStarterCard();
      });

      first.setPreserveRatio(true);
      second.setPreserveRatio(true);

      first.setFitWidth(150);
      second.setFitWidth(150);

      first.setStyle("-fx-cursor: hand");
      second.setStyle("-fx-cursor: hand");

      Node objectiveContainer = scene.lookup("#objective-container");
      ((HBox) objectiveContainer).getChildren().add(wrapAndBorder(first));
      ((HBox) objectiveContainer).getChildren().add(wrapAndBorder(second));
    });
  }

  @Override
  public void drawStarterCardSides(Card cardId) {
    Platform.runLater(() -> {
      loadSceneFXML("LobbyChooseStarterCardSide.fxml", "#content");

      ((Text) scene.lookup("#window-title")).setText("Lobby");

      ImageView front = loadCardImage(cardId, CardSideType.FRONT);
      ImageView back = loadCardImage(cardId, CardSideType.BACK);

      front.setPreserveRatio(true);
      back.setPreserveRatio(true);

      front.setFitWidth(150);
      back.setFitWidth(150);

      front.setStyle("-fx-cursor: hand");
      back.setStyle("-fx-cursor: hand");

      front.setOnMouseClicked((MouseEvent event) -> {
        client.lobbyJoinGame(CardSideType.FRONT);
        //TODO change scene as callback of message
      });

      back.setOnMouseClicked((MouseEvent event) -> {
        client.lobbyJoinGame(CardSideType.BACK);
        //TODO change scene as callback of message
      });

      Node starterCardSidesContainer = scene.lookup("#starter-side-container");
      ((HBox) starterCardSidesContainer).getChildren()
        .add(wrapAndBorder(front));
      ((HBox) starterCardSidesContainer).getChildren().add(wrapAndBorder(back));
    });
  }

  @Override
  public void drawWinner(String nickname) {
    // TODO
  }

  @Override
  public void drawChatMessage(ChatMessage message) {
    // TODO
  }

  @Override
  public void drawComonObjectiveCards(CardPair<Card> cardPair) {
    GridPane cardsContainer = (GridPane) scene.lookup(
      "#common-objective-cards"
    );
    cardsContainer.getChildren().clear();

    // TODO how do we want to display two sides?
    List<ImageView> images = List.of(
      loadCardImage(cardPair.getFirst(), CardSideType.FRONT),
      loadCardImage(cardPair.getSecond(), CardSideType.FRONT)
    );

    images.forEach(image -> {
      image.setPreserveRatio(true);
      image.setFitWidth(150);
      image.setStyle("-fx-cursor: hand");
    });

    cardsContainer.add(wrapAndBorder(images.get(0)), 0, 0);
    cardsContainer.add(wrapAndBorder(images.get(1)), 1, 0);
  }

  @Override
  public void drawCardDecks(
    PlayableCard firstResourceCard,
    PlayableCard firstGoldCard
  ) {
    HBox resourceCardsDeck = (HBox) scene.lookup("#resource-cards-deck");
    HBox goldCardsDeck = (HBox) scene.lookup("#gold-cards-deck");

    resourceCardsDeck.getChildren().clear();
    if (firstResourceCard != null) {
      ImageView resource = loadCardImage(firstResourceCard, CardSideType.BACK);
      resource.setPreserveRatio(true);
      resource.setFitWidth(150);
      resource.setStyle("-fx-cursor: hand");

      resourceCardsDeck.getChildren().add(wrapAndBorder(resource));
    }

    goldCardsDeck.getChildren().clear();
    if (firstGoldCard != null) {
      ImageView gold = loadCardImage(firstGoldCard, CardSideType.BACK);
      gold.setPreserveRatio(true);
      gold.setFitWidth(150);
      gold.setStyle("-fx-cursor: hand");

      goldCardsDeck.getChildren().add(wrapAndBorder(gold));
    }
  }

  public boolean isInitialized() {
    return (
      scene != null && notificationLoader != null && exceptionLoader != null
    );
  }
}
