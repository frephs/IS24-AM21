package polimi.ingsw.am21.codex.view.GUI;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.Main;
import polimi.ingsw.am21.codex.client.ClientContext;
import polimi.ingsw.am21.codex.client.localModel.GameEntry;
import polimi.ingsw.am21.codex.client.localModel.LocalGameBoard;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.listeners.FullUserGameContext;
import polimi.ingsw.am21.codex.controller.listeners.GameInfo;
import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.GUI.utils.*;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class Gui extends Application implements View {

  private static Gui gui;

  private static ClientConnectionHandler client;
  private final LocalModelContainer localModel;
  private static final CountDownLatch isInitializedLatch = new CountDownLatch(
    1
  );

  public Gui() {
    gui = this;
    localModel = new LocalModelContainer();
  }

  public static Gui getInstance() {
    return gui;
  }

  @Override
  public void setClient(ClientConnectionHandler client) {
    Gui.client = client;
    Platform.runLater(
      () ->
        primaryStage.setTitle(
          primaryStage.getTitle() + " [" + client.getConnectionType() + "]"
        )
    );
  }

  /** The side of the hand the player is currently looking at */
  private CardSideType visibleHandSide = CardSideType.FRONT;
  /**
   * The index of the hand that is currently selected, null otherwise
   */
  private Integer selectedHandIndex = null;
  /**
   * The nickname of the user whose playerboard is currently displayed
   */
  private String visibleUserNickname;

  private static Stage primaryStage;
  private static NotificationLoader notificationLoader;
  private static ExceptionLoader exceptionLoader;
  private static RulebookHandler rulebookHandler;
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
      rulebookHandler = new RulebookHandler(new Stage());
      isInitializedLatch.countDown();

      primaryStage.setTitle("Codex Naturalis");
      primaryStage.setScene(scene);

      primaryStage.getIcons().add(loadImage("icon.png").getImage());

      primaryStage.setOnCloseRequest(event -> {
        if (Main.Options.isDebug()) {
          try {
            exceptionLoader.loadException(new RuntimeException("Closing"));
          } catch (IOException e) {
            // TODO: Handle exception
            System.err.println(e);
          }
        }

        System.exit(0);
      });

      primaryStage.show();
    } catch (IOException e) {
      // TODO: Handle exception
      System.err.println(e);
    }
  }

  public CountDownLatch getIsInitializedLatch() {
    return isInitializedLatch;
  }

  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Helper function: sets the #content container to the fxml template provided
   * @param fxmlPath the path of the template to load in the #content container
   * @param containerId the container to lead the scene in (e.g. #content, #side-content)
   * */
  private void loadSceneFXML(String fxmlPath, String containerId) {
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

  /**
   * Loads an image view element from the provided path
   * @param path the path of the image to load
   */
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
   * Loads an image view element from the provided GuiElement
   * @param element the GuiElement to load the image for
   * @return an Image view element containing the GuiElement provided
   */
  private ImageView loadImage(GuiElement element) {
    if (element != null) return loadImage(element.getImagePath());
    else return new ImageView();
  }

  /**
   * Loads an image view that represents the provided card
   * @param card the card to load the image for
   * @param side the side of the card to load
   * @return an Image view element containing the card image
   */
  private ImageView loadCardImage(Card card, CardSideType side) {
    return switch (side) {
      case FRONT -> loadImage(card.getImagePath(CardSideType.FRONT));
      case BACK -> loadImage(card.getImagePath(CardSideType.BACK));
    };
  }

  /**
   * Helper method that wraps the given imageview with a border and padding.
   * @param imageView the image view to wrap
   */
  private static HBox wrapAndBorder(ImageView imageView) {
    HBox image = new HBox(imageView);
    HBox.setMargin(image, new Insets(0, 10, 10, 10));
    image.setPadding(new Insets(10, 10, 10, 10));
    image.getStyleClass().add("bordered");
    image.setAlignment(Pos.CENTER);
    return image;
  }

  /**
   * Helper method that draws the game window, after the splash screen has been
   * shown and the client is connected
   */
  private void drawGameWindow() {
    Platform.runLater(() -> {
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

        scene.setOnKeyPressed(event -> {
          if (event.getCode().toString().equals("ALT")) {
            Platform.runLater(() -> {});
          }
        });

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  /**
   * Posts a push notification to the user
   * @param notificationType the type of notification to display
   * (CONFIRM, WARNING, ERROR, UPDATE, RESPONSE)
   * @param message the message to display
   * */
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

  /**
   * Posts a push notification to the user
   * @param notification the notification to display
   * */
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
  ) {
    // add colorable.toString in the right index of the messages array
    List<String> messagesWithColorable = new ArrayList<>(
      Arrays.stream(messages).toList()
    );
    try {
      messagesWithColorable.add(colorableIndex, colorable.toString());
    } catch (IndexOutOfBoundsException e) {
      messagesWithColorable.add(colorable.toString());
    }
    postNotification(notificationType, String.join(" ", messagesWithColorable));

    Platform.runLater(() -> {
      try {
        notificationLoader.addNotification(
          notificationType,
          String.join(" ", messagesWithColorable)
        );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  /**
   * Displays an exception to the user
   * @param exception the exception to display
   * */
  @Override
  public void displayException(Exception exception) {
    if (Main.Options.isDebug()) {
      try {
        exceptionLoader.loadException(exception);
        // TODO: Handle exception
        exception.printStackTrace();
      } catch (IOException e) {
        // TODO: Handle exception
        System.err.println(exception);
      }
    }
  }

  /**
   * Helper method to load a game entry Gui Element from the provided game
   * */
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

  /**
   * Draw the available games in the lobby menu
   */
  @Override
  public void drawAvailableGames() {
    Platform.runLater(() -> {
      ((Pane) scene.lookup("#side-content")).getChildren().clear();
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
        .setOnMouseClicked((MouseEvent event) -> {
          String gameName =
            ((TextField) scene.lookup("#game-id-input")).getText();

          if (gameName.contains(" ")) {
            postNotification(
              NotificationType.WARNING,
              "No spaces allowed in game name"
            );
          } else if (gameName.isEmpty()) {
            postNotification(
              NotificationType.WARNING,
              "Game name cannot be empty"
            );
          } else {
            client.createGame(
              gameName,
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
            );
          }
        });

      List<GameEntry> games = localModel
        .getLocalMenu()
        .getGames()
        .values()
        .stream()
        .toList();
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

  /**
   * Draw a list of available token colors for the player to choose from
   * */
  @Override
  public void drawAvailableTokenColors() {
    Platform.runLater(() -> {
      loadSceneFXML("LobbyToken.fxml", "#content");

      ((Text) scene.lookup("#window-title")).setText(
          "Lobby of the game " +
          localModel.getLocalLobby().orElseThrow().getGameId()
        );

      Node tokenContainer = scene.lookup("#token-container");
      ((HBox) tokenContainer).getChildren().clear();

      ((HBox) tokenContainer).getChildren()
        .addAll(
          localModel
            .getLocalLobby()
            .orElseThrow()
            .getAvailableTokens()
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

  /**
   * Draw the current status of the lobby and its connected players alongside the lobby window
   */
  @Override
  public void drawLobby() {
    Platform.runLater(() -> {
      loadSceneFXML("LobbyPlayers.fxml", "#side-content");

      scene
        .lookup("#back-to-menu-button-lobby")
        .setOnMouseClicked((MouseEvent event) -> client.leaveGameLobby());

      GridPane playerGrid =
        ((GridPane) scene.lookup("#lobby-player-container"));

      List<LocalPlayer> players = localModel
        .getLocalLobby()
        .orElseThrow()
        .getPlayers()
        .values()
        .stream()
        .toList();
      for (int i = 0; i < players.size(); i++) {
        LocalPlayer player = players.get(i);

        ImageView token = loadImage(player.getToken());
        token.setPreserveRatio(true);
        token.setFitHeight(25);

        HBox tokenContainer = new HBox(token);
        tokenContainer.alignmentProperty().set(Pos.CENTER_RIGHT);
        playerGrid.add(token, 0, i);

        Label nickname = new Label(
          (player.getNickname()) != null ? player.getNickname() : "<pending>"
        );
        nickname.alignmentProperty().setValue(Pos.CENTER);
        playerGrid.add(nickname, 1, i);
      }
    });
  }

  /**
   * Draw the leaderboard of the game, display the players and their points
   * */
  @Override
  public void drawLeaderBoard() {
    Platform.runLater(() -> {
      GridPane container = (GridPane) scene.lookup("#leaderboard-grid");
      container.getChildren().clear();

      List<LocalPlayer> players = localModel
        .getLocalGameBoard()
        .orElseThrow()
        .getPlayers();

      for (int i = 0; i < players.size(); i++) {
        LocalPlayer player = players.get(i);

        ImageView token = loadImage(player.getToken());
        token.setPreserveRatio(true);
        token.setFitHeight(25);

        Label nickname = new Label(player.getNickname());
        nickname.getStyleClass().add("leaderboard-entry");

        Label points = new Label(String.valueOf(player.getPoints()));
        points.getStyleClass().add("leaderboard-entry");

        HBox nicknameAndToken = new HBox(token, nickname);
        if (
          Objects.equals(
            player.getNickname(),
            localModel
              .getLocalGameBoard()
              .orElseThrow()
              .getCurrentPlayer()
              .getNickname()
          )
        ) {
          nicknameAndToken.getStyleClass().add("highlighted");
        }

        if (
          player
            .getConnectionStatus()
            .equals(
              GameController.UserGameContext.ConnectionStatus.DISCONNECTED
            )
        ) {
          nicknameAndToken.getStyleClass().add("grayed-out");
        }

        nicknameAndToken.setAlignment(Pos.CENTER_LEFT);
        nicknameAndToken.setPadding(new Insets(0, 0, 0, 20));

        container.addRow(i, nicknameAndToken, points);
      }
    });
  }

  // TODO maybe implement this in the cli
  /**
   * Draw a map of the given player available resources and objects to the playerBoard
   * @param player the player whose resources are to be drawn
   * */
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

  /**
   * Draw the scene for the player to choose a nickname
   * */
  @Override
  public void drawNicknameChoice() {
    Platform.runLater(() -> {
      loadSceneFXML("LobbyNickname.fxml", "#content");
      scene
        .lookup("#nickname-submit-button")
        .setOnMouseClicked((MouseEvent event) -> {
          String nickname =
            ((TextField) (scene.lookup("#nickname-input"))).getText();
          if (nickname.contains(" ")) {
            postNotification(
              NotificationType.WARNING,
              "No spaces allowed in nickname"
            );
          } else if (nickname.isEmpty()) {
            postNotification(
              NotificationType.WARNING,
              "Nickname cannot be empty"
            );
          } else {
            client.lobbySetNickname(nickname);
          }
        });
    });
  }

  @Override
  public LocalModelContainer getLocalModel() {
    return localModel;
  }

  @Override
  public void drawPlayerBoards() {
    // TODO handle other players (switch button etc)
    loadSceneFXML("PlayerBoard.fxml", "#content");

    List<LocalPlayer> players = localModel
      .getLocalGameBoard()
      .orElseThrow()
      .getPlayers();

    players
      .stream()
      .filter(
        player ->
          player
            .getNickname()
            .equals(
              localModel.getLocalGameBoard().orElseThrow().getPlayerNickname()
            )
      )
      .findFirst()
      .ifPresent(player -> {
        drawHand();
        drawPlayerObjective();
      });

    // noinspection unchecked
    ChoiceBox<String> playerBoardChoiceBox = (ChoiceBox<String>) scene.lookup(
      "#player-board-choice"
    );

    players.forEach(
      player -> playerBoardChoiceBox.getItems().add(player.getNickname())
    );

    playerBoardChoiceBox.setValue(
      localModel.getLocalGameBoard().orElseThrow().getPlayer().getNickname()
    );

    playerBoardChoiceBox.setOnAction(event -> {
      visibleUserNickname = playerBoardChoiceBox.getValue();
      drawPlayerBoard(visibleUserNickname);
    });

    // Draw the default player board
    drawPlayerBoard();

    players
      .stream()
      .filter(
        player ->
          player
            .getNickname()
            .equals(
              localModel.getLocalGameBoard().orElseThrow().getPlayerNickname()
            )
      )
      .findFirst()
      .ifPresent(player -> drawHand());
  }

  @Override
  public void drawPlayerBoard(
    String nickname,
    int verticalOffset,
    int horizontalOffset
  ) {
    drawPlayerBoard(nickname);
  }

  /**
   * Draw the placed cards of the given player
   * @param nickname The nickname of the player whose playerboard is to be drawn
   */
  @Override
  public void drawPlayerBoard(String nickname) {
    Platform.runLater(() -> {
      updateGameBoardDrawAbility();

      ScrollPane scrollPane = (ScrollPane) scene.lookup(
        "#playerboard-scrollpane"
      );
      GridPane gridPane = new GridPane();
      gridPane.setId("playerboard-grid");

      LocalPlayer player = localModel
        .getLocalGameBoard()
        .orElseThrow()
        .getPlayers()
        .stream()
        .filter(p -> p.getNickname().equals(nickname))
        .findFirst()
        .orElseThrow();

      for (int row = 0; row < GUIGridPosition.gridSize; row++) {
        RowConstraints rowConstraint = new RowConstraints();
        rowConstraint.setMinHeight(GridCell.CELL_HEIGHT);
        rowConstraint.setMaxHeight(GridCell.CELL_HEIGHT);
        rowConstraint.setPrefHeight(GridCell.CELL_HEIGHT);
        rowConstraint.setVgrow(Priority.NEVER);
        gridPane.getRowConstraints().add(rowConstraint);
      }
      for (int col = 0; col < GUIGridPosition.gridSize; col++) {
        ColumnConstraints colConstraint = new ColumnConstraints();
        colConstraint.setMinWidth(GridCell.CELL_WIDTH);
        colConstraint.setMaxWidth(GridCell.CELL_WIDTH);
        colConstraint.setPrefWidth(GridCell.CELL_WIDTH);
        colConstraint.setHgrow(Priority.NEVER);
        gridPane.getColumnConstraints().add(colConstraint);
      }

      player
        .getPlayedCardsByOrder()
        .forEach(info -> {
          Position position = info.getKey();
          Pair<PlayableCard, CardSideType> cardInfo = info.getValue();

          GridCell cell = new GridCell(
            // These cells should never be clickable since we're placing the card right away, but just in case...
            getCellClickHandler(position),
            getCellPlacementActive()
          );

          cell.placeCard(loadCardImage(cardInfo.getKey(), cardInfo.getValue()));

          GUIGridPosition viewPos = new GUIGridPosition(position);
          gridPane.add(cell, viewPos.getCol(), viewPos.getRow());
        });

      drawAvailablePositions(
        player.getAvailableSpots().orElse(new HashSet<>()),
        gridPane
      );
      drawForbiddenPositions(
        player.getForbiddenSpots().orElse(new HashSet<>()),
        gridPane
      );
      drawResourcesAndObjects(player);

      scrollPane.setContent(gridPane);
      //TODO draw resources and objects
    });
  }

  /**
   * @return a runnable that handles the click event on a cell
   * */
  private Runnable getCellClickHandler(Position position) {
    return () -> {
      if (selectedHandIndex != null && visibleHandSide != null) {
        client.placeCard(selectedHandIndex, visibleHandSide, position);
        selectedHandIndex = null;
      }
    };
  }

  /**
   * @return a supplier that determines whether the playerboard should be clickable or not
   */
  private Supplier<Boolean> getCellPlacementActive() {
    return () -> (this.selectedHandIndex != null && canPlayerPlaceCards());
  }

  /**
   * @return a boolean that determines whether the player can place cards
   * */
  private boolean canPlayerPlaceCards() {
    return (
      !localModel.currentPlayerHasPlacedCard() &&
      localModel.getLocalGameBoard().isPresent() &&
      localModel
        .getLocalGameBoard()
        .get()
        .getCurrentPlayer()
        .getNickname()
        .equals(
          localModel.getLocalGameBoard().get().getPlayer().getNickname()
        ) &&
      visibleUserNickname != null &&
      visibleUserNickname.equals(
        localModel.getLocalGameBoard().get().getPlayer().getNickname()
      )
    );
  }

  /**
   * @return a boolean that determines whether the player can draw cards
   */
  private boolean canPlayerDrawCards() {
    return (
      localModel.currentPlayerHasPlacedCard() &&
      localModel
        .getLocalGameBoard()
        .map(
          gameBoard ->
            gameBoard
              .getCurrentPlayer()
              .getNickname()
              .equals(gameBoard.getPlayer().getNickname())
        )
        .orElse(false)
    );
  }

  /**
   * Toggle the hand side of the player: display the front or back of cards
   * */
  private void toggleHandSide() {
    if (visibleHandSide == CardSideType.FRONT) {
      visibleHandSide = CardSideType.BACK;
    } else {
      visibleHandSide = CardSideType.FRONT;
    }
    selectedHandIndex = null;
    drawHand();
  }

  /**
   * Helper method to draw the given available position in which cards can be placed in
   * @param positions the set of positions to draw
   * @param gridPane the gridpane in which the cards are placed
   * */
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

      GUIGridPosition viewPos = new GUIGridPosition(position);
      gridPane.add(cell, viewPos.getCol(), viewPos.getRow());
    });
  }

  /**
   * Helper method to draw the given forbidden position in which cards cannot be placed in
   * @param positions the set of forbidden positions to draw
   * @param gridPane the gridpane in which the cards are placed
   * */
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

      GUIGridPosition viewPos = new GUIGridPosition(position);
      gridPane.add(cell, viewPos.getCol(), viewPos.getRow());
    });
  }

  /**
   * Draw the playerboard and gameboard scenes after every player has finished in the lobby
   * */
  @Override
  public void drawGame() {
    Platform.runLater(() -> {
      loadSceneFXML("GameBoard.fxml", "#side-content");
      primaryStage.setTitle(
        primaryStage.getTitle() +
        " [" +
        localModel
          .getLocalGameBoard()
          .map(
            gameBoard ->
              Optional.ofNullable(gameBoard.getPlayer().getNickname()).orElse(
                gameBoard.getPlayer().getConnectionID().toString()
              )
          )
          .orElseThrow() +
        "]"
      );
      drawChat();
      drawGameBoard();
      drawPlayerBoards();
      drawLeaderBoard();
      ((Text) scene.lookup("#window-title")).setText(
          "Game" + localModel.getGameId().map(id -> " " + id).orElse("")
        );

      scene
        .lookup("#instruction-manual-button")
        .setOnMouseClicked((MouseEvent event) -> {
          try {
            rulebookHandler.loadRulebook();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    });
  }

  /**
   * Draw the gameboard contents.
   * */
  @Override
  public void drawGameBoard() {
    drawPairs();
    drawCardDecks();
    drawCommonObjectiveCards();
  }

  /**
   * Updates whether the style of the gameboard container, so that the player can
   * see when they can draw cards
   */
  private void updateGameBoardDrawAbility() {
    ScrollPane container = ((ScrollPane) scene.lookup("#gameboard-container"));
    if (canPlayerDrawCards()) {
      container.getContent().getStyleClass().add("allow-draw");
    } else {
      container.getContent().getStyleClass().clear();
    }
  }

  /**
   * Display the game is over and the final leaderboard
   * */
  @Override
  public void drawGameOver() {
    Platform.runLater(() -> {
      ((Text) scene.lookup("#window-title")).setText("Game Over");
      //draw the final leaderboard int the #side-content container and don't use drawLeaderBoard
      drawLeaderBoard();
      winningPlayer(
        localModel
          .getLocalGameBoard()
          .orElseThrow()
          .getPlayers()
          .stream()
          .min((p1, p2) -> p2.getPoints() - p1.getPoints())
          .map(LocalPlayer::getNickname)
          .orElseThrow()
      );

      VBox commonBoardContainer = (VBox) scene.lookup(
        "#common-board-container"
      );

      VBox commonBoardContainerParent = (VBox) commonBoardContainer.getParent();
      commonBoardContainerParent.getChildren().remove(commonBoardContainer);
    });
  }

  @Override
  public void drawCard(Card card) {
    // TODO remove this from view and move it to cli
  }

  /**
   * Draws the hand of the player
   * */
  @Override
  public void drawHand() {
    Platform.runLater(() -> {
      // Save the hand in case we need to flip it
      List<Card> hand = localModel
        .getLocalGameBoard()
        .orElseThrow()
        .getPlayer()
        .getHand();

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

  /**
   * Draws the pairs of resource and gold cards which the player can draw from
   * */
  @Override
  public void drawPairs() {
    Platform.runLater(() -> {
      updateGameBoardDrawAbility();

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

      CardPair<Card> resourceCards = localModel
        .getLocalGameBoard()
        .orElseThrow()
        .getResourceCards();
      CardPair<Card> goldCards = localModel
        .getLocalGameBoard()
        .orElseThrow()
        .getGoldCards();

      // TODO how do we want to display two sides?
      List<ImageView> images = List.of(
        loadCardImage(resourceCards.getFirst(), CardSideType.FRONT),
        loadCardImage(resourceCards.getSecond(), CardSideType.FRONT),
        loadCardImage(goldCards.getFirst(), CardSideType.FRONT),
        loadCardImage(goldCards.getSecond(), CardSideType.FRONT)
      );

      images
        .get(0)
        .setOnMouseClicked((MouseEvent event) -> {
          if (canPlayerDrawCards()) client.nextTurn(
            DrawingCardSource.CardPairFirstCard,
            DrawingDeckType.RESOURCE
          );
        });
      images
        .get(1)
        .setOnMouseClicked((MouseEvent event) -> {
          if (canPlayerDrawCards()) client.nextTurn(
            DrawingCardSource.CardPairSecondCard,
            DrawingDeckType.RESOURCE
          );
        });
      images
        .get(2)
        .setOnMouseClicked((MouseEvent event) -> {
          if (canPlayerDrawCards()) client.nextTurn(
            DrawingCardSource.CardPairFirstCard,
            DrawingDeckType.GOLD
          );
        });
      images
        .get(3)
        .setOnMouseClicked((MouseEvent event) -> {
          if (canPlayerDrawCards()) client.nextTurn(
            DrawingCardSource.CardPairSecondCard,
            DrawingDeckType.GOLD
          );
        });

      images.forEach(image -> {
        image.setPreserveRatio(true);
        image.setFitWidth(150);
        image.getStyleClass().add("card");
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

  /**
   * Draw the two objective cards the player can choose from in the lobby
   * */
  @Override
  public void drawObjectiveCardChoice() {
    Platform.runLater(() -> {
      loadSceneFXML("LobbyChooseObjective.fxml", "#content");

      ((Text) scene.lookup("#window-title")).setText("Lobby");

      CardPair<Card> cardPair = localModel
        .getLocalLobby()
        .orElseThrow()
        .getAvailableObjectives();

      ImageView first = loadImage(cardPair.getFirst());
      ImageView second = loadImage(cardPair.getSecond());

      first.setOnMouseClicked(
        (MouseEvent event) -> client.lobbyChooseObjectiveCard(true)
      );

      second.setOnMouseClicked(
        (MouseEvent event) -> client.lobbyChooseObjectiveCard(false)
      );

      List.of(first, second).forEach(image -> {
        image.setPreserveRatio(true);
        image.setFitWidth(150);
        image.getStyleClass().add("card");
      });

      Node objectiveContainer = scene.lookup("#objective-container");
      ((HBox) objectiveContainer).getChildren().add(wrapAndBorder(first));
      ((HBox) objectiveContainer).getChildren().add(wrapAndBorder(second));
    });
  }

  /**
   * Draw the sides of the starter card the player can choose from in the lobby
   * */
  @Override
  public void drawStarterCardSides() {
    Platform.runLater(() -> {
      loadSceneFXML("LobbyChooseStarterCardSide.fxml", "#content");

      ((Text) scene.lookup("#window-title")).setText("Lobby");

      Card card = localModel.getLocalLobby().orElseThrow().getStarterCard();

      ImageView front = loadCardImage(card, CardSideType.FRONT);
      ImageView back = loadCardImage(card, CardSideType.BACK);

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

  /**
   * Load the chat scene for chat messages to be drawn in.
   * */
  public void drawChat() {
    loadSceneFXML("Chat.fxml", "#side-content-bottom");
    // add recipients to chat-recipient combo box

    // noinspection unchecked
    ChoiceBox<String> recipientChoiceBox = (ChoiceBox<String>) scene.lookup(
      "#chat-recipient"
    );

    LocalGameBoard localGameBoard = localModel
      .getLocalGameBoard()
      .orElseThrow();

    localGameBoard
      .getPlayers()
      .stream()
      .filter(
        player ->
          !player.getNickname().equals(localGameBoard.getPlayerNickname())
      )
      .forEach(
        player -> recipientChoiceBox.getItems().add(player.getNickname())
      );

    recipientChoiceBox.getItems().addFirst("Broadcast");

    recipientChoiceBox.setValue("Broadcast");

    scene
      .lookup("#chat-send-button")
      .setOnMouseClicked((MouseEvent event) -> {
        String recipient = recipientChoiceBox.getValue();

        TextField textInput = ((TextField) scene.lookup("#chat-input"));
        String message = textInput.getText();
        ChatMessage chatMessage;

        if (!Objects.equals(recipient, "Broadcast")) {
          chatMessage = new ChatMessage(
            localGameBoard.getPlayerNickname(),
            recipient,
            message
          );
        } else {
          chatMessage = new ChatMessage(
            localGameBoard.getPlayerNickname(),
            message
          );
        }
        client.sendChatMessage(chatMessage);
        textInput.clear();

        drawChatMessage(chatMessage);
      });
  }

  /**
   * Draw a chat message in the chat window
   * */
  @Override
  public void drawChatMessage(ChatMessage message) {
    Platform.runLater(() -> {
      VBox chatMessageContainer = (VBox) ((ScrollPane) scene.lookup(
          "#chat-message-container"
        )).getContent()
        .lookup("#chat-message-container-box");

      FXMLLoader loader = new FXMLLoader(
        getClass().getResource("ChatMessage.fxml")
      );

      try {
        Node chatMessageTemplate = loader.load();

        Label senderLabel = (Label) chatMessageTemplate.lookup("#sender");
        Label messageLabel = (Label) chatMessageTemplate.lookup("#message");
        ImageView senderTokenImage = (ImageView) chatMessageTemplate.lookup(
          "#sender-token"
        );

        senderLabel.setText(message.getSender());
        messageLabel.setText(message.getContent());

        // whispering
        if (message.getRecipient().isPresent()) {
          messageLabel.setStyle("-fx-text-fill: #6b17b1");
        }

        Image tokenImage = localModel
          .getLocalGameBoard()
          .orElseThrow()
          .getPlayers()
          .stream()
          .filter(player -> player.getNickname().equals(message.getSender()))
          .map(player -> loadImage(player.getToken()))
          .findFirst()
          .orElseThrow()
          .getImage();

        senderTokenImage.setImage(tokenImage);
        senderTokenImage.setFitWidth(25);
        senderTokenImage.setFitHeight(25);

        //TODO fix message being displayed twice.

        HBox chatMessage = new HBox(chatMessageTemplate);

        chatMessage.setAlignment(
          (localModel
                .getLocalGameBoard()
                .orElseThrow()
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

  /**
   * Draw the common objective cards in the gameboard
   * */
  @Override
  public void drawCommonObjectiveCards() {
    Platform.runLater(() -> {
      VBox commonBoardContainer = (VBox) ((ScrollPane) scene.lookup(
          "#gameboard-container"
        )).getContent();

      HBox cardsContainer = (HBox) commonBoardContainer.lookup(
        "#common-objective-cards"
      );

      cardsContainer.setPadding(new Insets(5));

      cardsContainer.setAlignment(Pos.CENTER);

      cardsContainer.getChildren().clear();

      CardPair<Card> cardPair = localModel
        .getLocalGameBoard()
        .orElseThrow()
        .getObjectiveCards();

      List<ImageView> images = List.of(
        loadCardImage(cardPair.getFirst(), CardSideType.FRONT),
        loadCardImage(cardPair.getSecond(), CardSideType.FRONT)
      );

      images.forEach(image -> {
        image.setPreserveRatio(true);
        image.setFitWidth(150);
      });

      Separator separator = new Separator();
      //set a margin for the separator
      HBox.setMargin(separator, new Insets(0, 10, 0, 10));

      separator.setOrientation(Orientation.VERTICAL);

      cardsContainer.getChildren().add(images.get(0));
      cardsContainer.getChildren().add(separator);
      cardsContainer.getChildren().add(images.get(1));
    });
  }

  /**
   * Draw the player objective card in its player board alongside its hand
   * */
  @Override
  public void drawPlayerObjective() {
    VBox vbox = (VBox) scene.lookup("#player-objective-card");
    vbox.getChildren().clear();

    ImageView image = loadCardImage(
      localModel
        .getLocalGameBoard()
        .orElseThrow()
        .getPlayer()
        .getObjectiveCard(),
      CardSideType.FRONT
    );
    image.setPreserveRatio(true);
    image.setFitWidth(150);

    vbox.getChildren().add(image);
  }

  /**
   * Draw the card decks in the gameboard
   * */
  @Override
  public void drawCardDecks() {
    Platform.runLater(() -> {
      HBox resourceCardsDeck = (HBox) scene.lookup("#resource-cards-deck");
      HBox goldCardsDeck = (HBox) scene.lookup("#gold-cards-deck");

      PlayableCard firstResourceCard = localModel
        .getLocalGameBoard()
        .orElseThrow()
        .getResourceDeckTopCard();
      PlayableCard firstGoldCard = localModel
        .getLocalGameBoard()
        .orElseThrow()
        .getGoldDeckTopCard();

      resourceCardsDeck.getChildren().clear();
      if (firstResourceCard != null) {
        ImageView resource = loadCardImage(
          firstResourceCard,
          CardSideType.BACK
        );
        resource.setPreserveRatio(true);
        resource.setFitWidth(150);
        resource.getStyleClass().add("card");

        resource.setOnMouseClicked((MouseEvent event) -> {
          if (canPlayerDrawCards()) client.nextTurn(
            DrawingCardSource.Deck,
            DrawingDeckType.RESOURCE
          );
        });

        resourceCardsDeck.getChildren().add((resource));
      }

      goldCardsDeck.getChildren().clear();
      if (firstGoldCard != null) {
        ImageView gold = loadCardImage(firstGoldCard, CardSideType.BACK);
        gold.setPreserveRatio(true);
        gold.setFitWidth(150);
        gold.getStyleClass().add("card");

        gold.setOnMouseClicked((MouseEvent event) -> {
          if (canPlayerDrawCards()) client.nextTurn(
            DrawingCardSource.Deck,
            DrawingDeckType.GOLD
          );
        });

        goldCardsDeck.getChildren().add((gold));
      }
    });
  }

  //-------------------------------
  // GAME EVENT HANDLERS
  //-------------------------------
  @Override
  public void listGames() {
    drawGameWindow();
    drawAvailableGames();
  }

  @Override
  public void gameCreated(String gameId, int currentPlayers, int maxPlayers) {
    View.super.gameCreated(gameId, currentPlayers, maxPlayers);
    drawAvailableGames();
  }

  @Override
  public void gameDeleted(String gameId) {
    View.super.gameDeleted(gameId);
  }

  @Override
  public void playerJoinedLobby(String gameId, UUID connectionID) {
    View.super.playerJoinedLobby(gameId, connectionID);
    if (gameId.equals(localModel.getGameId().orElse(null))) {
      if (connectionID.equals(localModel.getConnectionID())) {
        drawAvailableTokenColors();
      } else {
        drawLobby();
      }
    } else if (
      ClientContext.MENU.equals(
        localModel.getClientContextContainer().get().orElse(null)
      )
    ) {
      drawAvailableGames();
    }
  }

  @Override
  public void playerLeftLobby(String gameId, UUID connectionID) {
    View.super.playerLeftLobby(gameId, connectionID);

    ClientContext context = localModel
      .getClientContextContainer()
      .get()
      .orElse(null);

    if (ClientContext.LOBBY.equals(context)) drawLobby();
    else if (ClientContext.MENU.equals(context)) drawAvailableGames();
  }

  @Override
  public void playerSetToken(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor token
  ) {
    if (gameId.equals(localModel.getGameId().orElse(null))) {
      if (connectionID.equals(localModel.getConnectionID())) {
        drawNicknameChoice();
        client.getObjectiveCards();
      } else if (
        localModel
          .getLocalLobby()
          .map(
            lobby ->
              lobby.getPlayers().get(localModel.getConnectionID()).getToken() ==
              null
          )
          .orElse(false)
      ) {
        drawAvailableTokenColors();
      }
      drawLobby();
    }
  }

  @Override
  public void playerSetNickname(
    String gameId,
    UUID connectionID,
    String nickname
  ) {
    if (gameId.equals(localModel.getGameId().orElse(null))) {
      if (connectionID.equals(localModel.getConnectionID())) {
        drawObjectiveCardChoice();
        client.getStarterCard();
      }

      drawLobby();
    }
  }

  @Override
  public void playerChoseObjectiveCard(
    String gameId,
    UUID connectionID,
    String nickname
  ) {
    View.super.playerChoseObjectiveCard(gameId, connectionID, nickname);
    if (gameId.equals(localModel.getGameId().orElse(null))) {
      if (connectionID.equals(localModel.getConnectionID())) {
        drawStarterCardSides();
      } else {
        drawLobby();
      }
    }
  }

  @Override
  public void gameStarted(String gameId, GameInfo gameInfo) {
    View.super.gameStarted(gameId, gameInfo);
    visibleUserNickname = gameInfo
      .getUsers()
      .stream()
      .filter(
        user -> user.getConnectionID().equals(localModel.getConnectionID())
      )
      .findFirst()
      .map(GameInfo.GameInfoUser::getNickname)
      .orElse(null);
    drawGame();
  }

  @Override
  public void gameOver() {
    View.super.gameOver();
    drawGameOver();
  }

  @Override
  public void playerScoresUpdate(Map<String, Integer> newScores) {
    View.super.playerScoresUpdate(newScores);
    drawLeaderBoard();
  }

  @Override
  public void remainingRounds(String gameID, int remainingRounds) {
    View.super.remainingRounds(gameID, remainingRounds);
  }

  @Override
  public void winningPlayer(String nickname) {
    View.super.winningPlayer(nickname);
    Platform.runLater(() -> {
      loadSceneFXML("Winner.fxml", "#content");
      ((Text) scene.lookup("#winner-container")).setText(nickname);
      scene
        .lookup("#back-to-menu-button")
        .setOnMouseClicked((MouseEvent event) -> client.getGames());
    });
  }

  @Override
  public void playerConnectionChanged(
    UUID connectionID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) {
    View.super.playerConnectionChanged(connectionID, nickname, status);
    //TODO make it visible in the leaderBoard
    if (
      localModel
        .getClientContextContainer()
        .get()
        .map(context -> context.equals(ClientContext.GAME))
        .orElse(false)
    ) {
      drawLeaderBoard();
    }
  }

  @Override
  public void lobbyInfo(LobbyUsersInfo usersInfo) {
    if (
      ClientContext.LOBBY.equals(
        localModel.getClientContextContainer().get().orElse(null)
      )
    ) drawLobby();

    if (
      localModel
        .getLocalLobby()
        .map(
          lobby ->
            lobby.getPlayers().get(localModel.getConnectionID()).getToken() ==
            null
        )
        .orElse(false)
    ) drawAvailableTokenColors();
  }

  @Override
  public void chatMessage(String gameID, ChatMessage message) {
    View.super.chatMessage(gameID, message);
  }

  @Override
  public void gameHalted(String gameID) {
    StackPane pane = (StackPane) scene.lookup("#content-container");
    // we load LobbyWaitRoom first
    String waitRoomId = "wait-room";

    try {
      pane
        .getChildren()
        .forEach(node -> {
          if (
            node.getId() == null || !node.getId().equals(waitRoomId)
          ) node.setVisible(false);
        });

      Node lobbyWaitRoom = FXMLLoader.load(
        Objects.requireNonNull(Gui.class.getResource("LobbyWaitRoom.fxml"))
      );
      lobbyWaitRoom.setId(waitRoomId);
      if (
        !pane
          .getChildren()
          .stream()
          .anyMatch(
            node -> node.getId() != null && node.getId().equals(waitRoomId)
          )
      ) pane.getChildren().add(lobbyWaitRoom);
    } catch (Exception e) {
      System.err.println(e);
    }
  }

  @Override
  public void gameResumed(String gameID) {
    StackPane pane = (StackPane) scene.lookup("#content-container");
    // we load LobbyWaitRoom first
    String waitRoomId = "wait-room";

    try {
      pane
        .getChildren()
        .forEach(node -> {
          if (
            node.getId() == null || !node.getId().equals(waitRoomId)
          ) node.setVisible(true);
        });
    } catch (Exception e) {
      System.err.println(e);
    }
    pane
      .getChildren()
      .removeIf(
        node -> node.getId() != null && node.getId().equals(waitRoomId)
      );
  }

  @Override
  public void userContext(FullUserGameContext context) {}

  @Override
  public void cardPlaced(
    String gameId,
    String playerId,
    Integer playerHandCardNumber,
    Integer cardId,
    CardSideType side,
    Position position,
    int newPlayerScore,
    Map<ResourceType, Integer> updatedResources,
    Map<ObjectType, Integer> updatedObjects,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots
  ) {
    View.super.cardPlaced(
      gameId,
      playerId,
      playerHandCardNumber,
      cardId,
      side,
      position,
      newPlayerScore,
      updatedResources,
      updatedObjects,
      availableSpots,
      forbiddenSpots
    );

    drawHand();
    drawLeaderBoard();
    drawPlayerBoard();
  }

  @Override
  public void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots,
    Integer resourceDeckTopCardId,
    Integer goldDeckTopCardId
  ) {
    View.super.changeTurn(
      gameId,
      playerNickname,
      playerIndex,
      isLastRound,
      availableSpots,
      forbiddenSpots,
      resourceDeckTopCardId,
      goldDeckTopCardId
    );

    drawHand();
    drawLeaderBoard();
    drawPlayerBoard();
    drawGameBoard();
  }
}
