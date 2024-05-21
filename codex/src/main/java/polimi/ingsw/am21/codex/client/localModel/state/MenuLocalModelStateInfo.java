package polimi.ingsw.am21.codex.client.localModel.state;

public class MenuLocalModelStateInfo implements LocalModelStateInfo {

  @Override
  public LocalModelState getType() {
    return LocalModelState.LOBBY;
  }
}
