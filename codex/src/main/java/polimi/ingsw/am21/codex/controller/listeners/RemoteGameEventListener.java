package polimi.ingsw.am21.codex.controller.listeners;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.Chat.ChatMessage;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface RemoteGameEventListener extends Remote {
  /**
   * Interface method used to process the game creation event.
   * @param gameId The unique identifier of the game that has just been created.
   * @param currentPlayers The number of players currently in the game.
   * @param maxPlayers The maximum number of players that can join the game.
   * */
  void gameCreated(String gameId, int currentPlayers, int maxPlayers)
    throws RemoteException;

  /**
   * Interface method used to refresh the status of the lobbies after a game is created or a client joins a game
   * @param lobbyIds A set containing the unique identifiers of all the game lobbies with available spots.
   * @param currentPlayers A map containing the number of players currently in each lobby.
   *                       The key is the unique identifier of the lobby and the value is the number of players.
   *                       The number of players is the number of players currently in the lobby.
   * @param maxPlayers A map containing the maximum number of players that can join each lobby.
   *                   The key is the unique identifier of the lobby and the value is the maximum number of players.
   *                   The maximum number of players is the maximum number of players that can join the lobby.
   * */
  void refreshLobbies(
    Set<String> lobbyIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) throws RemoteException;

  /**
   * Interface method used to process the game deletion event.
   * @param gameId The identifier of the game that has just been deleted.
   * */
  void gameDeleted(String gameId) throws RemoteException;

  /**
   * Interface method used to process the event of a player joining a game lobby.
   * @param gameId The identifier of the game lobby that the player has joined.
   * @param connectionID The unique identifier of the player that has joined the lobby.
   * */
  void playerJoinedLobby(String gameId, UUID connectionID)
    throws RemoteException;

  /**
   * Interface method used to process the event of a player leaving a game lobby.
   * @param gameId The identifier of the game lobby that the player has left.
   * @param connectionID The unique identifier of the player that has left the lobby.
   * */
  void playerLeftLobby(String gameId, UUID connectionID) throws RemoteException;

  /**
   * Interface method used to process the event of a player choosing their token color.
   * @param gameId The identifier of the game lobby in which the player has set their token color.
   * @param connectionID The unique identifier of the player that has set their token color.
   * @param nickname The nickname of the player that has set their token color,
   *                 if they have chosen it already
   * @param token The token color that the player has chosen
   * */
  void playerSetToken(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor token
  ) throws RemoteException;

  /**
   * Interface method used to process the event of a player choosing their nickname.
   * @param gameId The identifier of the game lobby in which the player has set their nickname.
   * @param connectionID The unique identifier of the player that has set their nickname.
   * @param nickname The nickname of the player that has set their nickname
   * */
  void playerSetNickname(String gameId, UUID connectionID, String nickname)
    throws RemoteException;

  /**
   * Interface method used to process the event of a player choosing their objective card.
   * @param gameId The identifier of the game lobby in which the player has set their objective card.
   * @param connectionID The unique identifier of the player that has set their objective card.
   * @param nickname The nickname of the player that has set their objective card, if they have chosen it already
   */
  void playerChoseObjectiveCard(
    String gameId,
    UUID connectionID,
    String nickname
  ) throws RemoteException;

  /**
   * Interface method used to process the event of a player joining a game.
   * This happens when a player chooses which side of their starter card they prefer to place
   * @param gameId The identifier of the game that the player has joined.
   * @param connectionID The unique identifier of the player that has joined the game.
   * @param nickname The nickname of the player that has joined the game.
   * @param color The token color of the player that has joined the game.
   * @param handIDs The unique identifiers of the cards in the player's hand.
   * @param starterCard The id of the starter card  the player was given as their starter card.
   * @param starterSide The side of the card that the player has chosen to place.
   * */
  void playerJoinedGame(
    String gameId,
    UUID connectionID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs,
    Integer starterCard,
    CardSideType starterSide
  ) throws RemoteException;

  /**
   * Interface method used to process the event of a game starting.
   * @param gameId The identifier of the game that has just started.
   * @param gameInfo The information about the game that has just started.
   *
   * */
  void gameStarted(String gameId, GameInfo gameInfo) throws RemoteException;

  /**
   * Interface method used to process the event of the player ending their turn drawing a card
   *
   * @param gameId The unique identifier of the game that has just ended.
   * @param playerNickname The nickname of the player that has just finished their turn. This can be null if the game ended because of a disconnection.
   * @param playerIndex The index of the player that has just finished their turn in the list of players.
   * @param isLastRound A boolean indicating if this is the last round of the game.
   * @param source The source from which the card was drawn.
   * @param deck The type of deck from which the card was drawn.
   * @param cardId The unique identifier of the card that was drawn. (null if the receiving client is not the one who drew the card)
   * @param newPairCardId The unique identifier of the new pair card that was drawn.
   * @param availableSpots A set of positions that are available for the next player to place a card.
   * @param forbiddenSpots A set of positions that are forbidden for the next player to place a card.
   * @throws RemoteException If a remote or network error occurs.
   */
  void changeTurn(
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
  ) throws RemoteException;

  /**
   * Interface method used to process the event of the player ending their turn choosing not to draw a card.
   * This possibility is given by the fact that the decks might be empty,
   * A player can choose not to draw in their last turn if they prefer not to draw from the pairs available on the gameboard
   *
   * @param gameId The unique identifier of the game that has just ended.
   * @param playerNickname The nickname of the player that has just finished their turn. This can be null if the game ended because of a disconnection.
   * @param playerIndex The index of the player that has just finished their turn in the list of players.
   * @param isLastRound A boolean indicating if this is the last round of the game.
   * @param availableSpots A set of positions that are available for the next player to place a card.
   * @param forbiddenSpots A set of positions that are forbidden for the next player to place a card.
   * @param resourceDeckTopCardId The unique identifier of the top card in the resource deck.
   * @param goldDeckTopCardId The unique identifier of the top card in the gold deck.
   * @throws RemoteException If a remote or network error occurs.
   */
  void changeTurn(
    String gameId,
    String playerNickname,
    Integer playerIndex,
    Boolean isLastRound,
    Set<Position> availableSpots,
    Set<Position> forbiddenSpots,
    Integer resourceDeckTopCardId,
    Integer goldDeckTopCardId
  ) throws RemoteException;

  /**
   * Interface method used to process the event of the current player placing a card on their playerboard.
   * @param gameId The identifier of the game in which the card was placed.
   * @param playerId The identifier of the player that placed the card.
   * @param playerHandCardNumber The number of the card in the player's hand that was placed.
   * @param cardId The unique identifier of the card that was placed.
   * @param side The side of the card that was placed.
   * @param position The position on the player board where the card was placed.
   * @param newPlayerScore The updated score of the player that placed the card.
   * @param updatedResources The updated resources of the player that placed the card.
   *                         The key is the type of resource and the value is the new amount of that resource.
   * @param updatedObjects The updated objects of the player that placed the card.
   *                       The key is the type of object and the value is the new amount of that object.
   * @param availableSpots A set of positions that are available for the next player to place a card.
   * @param forbiddenSpots A set of positions that are forbidden for the next player to place a card.
   *                       This set is used to display the forbidden spots on the gameboard.
   * */
  void cardPlaced(
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
  ) throws RemoteException;

  /**
   * Interface method used to process the event of the game ending.
   * */
  void gameOver() throws RemoteException;

  /**
   * Interface method used to process the event of the player's score being updated after the game ends and secret and common objectives are evaluated.
   * @param newScores A map containing the updated scores of the players.
   *                  The key is the nickname of the player and the value is the new score of the player.
   * */
  void playerScoresUpdate(Map<String, Integer> newScores)
    throws RemoteException;

  /**
   * Interface method used to process the event of the number of remaining rounds being updated or set when winning score is reached by any of the players.
   * @param gameID The identifier of the game in which the number of remaining rounds was updated.
   * @param remainingRounds The number of rounds remaining in the game.
   * */
  void remainingRounds(String gameID, int remainingRounds)
    throws RemoteException;

  /**
   * Interface method used to process and display the winning player of the game.
   * @param nickname The nickname of the player that has won the game.
   */
  void winningPlayer(String nickname) throws RemoteException;

  /**
   * Interface method used to process the event of a player's connection status changing.
   * @param connectionID The unique identifier of the player whose connection status has changed.
   *                 This is the identifier that the player uses to connect to the server.
   * @param nickname The nickname of the player whose connection status has changed.
   * @param status The new connection status of the player.
   * */
  void playerConnectionChanged(
    UUID connectionID,
    String nickname,
    GameController.UserGameContext.ConnectionStatus status
  ) throws RemoteException;

  /**
   * Interface method used to process the event of a player joining a game lobby
   * when there are other players already inside it that may or may not have already completed the process of joining the game.
   * @param usersInfo The information about the users in the lobby.
   *                  It includes the nickname, token color, objective card choice, starter card side chosen, and in-game status of each user.
   * */
  void lobbyInfo(LobbyUsersInfo usersInfo) throws RemoteException;

  /**
   * Interface method used to process the event of a player sending a chat message in the game they're in.
   * @param gameID The identifier of the game in which the chat message was sent.
   * @param message The chat message that was sent by the player.
   * */
  void chatMessage(String gameID, ChatMessage message) throws RemoteException;

  /**
   * Interface method to process the event of a client requesting their secret objective card
   * @param objectiveCards A pair of integers representing the unique identifiers of the secret objective cards.
   *                       The first integer is the unique identifier of the secret objective card that the player has chosen.
   *                       The second integer is the unique identifier of the secret objective card that the player has not chosen.
   * */
  void getObjectiveCards(Pair<Integer, Integer> objectiveCards)
    throws RemoteException;

  /**
   * Interface method to process the event of a client requesting their starter card
   * @param cardId The unique identifier of the starter card that the player has chosen.
   */
  void getStarterCard(Integer cardId) throws RemoteException;

  void gameHalted(String gameID) throws RemoteException;
  void gameResumed(String gameID) throws RemoteException;

  void userContext(FullUserGameContext context) throws RemoteException;
}
