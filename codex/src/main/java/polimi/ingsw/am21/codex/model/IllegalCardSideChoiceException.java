package polimi.ingsw.am21.codex.model;

public class IllegalCardSideChoiceException extends IllegalArgumentException{
 public IllegalCardSideChoiceException(){
   super("You do not currently satisfy this side placing conditions");
 }
}
