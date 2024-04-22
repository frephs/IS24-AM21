package polimi.ingsw.am21.codex.controller.rmi;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Lobby.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.GameBoard.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteLobbyServer extends Remote {
  void setPlayerNickname() throws RemoteException;
  void setTokenColor() throws RemoteException;
  void setObjectiveCardMessage(int id_1, int id_2) throws RemoteException;
  void setStarterCardSide(int id) throws RemoteException;
  void joinGame() throws RemoteException;

}
