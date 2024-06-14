package polimi.ingsw.am21.codex.view.GUI;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.GUI.utils.*;
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
  private static LocalModelContainer localModel;
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

  public void setLocalModel(LocalModelContainer localModel) {
    Gui.localModel = localModel;
  }

  /** The side of the hand the player is currently looking at */
  private CardSideType visibleHandSide = CardSideType.FRONT;
  /** The last hand that has been displayed, used to re-render it when flipped */
  private List<Card> lastHand = new ArrayList<>();
  /**
   * The index of the hand that is currently selected, null otherwise
   */
  private Integer selectedHandIndex = null;
  /**
   * A boolean that keeps track of whether the current player has place their card
   * for their turn
   */
  private boolean hasPlacedCard = false;

  // TODO track what user is being displayed

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
    drawGameWindow();

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

    //    this.drawGame(List.of(p1, p2, p3, p4));
    //    this.drawLeaderBoard(List.of(p1, p2, p3, p4));

    CardsLoader cards = new CardsLoader();
    //    this.drawPairs(
    //        new CardPair<>(cards.getCardFromId(7), cards.getCardFromId(17)),
    //        new CardPair<>(cards.getCardFromId(66), cards.getCardFromId(80))
    //      );
    //
    //    this.drawCardDecks(
    //        (PlayableCard) cards.getCardFromId(1),
    //        (PlayableCard) cards.getCardFromId(41)
    //      );
    //    this.drawComonObjectiveCards(
    //        new CardPair<>(cards.getCardFromId(90), cards.getCardFromId(91))
    //      );

    p1.setAvailableSpots(new HashSet<>(List.of(new Position(0, 1))));

    this.drawPlayerBoards(List.of(p1));
    this.drawPlayerBoard(p1);
    this.drawCardPlacement(
        cards.getCardFromId(81),
        CardSideType.BACK,
        new Position(0, 0),
        p1.getAvailableSpots(),
        p1.getForbiddenSpots()
      );
    this.drawCardPlacement(
        cards.getCardFromId(1),
        CardSideType.BACK,
        new Position(1, 0),
        p1.getAvailableSpots(),
        p1.getForbiddenSpots()
      );
    this.drawHand(
        List.of(
          cards.getCardFromId(1),
          cards.getCardFromId(81),
          cards.getCardFromId(2)
        )
      );
    this.drawPlayerObjective(cards.getCardFromId(100));
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

      primaryStage.setX((screenBounds.getWidth() - 405) / 2);
      primaryStage.setY((screenBounds.getHeight() - 405) / 2);

      notificationLoader = new NotificationLoader(new Stage());
      exceptionLoader = new ExceptionLoader(new Stage());

      primaryStage.setTitle("Codex Naturalis");
      primaryStage.setScene(scene);

      primaryStage.show();
      //drawAvailableGames(new ArrayList<>());
      //testLobby();
      //      testGame();
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
   * @param containerId the container to lead the scene in (e.g. #content, #side-content)
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

  private void drawGameWindow() {
    try {
      Parent root = FXMLLoader.load(
        Objects.requireNonNull(Gui.class.getResource("WindowScene.fxml"))
      );

      Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

      scene = new Scene(
        root,
        screenBounds.getWidth(),
        screenBounds.getHeight()
      );

      primaryStage.setScene(scene);
      primaryStage.setMaximized(true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @return an Image view element containing the GuiElement provided
   */
  private ImageView loadImage(GuiElement element) {
    if (element != null) return loadImage(element.getImagePath());
    else return new ImageView();
  }

  /**
   * Loads an image view that represents the provided card
   */
  private ImageView loadCardImage(Card card, CardSideType side) {
    return switch (side) {
      case FRONT -> loadImage(card.getImagePath(CardSideType.FRONT));
      case BACK -> loadImage(card.getImagePath(CardSideType.BACK));
    };
  }

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
        notificationLoader.addNotification(
          notificationType,
          message.replace(": ", ":\n").replace(".", "")
        );
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
      drawGameWindow();

      loadSceneFXML("LobbyMenu.fxml", "#content");
      ((Text) scene.lookup("#window-title")).setText("Menu");

      TextField input = (TextField) scene.lookup("#game-id-input");
      input
        .textProperty()
        .addListener((observable, oldValue, newValue) -> {
          if (newValue.contains(" ")) {
            input.setText(newValue.replaceAll("\\s", ""));
          }
        });
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
          (player.getNickname()) != null ? player.getNickname() : "<pending>"
        );
        nickname.alignmentProperty().setValue(Pos.CENTER);
        //TODO fix aligment
        playerGrid.add(nickname, 1, i);
      }
    });
  }

  @Override
  public void drawLeaderBoard(List<LocalPlayer> players) {
    Platform.runLater(() -> {
      GridPane container = (GridPane) scene.lookup("#leaderboard-grid");
      container.getChildren().clear();

      List<LocalPlayer> sortedPlayers = players
        .stream()
        .sorted((p1, p2) -> p2.getPoints() - p1.getPoints())
        .toList();
      for (int i = 0; i < sortedPlayers.size(); i++) {
        LocalPlayer player = sortedPlayers.get(i);

        ImageView token = loadImage(player.getToken());
        token.setPreserveRatio(true);
        token.setFitHeight(25);

        Label nickname = new Label(player.getNickname());
        nickname.getStyleClass().add("leaderboard-entry");

        Label points = new Label(String.valueOf(player.getPoints()));
        points.getStyleClass().add("leaderboard-entry");

        HBox nicknameAndToken = new HBox(token, nickname);
        nicknameAndToken.setAlignment(Pos.CENTER);

        container.addRow(i, nicknameAndToken, points);
      }
    });
  }

  // TODO maybe implement this in the cli
  public void drawResourcesAndObjects(LocalPlayer player) {
    Platform.runLater(() -> {
      HBox container = (HBox) scene.lookup("#player-resources-objects");

      (container).getChildren().clear();

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
        });
      resources.setAlignment(Pos.CENTER);
      container.getChildren().add(resources);

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

          entryContainer.setAlignment(Pos.CENTER);
          objects.getChildren().add(entryContainer);
        });

      objects.setAlignment(Pos.CENTER);
      container.getChildren().add(objects);
    });
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
    // TODO handle other players (switch button etc)
    loadSceneFXML("PlayerBoard.fxml", "#content");
    players
      .stream()
      .filter(
        player ->
          player
            .getNickname()
            .equals(localModel.getLocalGameBoard().getPlayerNickname())
      )
      .findFirst()
      .ifPresent(player -> {
        drawHand(player.getHand());
        drawPlayerObjective(player.getObjectiveCard());
      });

    ChoiceBox<String> playerBoardChoiceBox = (ChoiceBox<String>) scene.lookup(
      "#player-board-choice"
    );

    players.forEach(
      player -> playerBoardChoiceBox.getItems().add(player.getNickname())
    );

    playerBoardChoiceBox.setValue(
      localModel.getLocalGameBoard().getPlayer().getNickname()
    );

    playerBoardChoiceBox.setOnAction(event -> {
      String selectedPlayerNickname = playerBoardChoiceBox.getValue();
      players
        .stream()
        .filter(player -> player.getNickname().equals(selectedPlayerNickname))
        .findFirst()
        .ifPresent(this::drawPlayerBoard);
    });

    players
      .stream()
      .filter(
        player ->
          player
            .getNickname()
            .equals(localModel.getLocalGameBoard().getPlayerNickname())
      )
      .findFirst()
      .ifPresent(player -> drawHand(player.getHand()));
  }

  @Override
  public void drawPlayerBoard(LocalPlayer player) {
    Platform.runLater(() -> {
      ScrollPane scrollPane = (ScrollPane) scene.lookup(
        "#playerboard-scrollpane"
      );
      GridPane gridPane = new GridPane();
      gridPane.setId("playerboard-grid");

      for (int row = 0; row < ViewGridPosition.gridSize; row++) {
        RowConstraints rowConstraint = new RowConstraints();
        rowConstraint.setMinHeight(GridCell.cellHeight);
        rowConstraint.setMaxHeight(GridCell.cellHeight);
        rowConstraint.setPrefHeight(GridCell.cellHeight);
        rowConstraint.setVgrow(Priority.NEVER);
        gridPane.getRowConstraints().add(rowConstraint);
      }
      for (int col = 0; col < ViewGridPosition.gridSize; col++) {
        ColumnConstraints colConstraint = new ColumnConstraints();
        colConstraint.setMinWidth(GridCell.cellWidth);
        colConstraint.setMaxWidth(GridCell.cellWidth);
        colConstraint.setPrefWidth(GridCell.cellWidth);
        colConstraint.setHgrow(Priority.NEVER);
        gridPane.getColumnConstraints().add(colConstraint);
      }

      player
        .getPlayedCards()
        .forEach((position, cardInfo) -> {
          GridCell cell = new GridCell(
            // These cells should never be clickable since we're placing the card right away, but just in case...
            getCellClickHandler(position),
            getCellPlacementActive()
          );

          cell.placeCard(loadCardImage(cardInfo.getKey(), cardInfo.getValue()));

          ViewGridPosition viewPos = new ViewGridPosition(position);
          gridPane.add(cell, viewPos.getCol(), viewPos.getRow());
        });

      drawAvailablePositions(player.getAvailableSpots(), gridPane);
      drawForbiddenPositions(player.getForbiddenSpots(), gridPane);
      drawResourcesAndObjects(player);

      scrollPane.setContent(gridPane);
      //TODO draw resources and objects
    });
  }

  private Runnable getCellClickHandler(Position position) {
    return () -> {
      if (selectedHandIndex != null && visibleHandSide != null) {
        client.placeCard(selectedHandIndex, visibleHandSide, position);
      }
    };
  }

  /**
   * Returns a supplier that determines whether the playerboard should be clickable or not
   */
  private Supplier<Boolean> getCellPlacementActive() {
    return () -> (this.selectedHandIndex != null && canPlayerPlaceCards());
  }

  private boolean canPlayerPlaceCards() {
    return (
      !this.hasPlacedCard &&
      localModel
        .getLocalGameBoard()
        .getCurrentPlayer()
        .getNickname()
        .equals(localModel.getLocalGameBoard().getPlayer().getNickname())
    );
    // TODO && displayedUser == player
  }

  private void toggleHandSide() {
    if (visibleHandSide == CardSideType.FRONT) {
      visibleHandSide = CardSideType.BACK;
    } else {
      visibleHandSide = CardSideType.FRONT;
    }
    selectedHandIndex = null;
    drawHand(lastHand);
  }

  private void drawAvailablePositions(
    Set<Position> positions,
    GridPane gridPane
  ) {
    positions.forEach(position -> {
      GridCell cell = new GridCell(
        getCellClickHandler(position),
        getCellPlacementActive()
      );

      cell.setStatus(GridCellStatus.AVAILABLE);

      ViewGridPosition viewPos = new ViewGridPosition(position);
      gridPane.add(cell, viewPos.getCol(), viewPos.getRow());
    });
  }

  private void drawForbiddenPositions(
    Set<Position> positions,
    GridPane gridPane
  ) {
    positions.forEach(position -> {
      GridCell cell = new GridCell(
        getCellClickHandler(position),
        getCellPlacementActive()
      );

      cell.setStatus(GridCellStatus.FORBIDDEN);

      ViewGridPosition viewPos = new ViewGridPosition(position);
      gridPane.add(cell, viewPos.getCol(), viewPos.getRow());
    });
  }

  @Override
  public void drawCardDrawn(DrawingDeckType deck, Card card) {
    // TODO
    hasPlacedCard = false;
  }

  @Override
  public void drawCardDrawn(DrawingDeckType deck) {
    // TODO
    hasPlacedCard = false;
  }

  @Override
  public void drawCardPlacement(
    Card card,
    CardSideType side,
    Position position,
    Set<Position> availablePositions,
    Set<Position> forbiddenPositions
  ) {
    hasPlacedCard = true;
    Platform.runLater(() -> {
      ViewGridPosition viewPos = new ViewGridPosition(position);
      GridPane gridPane = (GridPane) scene.lookup("#playerboard-grid");

      gridPane
        .getChildren()
        .stream()
        .filter(
          child ->
            GridPane.getRowIndex(child) == viewPos.getRow() &&
            GridPane.getColumnIndex(child) == viewPos.getCol()
        )
        .findFirst()
        .ifPresentOrElse(
          current -> {
            GridCell cell = (GridCell) current;
            cell.placeCard(loadCardImage(card, side));
          },
          () -> {
            GridCell cell = new GridCell(
              getCellClickHandler(position),
              getCellPlacementActive()
            );

            cell.placeCard(loadCardImage(card, side));

            gridPane.add(cell, viewPos.getCol(), viewPos.getRow());
          }
        );

      drawAvailablePositions(availablePositions, gridPane);
      drawForbiddenPositions(forbiddenPositions, gridPane);
      //TODO draw resources and objects
    });
  }

  @Override
  public void drawGame(List<LocalPlayer> players) {
    Platform.runLater(() -> {
      loadSceneFXML("GameBoard.fxml", "#side-content");
      drawChat(players);
      drawGameBoard();
      drawPlayerBoards(players);
      ((Text) scene.lookup("#window-title")).setText(
          "Game " + localModel.getGameId()
        );
    });
  }

  //TODO maybe implement this into the cli

  public void drawGameBoard() {
    drawPairs(
      localModel.getLocalGameBoard().getResourceCards(),
      localModel.getLocalGameBoard().getGoldCards()
    );
    //TODO draw decks
    //TODO draw common objective cards
    drawCommonObjectiveCards(
      localModel.getLocalGameBoard().getCommonObjectives()
    );
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
    Platform.runLater(() -> {
      // Save the hand in case we need to flip it
      lastHand = hand;

      scene
        .lookup("#flip-hand-button")
        .setOnMouseClicked(event -> toggleHandSide());

      for (int i = 0; i < 3; i++) {
        VBox vbox = ((VBox) scene.lookup("#hand-" + i));

        vbox.getStyleClass().add("hand-not-selected-card");
        vbox.getChildren().clear();

        if (i < hand.size()) {
          Card card = hand.get(i);
          ImageView image = loadCardImage(card, visibleHandSide);
          image.setPreserveRatio(true);
          image.setFitWidth(150);

          int finalI = i;
          image.setOnMouseClicked(event -> {
            if (canPlayerPlaceCards()) {
              selectedHandIndex = finalI;

              for (int j = 0; j < hand.size(); j++) {
                // Clear existing class selection
                (scene.lookup("#hand-" + j)).getStyleClass().clear();

                if (finalI == j) (scene.lookup("#hand-" + j)).getStyleClass()
                  .add("hand-selected-card");
                else (scene.lookup("#hand-" + j)).getStyleClass()
                  .add("hand-not-selected-card");
              }
            }
          });

          vbox.getChildren().add(image);
        }
      }
    });
  }

  @Override
  public void drawPairs(
    CardPair<Card> resourceCards,
    CardPair<Card> goldCards
  ) {
    Platform.runLater(() -> {
      VBox commonBoardContainer = (VBox) ((ScrollPane) scene.lookup(
          "#gameboard-container"
        )).getContent();

      HBox resourceCardContainer = (HBox) commonBoardContainer.lookup(
        "#resource-card-pair"
      );

      HBox goldCardContainer = (HBox) commonBoardContainer.lookup(
        "#gold-card-pair"
      );

      goldCardContainer.setPadding(new Insets(5));
      resourceCardContainer.setPadding(new Insets(5));

      resourceCardContainer.setAlignment(Pos.CENTER);
      goldCardContainer.setAlignment(Pos.CENTER);

      resourceCardContainer.getChildren().clear();
      goldCardContainer.getChildren().clear();

      // TODO how do we want to display two sides?
      List<ImageView> images = List.of(
        loadCardImage(resourceCards.getFirst(), CardSideType.FRONT),
        loadCardImage(resourceCards.getSecond(), CardSideType.FRONT),
        loadCardImage(goldCards.getFirst(), CardSideType.FRONT),
        loadCardImage(goldCards.getSecond(), CardSideType.FRONT)
      );

      images
        .get(0)
        .setOnMouseClicked(
          (MouseEvent event) ->
            client.nextTurn(
              DrawingCardSource.CardPairFirstCard,
              DrawingDeckType.RESOURCE
            )
        );
      images
        .get(1)
        .setOnMouseClicked(
          (MouseEvent event) ->
            client.nextTurn(
              DrawingCardSource.CardPairSecondCard,
              DrawingDeckType.RESOURCE
            )
        );
      images
        .get(2)
        .setOnMouseClicked(
          (MouseEvent event) ->
            client.nextTurn(
              DrawingCardSource.CardPairFirstCard,
              DrawingDeckType.GOLD
            )
        );
      images
        .get(3)
        .setOnMouseClicked(
          (MouseEvent event) ->
            client.nextTurn(
              DrawingCardSource.CardPairSecondCard,
              DrawingDeckType.GOLD
            )
        );

      images.forEach(image -> {
        image.setPreserveRatio(true);
        image.setFitWidth(150);
        image.setStyle("-fx-cursor: hand");
      });

      Separator separator1 = new Separator();
      Separator separator2 = new Separator();

      separator1.setOrientation(Orientation.VERTICAL);
      separator2.setOrientation(Orientation.VERTICAL);

      //set a margin for the separator
      HBox.setMargin(separator1, new Insets(0, 10, 0, 10));
      HBox.setMargin(separator2, new Insets(0, 10, 0, 10));

      resourceCardContainer.getChildren().add(images.get(0));
      resourceCardContainer.getChildren().add(separator1);
      resourceCardContainer.getChildren().add(images.get(1));

      goldCardContainer.getChildren().add(images.get(2));
      goldCardContainer.getChildren().add(separator2);
      goldCardContainer.getChildren().add(images.get(3));
    });
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
        loadSceneFXML("LobbyWaitRoom.fxml", "#content");
        client.lobbyJoinGame(CardSideType.FRONT);
      });

      back.setOnMouseClicked((MouseEvent event) -> {
        loadSceneFXML("LobbyWaitRoom.fxml", "#content");
        client.lobbyJoinGame(CardSideType.BACK);
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

  //TODO maybe implement this in CLI
  public void drawChat(List<LocalPlayer> players) {
    loadSceneFXML("Chat.fxml", "#side-content-bottom");
    // add recipients to chat-recipient combo box

    ChoiceBox<String> recipientChoiceBox = (ChoiceBox<String>) scene.lookup(
      "#chat-recipient"
    );

    players.forEach(
      player -> recipientChoiceBox.getItems().add(player.getNickname())
    );

    recipientChoiceBox.getItems().addFirst("Broadcast");

    recipientChoiceBox.setValue("Broadcast");

    ((Button) scene.lookup("#chat-send-button")).setOnMouseClicked(
        (MouseEvent event) -> {
          String recipient =
            ((ChoiceBox<String>) scene.lookup("#chat-recipient")).getValue();
          String message = ((TextField) scene.lookup("#chat-input")).getText();
          ChatMessage chatMessage;

          if (!Objects.equals(recipient, "Broadcast")) {
            chatMessage = new ChatMessage(
              recipient,
              localModel.getLocalGameBoard().getPlayerNickname(),
              message
            );
          } else {
            chatMessage = new ChatMessage(
              localModel.getLocalGameBoard().getPlayerNickname(),
              message
            );
          }
          client.sendChatMessage(chatMessage);
          drawChatMessage(chatMessage);
        }
      );
  }

  public void drawChatMessage(ChatMessage message) {
    Platform.runLater(() -> {
      VBox chatMessageContainer = (VBox) ((ScrollPane) scene.lookup(
          "#chat-message-container"
        )).getContent()
        .lookup("#chat-message-container-box");

      // Load the chat message template from the FXML file
      FXMLLoader loader = new FXMLLoader(
        getClass().getResource("ChatMessage.fxml")
      );

      try {
        Node chatMessageTemplate = loader.load();

        // Look up the elements in the template
        Label senderLabel = (Label) chatMessageTemplate.lookup("#sender");
        Label messageLabel = (Label) chatMessageTemplate.lookup("#message");
        ImageView senderTokenImage = (ImageView) chatMessageTemplate.lookup(
          "#sender-token"
        );

        // Set the data from the ChatMessage object to the elements in the
        // template
        senderLabel.setText(message.getSender());
        messageLabel.setText(message.getContent());

        Image tokenImage = localModel
          .getLocalGameBoard()
          .getPlayers()
          .stream()
          .filter(player -> player.getNickname().equals(message.getSender()))
          .map(player -> loadImage(player.getToken()))
          .findFirst()
          .get()
          .getImage();

        senderTokenImage.setImage(tokenImage);
        senderTokenImage.setFitWidth(25);
        senderTokenImage.setFitHeight(25);

        //TODO fix message being displayed twice.

        HBox chatMessage = new HBox(chatMessageTemplate);

        chatMessage.setAlignment(
          (localModel
                .getLocalGameBoard()
                .getPlayerNickname()
                .equals(message.getSender()))
            ? Pos.CENTER_RIGHT
            : Pos.CENTER_LEFT
        );

        chatMessageContainer.getChildren().add(chatMessage);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  @Override
  public void drawCommonObjectiveCards(CardPair<Card> cardPair) {
    VBox commonBoardContainer = (VBox) ((ScrollPane) scene.lookup(
        "#gameboard-container"
      )).getContent();

    HBox cardsContainer = (HBox) commonBoardContainer.lookup(
      "#common-objective-cards"
    );

    cardsContainer.setPadding(new Insets(5));

    cardsContainer.setAlignment(Pos.CENTER);

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

    Separator separator = new Separator();
    //set a margin for the separator
    HBox.setMargin(separator, new Insets(0, 10, 0, 10));

    separator.setOrientation(Orientation.VERTICAL);

    cardsContainer.getChildren().add(images.get(0));
    cardsContainer.getChildren().add(separator);
    cardsContainer.getChildren().add(images.get(1));
  }

  @Override
  public void drawPlayerObjective(Card card) {
    VBox vbox = (VBox) scene.lookup("#player-objective-card");
    vbox.getChildren().clear();

    ImageView image = loadCardImage(card, CardSideType.FRONT);
    image.setPreserveRatio(true);
    image.setFitWidth(150);

    vbox.getChildren().add(image);
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
