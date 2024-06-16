package polimi.ingsw.am21.codex.controller.messages;

import java.io.Serializable;

public enum MessageType implements Serializable {
  CONNECT(MessageTypeCategory.CLIENT_ACTION),
  HEART_BEAT(MessageTypeCategory.CLIENT_ACTION),
  NEXT_TURN_ACTION(MessageTypeCategory.CLIENT_ACTION),
  PLACE_CARD(MessageTypeCategory.CLIENT_ACTION),
  CREATE_GAME(MessageTypeCategory.CLIENT_ACTION),
  LOBBY_INFO(MessageTypeCategory.CLIENT_ACTION),
  JOIN_LOBBY(MessageTypeCategory.CLIENT_ACTION),
  LEAVE_LOBBY(MessageTypeCategory.CLIENT_ACTION),
  SELECT_OBJECTIVE(MessageTypeCategory.CLIENT_ACTION),
  SELECT_CARD_SIDE(MessageTypeCategory.CLIENT_ACTION),
  SET_NICKNAME(MessageTypeCategory.CLIENT_ACTION),
  SET_TOKEN_COLOR(MessageTypeCategory.CLIENT_ACTION),

  GET_GAME_STATUS(MessageTypeCategory.CLIENT_REQUEST),
  GET_AVAILABLE_GAME_LOBBIES(MessageTypeCategory.CLIENT_REQUEST),
  GET_OBJECTIVE_CARDS(MessageTypeCategory.CLIENT_REQUEST),
  GET_STARTER_CARD_SIDE(MessageTypeCategory.CLIENT_REQUEST),

  GAME_STATUS(MessageTypeCategory.SERVER_RESPONSE),
  AVAILABLE_GAME_LOBBIES(MessageTypeCategory.SERVER_RESPONSE),
  OBJECTIVE_CARDS(MessageTypeCategory.SERVER_RESPONSE),
  STARTER_CARD_SIDES(MessageTypeCategory.SERVER_RESPONSE),

  INVALID_ACTION(MessageTypeCategory.SERVER_ERROR),
  NOT_A_CLIENT_MESSAGE(MessageTypeCategory.SERVER_ERROR),
  UNKNOWN_MESSAGE_TYPE(MessageTypeCategory.SERVER_ERROR),

  CARD_PLACED(MessageTypeCategory.VIEW_UPDATE),
  GAME_OVER(MessageTypeCategory.VIEW_UPDATE),
  NEXT_TURN_UPDATE(MessageTypeCategory.VIEW_UPDATE),
  PLAYER_JOINED_GAME(MessageTypeCategory.VIEW_UPDATE),
  PLAYER_CHOSE_OBJECTIVE(MessageTypeCategory.VIEW_UPDATE),
  PLAYER_SCORES_UPDATE(MessageTypeCategory.VIEW_UPDATE),
  REMAINING_ROUNDS(MessageTypeCategory.VIEW_UPDATE),
  WINNING_PLAYER(MessageTypeCategory.VIEW_UPDATE),
  GAME_CREATED(MessageTypeCategory.VIEW_UPDATE),
  GAME_DELETED(MessageTypeCategory.VIEW_UPDATE),
  GAME_STARTED(MessageTypeCategory.VIEW_UPDATE),
  PLAYER_JOINED_LOBBY(MessageTypeCategory.VIEW_UPDATE),
  PLAYER_LEFT_LOBBY(MessageTypeCategory.VIEW_UPDATE),
  PLAYER_SET_NICKNAME(MessageTypeCategory.VIEW_UPDATE),
  PLAYER_SET_TOKEN_COLOR(MessageTypeCategory.VIEW_UPDATE),
  SOCKET_ID(MessageTypeCategory.VIEW_UPDATE),
  PLAYER_CONNECTION_CHANGED(MessageTypeCategory.VIEW_UPDATE);

  private enum MessageTypeCategory implements Serializable {
    VIEW_UPDATE,
    SERVER_CONFIRM,
    SERVER_ERROR,
    SERVER_RESPONSE,
    CLIENT_REQUEST,
    CLIENT_ACTION,
  }

  private final MessageTypeCategory messageTypeCategory;

  MessageType(MessageTypeCategory messageTypeCategory) {
    this.messageTypeCategory = messageTypeCategory;
  }

  public Boolean isViewUpdate() {
    return this.messageTypeCategory == MessageTypeCategory.VIEW_UPDATE;
  }

  public Boolean isServerConfirm() {
    return this.messageTypeCategory == MessageTypeCategory.SERVER_CONFIRM;
  }

  public Boolean isServerError() {
    return this.messageTypeCategory == MessageTypeCategory.SERVER_ERROR;
  }

  public Boolean isServerResponse() {
    return this.messageTypeCategory == MessageTypeCategory.SERVER_RESPONSE;
  }

  public Boolean isClientRequest() {
    return this.messageTypeCategory == MessageTypeCategory.CLIENT_REQUEST;
  }

  public Boolean isClientAction() {
    return this.messageTypeCategory == MessageTypeCategory.CLIENT_ACTION;
  }
}
