package polimi.ingsw.am21.codex.view;

import java.util.*;
import polimi.ingsw.am21.codex.client.ClientContext;
import polimi.ingsw.am21.codex.client.localModel.GameEntry;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.controller.listeners.GameInfo;
import polimi.ingsw.am21.codex.controller.listeners.LobbyUsersInfo;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.TUI.utils.CliUtils;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.ColorStyle;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;

public interface View extends GameEventListener {
  public LocalModelContainer getLocalModel();

  void postNotification(NotificationType notificationType, String message);

  void postNotification(Notification notification);

  void postNotification(
    NotificationType notificationType,
    String[] messages,
    Colorable colorable,
    int colorableIndex
  );

  void displayException(Exception e);

  // lobby
  void drawAvailableGames(List<GameEntry> games);

  void drawAvailableTokenColors(Set<TokenColor> tokenColors);

  void drawLobby(Map<UUID, LocalPlayer> players);

  // game

  void drawLeaderBoard(List<LocalPlayer> players);

  void drawPlayerBoards(List<LocalPlayer> players);

  void drawPlayerBoard(LocalPlayer player);

  /**
   * Displays that the client player has drawn a card from a deck
   * @param deck The deck the card has been drawn from
   * @param card The card that has been drawn
   */
  void drawCardDrawn(DrawingDeckType deck, Card card);

  /**
   * Displays that a card has been drawn from a deck
   * @param deck The deck that the card has been drawn from
   */
  void drawCardDrawn(DrawingDeckType deck);

  void drawCardPlacement(
    Card card,
    CardSideType side,
    Position position,
    Set<Position> availablePositions,
    Set<Position> forbiddenPositions
  );

  void drawGame(List<LocalPlayer> players);

  void drawGameOver(List<LocalPlayer> players);

  void drawCard(Card card);

  /**
   * Displays the hand of the client player
   */
  void drawHand(List<Card> hand);

  /**
   * Displays the pairs the players can draw from
   * @param resourceCards The resource cards pair
   * @param goldCards The gold cards pair
   */

  void drawPairs(CardPair<Card> resourceCards, CardPair<Card> goldCards);

  void drawObjectiveCardChoice(CardPair<Card> cardPair);
  void drawStarterCardSides(Card card);

  void drawWinner(String nickname);

  void drawChatMessage(ChatMessage message);
  void drawCommonObjectiveCards(CardPair<Card> cardPair);
  void drawPlayerObjective(Card card);

  /**
   * Displays the cards decks to draw from
   * @param firstResourceCard The first resource card (null if none)
   * @param firstGoldCard The first gold card (null if none)
   */
  void drawCardDecks(
    PlayableCard firstResourceCard,
    PlayableCard firstGoldCard
  );

  void drawNicknameChoice();

  // ---------------------------
  // GAME EVENT LISTENER METHODS
  // ---------------------------

  @Override
  default void gameCreated(String gameId, int currentPlayers, int maxPlayers) {
    postNotification(NotificationType.UPDATE, "Game " + gameId + " created");
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
    } else if (getLocalModel().getLocalGameBoard().getGameId().equals(gameId)) {
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
    } else if (getLocalModel().getLocalGameBoard().getGameId().equals(gameId)) {
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
        "A Player ",
        nickname != null ? nickname : socketID.toString().substring(0, 6),
        " has set their token to ",
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
    String message;
    if (getLocalModel().getSocketID().equals(socketID)) {
      message = "You set your nickname to " + nickname;
    } else {
      message = socketID.toString().substring(0, 6) +
      " set their nickname to " +
      nickname;
    }
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
      "It's " +
      playerNickname +
      "'s turn" +
      (isLastRound ? " (last round)" : "")
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
      playerNickname +
      "'s turn" +
      (isLastRound ? " (last round)" : "")
    );
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
    postNotification(NotificationType.UPDATE, remainingRounds + " rounds left");
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
  }
}
