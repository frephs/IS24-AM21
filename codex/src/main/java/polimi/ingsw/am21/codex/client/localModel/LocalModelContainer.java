package polimi.ingsw.am21.codex.client.localModel;

import java.util.*;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.NotificationType;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;
import polimi.ingsw.am21.codex.view.View;

public class LocalModelContainer implements GameEventListener {

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

  // TODO add methods for getters (listers)

  @Override
  public void gameCreated(String gameId, int players) {
    //TODO add createGameMessage
    Set<String> availableGames = localLobby.getAvailableGames();
    availableGames.add(gameId);
    Map<String, Integer> playersPerGame = localLobby.getPlayersPerGame();
    playersPerGame.put(gameId, players);
    view.drawAvailableGames(availableGames, playersPerGame);
  }

  @Override
  public void gameDeleted(String gameId) {
    // TODO add gameDeletedMessage
    Set<String> availableGames = localLobby.getAvailableGames();
    availableGames.remove(gameId);
    Map<String, Integer> playersPerGame = localLobby.getPlayersPerGame();
    playersPerGame.remove(gameId);
    view.postNotification(
      NotificationType.ERROR,
      "Game " + gameId + " not found. "
    );
    view.drawAvailableGames(availableGames, localLobby.getPlayersPerGame());
  }

  @Override
  public void playerJoinedLobby(String gameId, UUID socketId) {
    if (socketId == this.socketId) {
      localLobby = new LocalLobby(gameId);
      localGameBoard = new LocalGameBoard(
        gameId,
        localLobby.getPlayersPerGame().get(gameId)
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
        "Player" + socketID + " joined game " + gameId + ". "
      );
      Map<UUID, String> nicknames = localLobby.getNicknames();
      nicknames.remove(socketID);
      Map<UUID, TokenColor> tokens = localLobby.getTokens();
      tokens.remove(socketID);
      view.drawLobby(tokens, nicknames);
    }
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
  public void playerSetNickname(String gameId, UUID socketID, String nickname) {
    view.postNotification(
      NotificationType.UPDATE,
      "Player " + socketID + " chose the nickname" + nickname + ". "
    );
    view.drawLobby(localLobby.getTokens(), localLobby.getNicknames());
  }

  @Override
  public void playerChoseObjectiveCard(
    String gameId,
    UUID socketID,
    Boolean isFirst
  ) {
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
    localGameBoard.getPlayers().add(new LocalPlayer(nickname, color, hand));
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
    Integer playerHandCardNumber,
    Integer cardId,
    CardSideType side,
    Position position,
    int newPlayerScore,
    Map<ResourceType, Integer> updatedResources,
    Map<ObjectType, Integer> updatedObjects
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
    Integer nextPlayer,
    DrawingCardSource source,
    DrawingDeckType deck,
    Integer cardId,
    Boolean isLastRound
  ) {
    Card newCard = cardsLoader.getCardFromId(cardId);

    localGameBoard.getCurrentPlayer().getHand().add(newCard);

    view.drawPlayerBoard(localGameBoard.getCurrentPlayer());
    if (
      source == DrawingCardSource.CardPairFirstCard ||
      source == DrawingCardSource.CardPairSecondCard
    ) {
      view.drawCardDraw(deck, newCard);
    } else if (source == DrawingCardSource.Deck) {
      view.drawCardDraw(deck);
    }

    view.postNotification(
      NotificationType.UPDATE,
      localGameBoard.getCurrentPlayer().getNickname() +
      "has drawn a card from the " +
      deck.toString().toLowerCase() +
      source.toString().toLowerCase() +
      ". "
    );
    changeTurn(gameId, nextPlayer, isLastRound);
  }

  @Override
  public void changeTurn(
    String gameId,
    Integer nextPlayer,
    Boolean isLastRound
  ) {
    if (isLastRound) {
      view.postNotification(NotificationType.WARNING, "Last round of the game");
    }

    view.postNotification(
      NotificationType.UPDATE,
      "It's" + localGameBoard.getCurrentPlayer().getNickname() + "'s turn. "
    );
    localGameBoard.setCurrentPlayer(nextPlayer);
  }
}
