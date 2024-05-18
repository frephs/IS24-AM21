package polimi.ingsw.am21.codex.connection.client.RMI;

import javafx.geometry.Pos;
import polimi.ingsw.am21.codex.client.localModel.LocalGameBoard;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.server.RMI.RMIConnectionHandler;
import polimi.ingsw.am21.codex.controller.exceptions.GameAlreadyStartedException;
import polimi.ingsw.am21.codex.controller.exceptions.GameNotFoundException;
import polimi.ingsw.am21.codex.controller.exceptions.PlayerNotActive;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameState;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.UUID;

public class RMIClientConnectionHandler implements ClientConnectionHandler, Remote {
  private  LocalGameBoard localGameBoard;
  private  LocalPlayer localPlayer;
  private final RMIConnectionHandler rmiConnectionHandler;
  private final UUID index;

  public RMIClientConnectionHandler(RMIConnectionHandler rmiConnectionHandler) {
    this.rmiConnectionHandler = rmiConnectionHandler;
    index = UUID.randomUUID();
  }


  @Override
  public void connect() {
  }

  @Override
  public void listGames() {
    try{
      rmiConnectionHandler.getGames();
    } catch (RemoteException e) {

    }
  }

  @Override
  public void connectToGame(String gameId) {
    try{
      rmiConnectionHandler.joinLobby(gameId, this.index);
    } catch (GameAlreadyStartedException e) {

    } catch (LobbyFullException e) {

    } catch (RemoteException e) {

    } catch (GameNotFoundException e) {

    }
  }

  @Override
  public void createAndConnectToGame(String gameId, int numberPlayers)
  throws EmptyDeckException, RemoteException, GameAlreadyStartedException, LobbyFullException, GameNotFoundException {
    rmiConnectionHandler.createGame(gameId, this.index, numberPlayers);
    rmiConnectionHandler.joinLobby(gameId, this.index);
  }

  //TODO how can i know which game started and which no?
  @Override
  public void checkIfGameStarted() throws RemoteException {
  }

  @Override
  public void lobbySetToken(TokenColor color)
  throws GameAlreadyStartedException, GameNotFoundException {
    rmiConnectionHandler.lobbySetTokenColor(this.localGameBoard.getGameId(), this.index, color);
  }

  @Override
  public Set<TokenColor> getTokens() {
    return null;
  }

  @Override
  public void lobbySetNickname(String nickname)
  throws GameAlreadyStartedException, NicknameAlreadyTakenException, GameNotFoundException {
    rmiConnectionHandler.lobbySetNickname(localGameBoard.getGameId(), this.index, nickname);
  }


  //TODO capire cosa cambia nell localGameBoard
  @Override
  public void lobbyChooseObjectiveCard(Boolean first)
  throws GameAlreadyStartedException, GameNotFoundException {
    rmiConnectionHandler.lobbySetObjectiveCard(localGameBoard.getGameId(),this.index, first);
  }

  @Override
  public void lobbyJoinGame(CardSideType cardSide)
  throws GameNotReadyException, GameAlreadyStartedException, EmptyDeckException, IllegalCardSideChoiceException, IllegalPlacingPositionException, GameNotFoundException {
    rmiConnectionHandler.lobbyJoinGame(localGameBoard.getGameId(),this.index, cardSide);
  }

  @Override
  public void placeCard(Integer playerHandCardNumber, CardSideType side, Position position)
  throws PlayerNotActive, IllegalCardSideChoiceException, RemoteException, IllegalPlacingPositionException, GameNotFoundException {
    rmiConnectionHandler.placeCard(localGameBoard.getGameId(), localPlayer.getNickname(), playerHandCardNumber, side, position);
  }

  @Override
  public void nextTurn(DrawingCardSource drawingSource, DrawingDeckType deckType)
  throws PlayerNotActive, GameOverException, EmptyDeckException, InvalidNextTurnCallException, GameNotFoundException {
    rmiConnectionHandler.nextTurn(localGameBoard.getGameId(), localPlayer.getNickname(),drawingSource, deckType);

  }

  @Override
  public void nextTurn()
  throws PlayerNotActive, GameOverException, InvalidNextTurnCallException, RemoteException, GameNotFoundException {
    rmiConnectionHandler.nextTurn(localGameBoard.getGameId(), localPlayer.getNickname());
  }

  @Override
  public GameState getGameState() {
    return null;
  }

}
