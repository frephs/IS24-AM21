package polimi.ingsw.am21.codex.controller.utils;

import java.rmi.RemoteException;

@FunctionalInterface
public interface RemoteListenerFunction<T> {
  void apply(T t) throws RemoteException;
}
