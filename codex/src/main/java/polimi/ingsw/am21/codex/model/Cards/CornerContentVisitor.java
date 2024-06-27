package polimi.ingsw.am21.codex.model.Cards;

public interface CornerContentVisitor {
  /**
   * Updates the count of the given object in the object map
   * @param object The object whose count will be updated
   * @param diff The count difference that will be applied to the object map (usually either 1 or -1)
   */
  void visit(ObjectType object, int diff);

  /**
   * Updates the count of the given resource in the resource map
   * @param resource The resource whose count will be updated
   * @param diff The count difference that will be applied to the resource map (usually either 1 or -1)
   */
  void visit(ResourceType resource, int diff);
}
