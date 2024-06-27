package polimi.ingsw.am21.codex.view;

import java.util.*;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.controller.listeners.GameInfo;
import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;

public interface View extends GameEventListener {
  void setClient(ClientConnectionHandler client);

  LocalModelContainer getLocalModel();

  void postNotification(NotificationType notificationType, String message);

  void postNotification(Notification notification);

  void postNotification(
    NotificationType notificationType,
    String[] messages,
    Colorable colorable,
    int colorableIndex
  );

  void listGames();

  void displayException(Exception e);

  // lobby
  void drawAvailableGames();

  void drawLobby();

  // game

  void drawGameBoard();

  void drawLeaderBoard();

  void drawPlayerBoards();

  void drawPlayerBoard(String nickname);

  default void drawPlayerBoard() {
    drawPlayerBoard(
      getLocalModel()
        .getLocalGameBoard()
        .orElseThrow()
        .getPlayer()
        .getNickname()
    );
  }

  void drawGame();

  void drawGameOver();

  void drawCard(Card card);

  /**
   * Displays the hand of the client player
   */
  void drawHand();

  /**
   * Displays the pairs the players can draw from
   */
  void drawPairs();

  void drawAvailableTokenColors();
  void drawObjectiveCardChoice();
  void drawNicknameChoice();
  void drawStarterCardSides();

  void drawChatMessage(ChatMessage message);
  void drawCommonObjectiveCards();
  void drawPlayerObjective();

  /**
   * Displays the cards decks to draw from
   */
  void drawCardDecks();

  // <editor-fold desc="Game Event Listener Methods">

  @Override
  default void gameCreated(String gameId, int currentPlayers, int maxPlayers) {
    postNotification(NotificationType.UPDATE, "Game " + gameId + " created");
  }

  @Override
  default void refreshLobbies(
    Set<String> lobbyIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) {
    // We don't want to show any notification by default
    listGames();
  }

  @Override
  default void gameDeleted(String gameId) {
    postNotification(NotificationType.UPDATE, "Game " + gameId + " deleted");
  }

  @Override
  default void playerJoinedLobby(String gameId, UUID socketID) {
    String message;
    if (getLocalModel().getSocketID().equals(socketID)) {
      message = "You joined the game " + gameId;
    } else if (
      getLocalModel()
        .getLocalGameBoard()
        .map(gameBoard -> gameBoard.getGameId().equals(gameId))
        .orElse(false)
    ) {
      message = socketID.toString() + " joined your game";
    } else {
      message = socketID.toString() + " joined game " + gameId;
    }

    postNotification(NotificationType.UPDATE, message);
  }

  @Override
  default void playerLeftLobby(String gameId, UUID socketID) {
    String message;

    if (getLocalModel().getSocketID().equals(socketID)) {
      message = "You left the lobby of the game " + gameId;
    } else if (
      getLocalModel()
        .getLocalGameBoard()
        .map(gameBoard -> gameBoard.getGameId().equals(gameId))
        .orElse(false)
    ) {
      message = socketID.toString() + " left your game lobby";
    } else {
      message = socketID.toString() + " left the game lobby " + gameId;
    }

    postNotification(NotificationType.UPDATE, message);
  }

  @Override
  default void playerSetToken(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor token
  ) {
    postNotification(
      NotificationType.UPDATE,
      new String[] {
        getLocalModel().getSocketID().equals(socketID)
          ? "You"
          : ("The player " +
            (nickname != null
                ? nickname
                : socketID.toString().substring(0, 6))),
        " set their token to ",
      },
      token,
      3
    );
  }

  @Override
  default void playerSetNickname(
    String gameId,
    UUID socketID,
    String nickname
  ) {
    postNotification(
      NotificationType.UPDATE,
      getLocalModel().getSocketID().equals(socketID)
        ? "You set your nickname to " + nickname
        : socketID.toString().substring(0, 6) +
        " set their nickname to " +
        nickname
    );
  }

  @Override
  default void playerChoseObjectiveCard(
    String gameId,
    UUID socketID,
    String nickname
  ) {
    postNotification(
      NotificationType.UPDATE,
      "Player " +
      (nickname != null ? nickname : socketID.toString().substring(0, 6)) +
      " has chosen their secret objective card"
    );
  }

  @Override
  default void playerJoinedGame(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCardID,
    CardSideType starterSide
  ) {
    postNotification(
      NotificationType.UPDATE,
      "Player " + nickname + " has joined the game "
    );
  }

  @Override
  default void gameStarted(String gameId, GameInfo gameInfo) {
    postNotification(
      NotificationType.UPDATE,
      "Game " + gameId + " has started"
    );

    if (
      getLocalModel()
        .getLocalGameBoard()
        .orElseThrow()
        .getCurrentPlayer()
        .getSocketID()
        .equals(getLocalModel().getSocketID())
    ) {
      postNotification(NotificationType.UPDATE, "It's your turn. ");
    } else {
      postNotification(
        NotificationType.UPDATE,
        "It's " +
        getLocalModel()
          .getLocalGameBoard()
          .orElseThrow()
          .getCurrentPlayer()
          .getNickname() +
        "'s turn. "
      );
    }
  }

  @Override
  default void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    DrawingCardSource source,
    DrawingDeckType deck,
    Integer cardId,
    Integer newPairCardId,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots,
    Integer resourceDeckTopCardId,
    Integer goldDeckTopCardId
  ) {
    postNotification(
      NotificationType.UPDATE,
      playerNickname +
      " has drawn a card from the " +
      source.toString().toLowerCase() +
      " " +
      deck.toString().toLowerCase() +
      ". "
    );

    changeTurn(
      gameId,
      playerNickname,
      playerIndex,
      isLastRound,
      availableSpots,
      forbiddenSpots,
      resourceDeckTopCardId,
      goldDeckTopCardId
    );
  }

  @Override
  default void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots,
    Integer resourceDeckTopCardId,
    Integer goldDeckTopCardId
  ) {
    postNotification(
      NotificationType.UPDATE,
      "It's " +
      (getLocalModel()
            .getLocalGameBoard()
            .get()
            .getPlayerNickname()
            .equals(
              getLocalModel()
                .getLocalGameBoard()
                .orElseThrow()
                .getCurrentPlayer()
                .getNickname()
            )
          ? "your "
          : getLocalModel()
            .getLocalGameBoard()
            .orElseThrow()
            .getCurrentPlayer()
            .getNickname()) +
      "'turn" +
      (isLastRound ? " (last round)" : "")
    );

    drawHand();
    drawLeaderBoard();
    drawPlayerBoard();
    drawGameBoard();
  }

  @Override
  default void cardPlaced(
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
    postNotification(
      NotificationType.UPDATE,
      "Player " + playerId + " placed card " + cardId + " on the board"
    );

    drawHand();
    drawLeaderBoard();
    drawPlayerBoard();
  }

  @Override
  default void gameOver() {
    postNotification(NotificationType.UPDATE, "Game is over");
  }

  @Override
  default void playerScoresUpdate(Map<String, Integer> newScores) {
    postNotification(NotificationType.UPDATE, "Scores updated");
  }

  @Override
  default void remainingRounds(String gameID, int remainingRounds) {
    if (remainingRounds == 2 || remainingRounds == 1) postNotification(
      NotificationType.UPDATE,
      remainingRounds == 2
        ? "The next round will be the last one. "
        : "The last round has started."
    );
  }

  @Override
  default void winningPlayer(String nickname) {
    postNotification(NotificationType.UPDATE, nickname + " has won the game!");
  }

  @Override
  default void playerConnectionChanged(
    UUID socketID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) {
    postNotification(
      NotificationType.UPDATE,
      "Player " +
      nickname +
      " has " +
      status.toString().toLowerCase() +
      " the game"
    );
  }

  @Override
  void lobbyInfo(LobbyUsersInfo usersInfo);

  @Override
  default void chatMessage(String gameID, ChatMessage message) {
    postNotification(
      NotificationType.UPDATE,
      "New chat message from " + message.getSender()
    );

    drawChatMessage(message);
  }

  @Override
  void getObjectiveCards(Pair<Integer, Integer> objectiveCards);

  @Override
  void getStarterCard(Integer cardId);
  // </editor-fold>
}
