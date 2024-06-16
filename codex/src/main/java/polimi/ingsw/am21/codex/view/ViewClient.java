package polimi.ingsw.am21.codex.view;

import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.connection.client.ClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.RMI.RMIClientConnectionHandler;
import polimi.ingsw.am21.codex.connection.client.TCP.TCPClientConnectionHandler;

public abstract class ViewClient {

  protected LocalModelContainer localModel;
  protected ClientConnectionHandler client;

  public void start(ConnectionType connectionType, String address, int port) {
    if (connectionType == ConnectionType.TCP) {
      client = new TCPClientConnectionHandler(address, port, localModel);
    } else {
      client = new RMIClientConnectionHandler(address, port, localModel);
    }
    client.connect();
  }
}
