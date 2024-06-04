package polimi.ingsw.am21.codex.view.GUI;

import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.view.ViewClient;

public class GuiClient extends ViewClient {

  Gui gui = new Gui();

  public GuiClient() {
    localModel = new LocalModelContainer(gui);
  }

  public void start(ConnectionType connectionType, String address, int port) {
    super.start(connectionType, address, port);
  }

  public static void main(String[] args) {
    GuiClient guiClient = new GuiClient();
    guiClient.start(ConnectionType.TCP, "localhost", 1234);
  }
}
