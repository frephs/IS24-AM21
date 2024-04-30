package polimi.ingsw.am21.codex.controller.rmi;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameManager extends Remote {
  void joinGame(String nickname, TokenColor tokenColor) throws RemoteException;
  void placeCard(String nickname, PlayableCard playableCard) throws RemoteException;
  void drawCard(String nickname, PlayableCard playableCard) throws RemoteException;
  void chooseObjective(String nickname, ObjectiveCard objectiveCard) throws RemoteException;
  void showOtherBoard(String nickname) throws RemoteException;
  void getPoint() throws RemoteException;

}
