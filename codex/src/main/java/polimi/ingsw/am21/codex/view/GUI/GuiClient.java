package polimi.ingsw.am21.codex.view.GUI;

import java.util.Objects;
import javafx.application.Application;
import polimi.ingsw.am21.codex.client.ClientGameEventHandler;
import polimi.ingsw.am21.codex.client.localModel.LocalModelContainer;
import polimi.ingsw.am21.codex.connection.ConnectionType;
import polimi.ingsw.am21.codex.view.TUI.utils.Cli;
import polimi.ingsw.am21.codex.view.ViewClient;

public class GuiClient extends ViewClient {

  private Gui gui;

  public GuiClient(LocalModelContainer localModel) {
    super(new Gui(localModel));
    gui = (Gui) view;
  }

  @Override
  public void start(ConnectionType connectionType, String address, int port) {
    new Thread(() -> Application.launch(gui.getClass())).start();

    try {
      while (!gui.isInitialized()) {
        Thread.sleep(100);
      }
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    super.start(connectionType, address, port);
  }

  public static void main(String[] args) {
    if (args.length != 3) throw new IllegalArgumentException(
      "Usage: GuiClient <connection-type> <address> <port>"
    );

    GuiClient guiClient = new GuiClient(new LocalModelContainer());

    // TODO add defaults from config file
    guiClient.start(
      Objects.equals(args[0], "--TCP")
        ? ConnectionType.TCP
        : ConnectionType.RMI,
      args[1] != null ? args[1] : "localhost",
      args[2] != null ? Integer.parseInt(args[2]) : 12345
    );
  }
}
