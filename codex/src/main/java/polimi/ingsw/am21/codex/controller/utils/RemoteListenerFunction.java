package polimi.ingsw.am21.codex.controller.utils;

import java.rmi.RemoteException;
import java.util.UUID;
import polimi.ingsw.am21.codex.controller.listeners.RemoteGameEventListener;

@FunctionalInterface
public interface RemoteListenerFunction {
  void apply(RemoteGameEventListener listener, UUID connectionID)
    throws RemoteException;
}
