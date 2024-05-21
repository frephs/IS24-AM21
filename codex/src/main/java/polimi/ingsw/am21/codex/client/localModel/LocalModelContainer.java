package polimi.ingsw.am21.codex.client.localModel;

import java.util.*;
import polimi.ingsw.am21.codex.controller.listeners.GameErrorListener;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class LocalModelContainer
  implements GameEventListener, GameErrorListener {

  private LocalGameBoard localGameBoard;
  private LocalLobby localLobby;

  private final View view;

  private final CardsLoader cardsLoader = new CardsLoader();

  private UUID socketId;

  public LocalModelContainer(UUID socketID, View view) {
    this.view = view;
    this.socketId = socketID;

    cardsLoader.loadCards();
  }

  public LocalGameBoard getLocalGameBoard() {
    return localGameBoard;
  }

  public void setSocketId(UUID socketId) {
    this.socketId = socketId;
  }

  @Override
  public void unknownResponse() {
    view.postNotification(Notification.UNKNOWN_RESPONSE);
  }

  @Override
  public void gameAlreadyStarted() {
    view.postNotification(NotificationType.ERROR, "Game has already started");
  }

  @Override
  public void gameNotStarted() {
    view.postNotification(NotificationType.ERROR, "Game not started");
  }

  @Override
  public void gameNotFound() {
    view.postNotification(NotificationType.ERROR, "Game not found");
  }

  public void getGames(
    Set<String> gameIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  ) {
    Set<String> availableGames = localLobby.getAvailableGames();
    Map<String, Integer> playerSlots = localLobby.getPlayerSlots();
    Map<String, Integer> maxPlayerSlots = localLobby.getMaxPlayerSlots();

    if (gameIds.isEmpty()) {
      view.postNotification(NotificationType.WARNING, "No games available");
    } else {
      availableGames.addAll(gameIds);
      playerSlots.putAll(currentPlayers);
      maxPlayerSlots.putAll(maxPlayers);
    }
    view.drawAvailableGames(availableGames, playerSlots, maxPlayerSlots);
  }

  @Override
  public void gameCreated(String gameId, int currentPlayers, int maxPlayers) {
    Set<String> availableGames = localLobby.getAvailableGames();
    Map<String, Integer> playerSlots = localLobby.getPlayerSlots();
    Map<String, Integer> maxPlayerSlots = localLobby.getMaxPlayerSlots();

    availableGames.add(gameId);
    playerSlots.put(gameId, currentPlayers);
    maxPlayerSlots.put(gameId, maxPlayers);

    view.drawAvailableGames(availableGames, playerSlots, maxPlayerSlots);
  }

  @Override
  public void gameDeleted(String gameId) {
    Set<String> availableGames = localLobby.getAvailableGames();
    Map<String, Integer> playerSlots = localLobby.getPlayerSlots();
    Map<String, Integer> maxPlayerSlots = localLobby.getMaxPlayerSlots();

    availableGames.remove(gameId);
    playerSlots.remove(gameId);
    maxPlayerSlots.remove(gameId);

    view.postNotification(
      NotificationType.ERROR,
      "Game " + gameId + " not found. "
    );
    view.drawAvailableGames(availableGames, playerSlots, maxPlayerSlots);
  }

  @Override
  public void lobbyFull() {
    view.postNotification(
      NotificationType.ERROR,
      "Game " + localLobby.getGameId() + " lobby is full. "
    );
  }

  @Override
  public void gameFull(String gameId) {
    view.postNotification(
      NotificationType.ERROR,
      "Game " + gameId + " is full. "
    );
  }

  public String getGameId() {
    return localGameBoard.getGameId();
  }

  public Set<TokenColor> getTokenColor() {
    return localLobby.getAvailableTokens();
  }

  @Override
  public void playerJoinedLobby(String gameId, UUID socketId) {
    if (socketId == this.socketId) {
      localLobby = new LocalLobby(gameId);
      localGameBoard = new LocalGameBoard(
        gameId,
        localLobby.getMaxPlayerSlots().get(gameId)
      );

      localLobby.getPlayers().add(this.socketId);
      view.postNotification(
        NotificationType.RESPONSE,
        "You joined the lobby of the game" + gameId
      );
    } else {
      //TODO(server) send a playerJoinedLobbyUpdate for every player that is already in the lobby, also token color and nickname should be sent if they set it.
      localLobby.getPlayers().add(this.socketId);
      view.postNotification(
        NotificationType.UPDATE,
        "Player" + socketId + " joined game " + gameId
      );
    }
  }

  public void playerLeftLobby() {
    playerLeftLobby(localLobby.getGameId(), this.socketId);
  }

  @Override
  public void playerLeftLobby(String gameId, UUID socketID) {
    if (socketID == this.socketId) {
      localLobby = null;
      localGameBoard = null;

      view.postNotification(
        NotificationType.RESPONSE,
        "You left the lobby of the game" + gameId + ". "
      );
    } else {
      localLobby.getPlayers().remove(socketID);
      view.postNotification(
        NotificationType.UPDATE,
        "Player" + socketID + " left the game lobby " + gameId + ". "
      );
      Map<UUID, String> nicknames = localLobby.getNicknames();
      nicknames.remove(socketID);
      Map<UUID, TokenColor> tokens = localLobby.getTokens();
      tokens.remove(socketID);
      view.drawLobby(tokens, nicknames);
    }
  }

  public void playerSetToken(TokenColor color) {
    playerSetToken(localLobby.getGameId(), this.socketId, color);
  }

  @Override
  public void playerSetToken(String gameId, UUID socketID, TokenColor token) {
    Set<TokenColor> availableTokens = localLobby.getAvailableTokens();
    availableTokens.remove(token);

    view.postNotification(
      NotificationType.UPDATE,
      new String[] { socketID.toString(), " chose the ", " token. " },
      token,
      2
    );

    view.drawAvailableTokenColors(availableTokens);
    view.drawLobby(localLobby.getTokens(), localLobby.getNicknames());
  }

  @Override
  public void tokenTaken(TokenColor token) {
    view.postNotification(
      NotificationType.ERROR,
      new String[] { "The", "token is already taken" },
      token,
      2
    );
  }

  public void playerSetNickname(String nickname) {
    this.playerSetNickname(localLobby.getGameId(), this.socketId, nickname);
  }

  @Override
  public void playerSetNickname(String gameId, UUID socketId, String nickname) {
    if (this.socketId.equals(socketId)) {
      view.postNotification(
        NotificationType.UPDATE,
        "You chose the nickname" + nickname + ". "
      );
    } else {
      view.postNotification(
        NotificationType.UPDATE,
        "Player " + socketId + " chose the nickname" + nickname + ". "
      );
    }
    view.drawLobby(localLobby.getTokens(), localLobby.getNicknames());
  }

  @Override
  public void nicknameTaken(String nickname) {
    view.postNotification(
      NotificationType.ERROR,
      "The nickname " + nickname + " is already taken. "
    );
  }

  @Override
  public void notInGame() {
    view.postNotification(NotificationType.ERROR, "You are not in a game. ");
  }

  @Override
  public void playerChoseObjectiveCard(Boolean isFirst) {
    this.localGameBoard.setSecretObjective(
        isFirst
          ? this.localLobby.getAvailableObjectives().getFirst()
          : this.localLobby.getAvailableObjectives().getSecond()
      );
    this.view.postNotification(
        NotificationType.RESPONSE,
        "Secret objective chosen. "
      );
  }

  @Override
  public void playerJoinedGame(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor color,
    List<Integer> cardIds
  ) {
    List<Card> hand = cardsLoader.getCardsFromIds(cardIds);
    // TODO
    //    localGameBoard.getPlayers().add(new LocalPlayer(nickname, color, hand));
    view.postNotification(
      NotificationType.UPDATE,
      "Player " + nickname + " joined game " + gameId + ". "
    );
    view.drawLeaderBoard(localGameBoard.getPlayers());
    view.drawPlayerBoards(localGameBoard.getPlayers());
  }

  @Override
  public void gameStarted(String gameId, List<String> players) {
    Map<String, Integer> nicknameToIndex = new HashMap<>();
    for (int i = 0; i < players.size(); i++) {
      String currentNickname = players.get(i);
      if (
        Objects.equals(
          currentNickname,
          localLobby.getNicknames().get(this.socketId)
        )
      ) {
        localGameBoard.setPlayerIndex(i);
      }
      nicknameToIndex.put(currentNickname, i);
    }

    this.localGameBoard.getPlayers()
      .sort(
        Comparator.comparingInt(
          player ->
            nicknameToIndex.getOrDefault(
              player.getNickname(),
              Integer.MAX_VALUE
            )
        )
      );

    view.postNotification(
      NotificationType.UPDATE,
      "Game " + gameId + " started. "
    );

    view.drawGame(localGameBoard.getPlayers());
  }

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
    Set<Position> availablePositions,
    Set<Position> forbiddenPositions
  ) {
    Card card = cardsLoader.getCardFromId(cardId);
    localGameBoard.getCurrentPlayer().addPlayedCards(card, side, position);

    view.postNotification(NotificationType.UPDATE, "Card" + cardId + " placed");
    view.drawCardPlacement(card, side, position);

    diffMessage(
      newPlayerScore - localGameBoard.getCurrentPlayer().getPoints(),
      "point"
    );

    Arrays.stream(ResourceType.values()).forEach(resourceType -> {
      diffMessage(
        updatedResources.get(resourceType) -
        localGameBoard.getCurrentPlayer().getResources().get(resourceType),
        resourceType
      );
    });

    Arrays.stream(ObjectType.values()).forEach(objectType -> {
      diffMessage(
        updatedObjects.get(objectType) -
        localGameBoard.getCurrentPlayer().getResources().get(objectType),
        objectType
      );
    });

    localGameBoard.getCurrentPlayer().getResources().putAll(updatedResources);
    localGameBoard.getCurrentPlayer().getObjects().putAll(updatedObjects);

    localGameBoard.getCurrentPlayer().setAvailableSpots(availablePositions);
    localGameBoard.getCurrentPlayer().setForbiddenSpots(forbiddenPositions);

    view.drawPlayerBoard(localGameBoard.getCurrentPlayer());
  }

  void diffMessage(int diff, String attributeName) {
    if (diff != 0) {
      view.postNotification(
        NotificationType.UPDATE,
        localGameBoard.getCurrentPlayer().getNickname() +
        (diff > 0 ? "gained" : "lost" + diff) +
        attributeName +
        ((Math.abs(diff) != 1) ? "s" : "") +
        ". "
      );
    }
  }

  void diffMessage(int diff, Colorable colorable) {
    if (diff != 0) {
      view.postNotification(
        NotificationType.UPDATE,
        new String[] {
          localGameBoard.getCurrentPlayer().getNickname(),
          (diff > 0 ? "gained" : "lost" + diff),
          ((Math.abs(diff) != 1) ? "s" : ""),
          ". ",
        },
        colorable,
        2
      );
    }
  }

  @Override
  public void changeTurn(
    String gameId,
    String playerId,
    Boolean isLastRound,
    DrawingCardSource source,
    DrawingDeckType deck,
    Integer drawnCardId,
    Integer newPairCardId
  ) {
    Card drawnCard = cardsLoader.getCardFromId(drawnCardId);

    localGameBoard.getCurrentPlayer().getHand().add(drawnCard);
    view.drawPlayerBoard(localGameBoard.getCurrentPlayer());

    switch (source) {
      case CardPairFirstCard, CardPairSecondCard -> {
        Card newPairCard = cardsLoader.getCardFromId(newPairCardId);
        CardPair<Card> cardPairToUpdate =
          switch (deck) {
            case GOLD -> localGameBoard.getGoldCards();
            case RESOURCE -> localGameBoard.getResourceCards();
          };

        switch (source) {
          case CardPairFirstCard -> cardPairToUpdate.replaceFirst(newPairCard);
          case CardPairSecondCard -> cardPairToUpdate.replaceSecond(
            newPairCard
          );
        }

        view.drawCardDraw(deck, newPairCard);
      }
      case Deck -> view.drawCardDraw(deck);
    }

    view.postNotification(
      NotificationType.UPDATE,
      localGameBoard.getCurrentPlayer().getNickname() +
      "has drawn a card from the " +
      source.toString().toLowerCase() +
      " " +
      deck.toString().toLowerCase() +
      ". "
    );

    changeTurn(gameId, playerId, isLastRound);
  }

  @Override
  public void changeTurn(String gameId, String playerId, Boolean isLastRound) {
    if (isLastRound) {
      view.postNotification(NotificationType.WARNING, "Last round of the game");
    }

    view.postNotification(
      NotificationType.UPDATE,
      "It's" + localGameBoard.getCurrentPlayer().getNickname() + "'s turn. "
    );

    int nextPlayerIndex =
      ((localGameBoard
            .getPlayers()
            .indexOf(
              localGameBoard
                .getPlayers()
                .stream()
                .filter(player -> player.getNickname().equals(playerId))
                .findFirst()
                .orElseThrow(() -> {
                  // TODO replace with a better error?
                  return new RuntimeException("No player found");
                })
            )) +
        1) %
      localGameBoard.getPlayers().size();

    localGameBoard.setCurrentPlayer(nextPlayerIndex);
  }

  @Override
  public void playerNotActive() {
    view.postNotification(NotificationType.ERROR, "It's not your turn. ");
  }

  @Override
  public void invalidNextTurnCall() {
    view.postNotification(NotificationType.ERROR, "Invalid next turn call. ");
  }

  @Override
  public void gameOver() {
    view.postNotification(NotificationType.WARNING, "Game over. ");
  }

  @Override
  public void emptyDeck() {
    view.postNotification(NotificationType.ERROR, "Deck is empty. ");
  }

  public View getView() {
    return this.view;
  }
}
