package polimi.ingsw.am21.codex.connection.server.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.UUID;
import polimi.ingsw.am21.codex.controller.GameController;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

public class RMIConnectionHandler implements Remote {

  GameController controller;

  // TODO: Implement this method when the client is ready
  // Client client;
  public RMIConnectionHandler(GameController controller) {
    super();
    this.controller = controller;
  }

  // TODO: Implement this method when the client is ready
  /*public void setClient(Client client) {
    this.client = client;
    this.controller.addListener(client.getListener());
  }*/

  public Set<String> getGames() throws RemoteException {
    return this.controller.getGames();
  }

  public void joinLobby(String gameId, UUID connectionID)
    throws RemoteException, GameNotFoundException, LobbyFullException {
    this.controller.joinLobby(gameId, connectionID);
  }

  public void lobbySetNickname(
    String gameId,
    UUID connectionID,
    String username
  ) throws NicknameAlreadyTakenException, GameNotFoundException {
    this.controller.lobbySetNickname(gameId, connectionID, username);
  }

  public void lobbySetTokenColor(
    String gameId,
    UUID connectionID,
    TokenColor tokenColor
  ) throws GameNotFoundException {
    this.controller.lobbySetTokenColor(gameId, connectionID, tokenColor);
  }

  public void lobbySetObjectiveCard(
    String gameId,
    UUID connectionID,
    Boolean isFirst
  ) throws GameNotFoundException {
    this.controller.lobbyChooseObjective(gameId, connectionID, isFirst);
  }

  public void lobbyJoinGame(
    String gameId,
    UUID connectionID,
    CardSideType starterCardSize
  )
    throws GameNotReadyException, EmptyDeckException, IllegalCardSideChoiceException, IllegalPlacingPositionException, GameNotFoundException {
    this.controller.joinGame(gameId, connectionID, starterCardSize);
  }

  public void createGame(String gameId, UUID socketID, Integer players)
    throws RemoteException, EmptyDeckException {
    this.controller.createGame(gameId, socketID, players);
  }

  public void placeCard(
    String gameId,
    String playerNickname,
    Integer playerHandCardNumber,
    CardSideType side,
    Position position
  )
    throws RemoteException, GameNotFoundException, PlayerNotActive, IllegalCardSideChoiceException, IllegalPlacingPositionException {
    this.controller.placeCard(
        gameId,
        playerNickname,
        playerHandCardNumber,
        side,
        position
      );
  }

  public void nextTurn(String gameId, String playerNickname)
    throws GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive, GameOverException, RemoteException {
    this.controller.nextTurn(gameId, playerNickname);
  }

  public void nextTurn(
    String gameId,
    String playerNickname,
    DrawingCardSource drawingSource,
    DrawingDeckType deckType
  )
    throws GameNotFoundException, InvalidNextTurnCallException, PlayerNotActive, GameOverException, EmptyDeckException {
    this.controller.nextTurn(gameId, playerNickname, drawingSource, deckType);
  }
}
