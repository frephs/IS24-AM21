package polimi.ingsw.am21.codex.controller.rmi;

import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.GameBoard.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.GameBoard.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.TokenColor;

import java.rmi.RemoteException;

public interface RemoteLobbyClient {
  void joinLobby(int numLobby) throws RemoteException;

  void playerNicknameSet(String nickname) throws RemoteException, NicknameAlreadyTakenException;
  void tokenColorSet(TokenColor tokenColor) throws RemoteException, TokenAlreadyTakenException;
  void selectObjectiveCard(int id) throws RemoteException;
  void selectStarterSide(CardSideType cardSideType) throws RemoteException;

}
