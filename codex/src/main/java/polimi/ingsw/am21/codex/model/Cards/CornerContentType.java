package polimi.ingsw.am21.codex.model.Cards;

public interface CornerContentType {

  // visitor pattern
  public default void acceptVisitor(CornerContentVisitor visitor){}
  public void acceptVisitor(CornerContentVisitor visitor, int arg);
}
