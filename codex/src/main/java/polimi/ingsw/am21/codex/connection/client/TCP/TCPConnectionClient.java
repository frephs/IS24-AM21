package polimi.ingsw.am21.codex.connection.client.TCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import polimi.ingsw.am21.codex.view.Notification;
import polimi.ingsw.am21.codex.view.View;

public class TCPConnectionClient {

  private final String ip;
  private final int port;

  private final Socket socket;
  private boolean connected = false;

  private final ObjectInputStream inputStream;
  private final ObjectOutputStream outputStream;

  private final ExecutorService threadManager = Executors.newCachedThreadPool();
  private final View view;

  public TCPConnectionClient(View view, String ip, int port) {
    this.view = view;
    this.ip = ip;
    this.port = port;
    try {
      this.socket = new Socket(ip, port);
      connected = true;
      this.view.postNotication(Notification.CONNECTION_ENSTABLISHED);
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
