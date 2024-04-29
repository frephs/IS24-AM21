package polimi.ingsw.am21.codex.controller;

import javafx.util.Pair;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.controller.listeners.ControllerEventListener;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Game;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameManager;
import polimi.ingsw.am21.codex.model.Lobby.Lobby;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.IncompletePlayerBuilderException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Player.Player;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

import java.net.Socket;
import java.util.*;

public class GameController {
  GameManager manager;
  List<ControllerEventListener> listeners;

  public GameController() {
    manager = new GameManager();
  }


  public Set<String> getGames() {
    return manager.getGames();
  }

  public Game getGame(String gameId) throws GameNotFoundException {
    Optional<Game> oGame = manager.getGame(gameId);
    if (oGame.isEmpty()) {
      throw new GameNotFoundException();
    }
    return oGame.get();
  }


  public void removePlayerFromLobby(Game game, UUID socketID) {
    Lobby lobby = game.getLobby();
    Pair<CardPair<ObjectiveCard>, PlayableCard> oldPlayerCards =
      lobby.removePlayer(socketID);
    game.insertObjectiveCard(oldPlayerCards.getKey().getFirst());
    game.insertObjectiveCard(oldPlayerCards.getKey().getSecond());
    PlayableCard starterCard = oldPlayerCards.getValue();
    starterCard.clearPlayedSide();
    game.insertStarterCard(starterCard);

  }

  public void removePlayerFromLobby(String gameId, UUID socketID)
  throws GameNotFoundException {
    Game game = this.getGame(gameId);
    this.removePlayerFromLobby(game, socketID);
  }

  public void joinLobby(String gameId, UUID socketID)
  throws GameNotFoundException, LobbyFullException {
    Optional<Game> oGame = manager.getGame(gameId);

    if (oGame.isEmpty()) {
      throw new GameNotFoundException();
    }

    Game game = oGame.get();

    Lobby lobby = game.getLobby();
    try {
      if (lobby.containsSocketID(socketID)) {
        this.removePlayerFromLobby(game, socketID);
      }
      lobby.addPlayer(socketID, game.drawObjectiveCardPair(),
        game.drawStarterCard());
      listeners.forEach(listener -> {
        listener.playerJoinedLobby(gameId, socketID);
      });
    } catch (EmptyDeckException e) {
      throw new RuntimeException("EmptyDeckException");
    }
  }

  public void joinGame(String gameId, UUID socketID, CardSideType sideType)
  throws GameNotFoundException, IncompletePlayerBuilderException {
    Game game = this.getGame(gameId);
    Player newPlayer = game.getLobby()
      .finalizePlayer(socketID, sideType, game.drawHand());
    game.addPlayer(newPlayer);
    listeners.forEach(listener -> listener.playerJoinedGame(gameId, socketID, newPlayer.getNickname()));
  }


  public void createGame(String gameId, Integer players) {
    manager.createGame(gameId, players);
    listeners.forEach(listener -> {
      listener.gameCreated(gameId);
    });
  }

  public void deleteGame(String gameId) {
    manager.deleteGame(gameId);
    listeners.forEach(listener -> {
      listener.gameDeleted(gameId);
    });
  }

  private void checkIfCurrentPlayer(Game game, String playerNickname)
  throws PlayerNotActive {
    Player player = game.getCurrentPlayer();
    if (player.getNickname().equals(playerNickname))
      throw new PlayerNotActive();
  }

  private void sendNextTurnEvents(Game game) {
    if (game.isLastRound()) {
      listeners.forEach(listener -> listener.changeTurn(game.getCurrentPlayerIndex(), game.isLastRound()));
    }
  }

  public void nextTurn(String gameId, String playerNickname)
  throws GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive
    , GameOverException {
    Game game = this.getGame(gameId);
    this.checkIfCurrentPlayer(game, playerNickname);
    game.nextTurn();
    this.sendNextTurnEvents(game);
  }

  public void nextTurn(String gameId, String playerNickname,
                       DrawingCardSource drawingSource,
                       DrawingDeckType deckType)
  throws GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive
    , GameOverException, EmptyDeckException {
    Game game = this.getGame(gameId);
    this.checkIfCurrentPlayer(game, playerNickname);
    game.nextTurn(drawingSource, deckType);
    this.sendNextTurnEvents(game);
  }

  public void addListener(ControllerEventListener listener) {
    listeners.add(listener);
  }

  public void removeListener(ControllerEventListener listener) {
    listeners.remove(listener);
  }

  // TODO: implement
  public void placeCard() {

  }
}
