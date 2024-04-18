package polimi.ingsw.am21.codex.model.Cards;

public interface CornerContentVisitor {


  default void visit(ResourceType resource){}
  default void visit(ObjectType object){}

  // version with int argument
  void visit(ObjectType object, int arg);
  void visit(ResourceType resource, int arg);

}
