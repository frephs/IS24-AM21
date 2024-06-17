package polimi.ingsw.am21.codex.view;

import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.RMI.RMIClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.TCP.TCPClientConnectionHandler;
import polimi.ingsw.am21.codex.controller.listeners.GameEventListener;

public abstract class ViewClient {

  protected ClientConnectionHandler client;
  protected View view;

  public ViewClient(View view) {
    this.view = view;
  }

  public void start(ConnectionType connectionType, String address, int port) {
    if (connectionType == ConnectionType.TCP) {
      client = new TCPClientConnectionHandler(address, port, view);
    } else {
      client = new RMIClientConnectionHandler(address, port, view);
    }
    client.connect();
  }

  @Override
  public void gameCreated(String gameId, int currentPlayers, int maxPlayers) {
    view.getLocalModel().gameCreated(gameId, currentPlayers, maxPlayers);
  }
}
