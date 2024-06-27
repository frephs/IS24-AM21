package polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;

import java.io.Serializable;

/**
 * A generic implementation of a Pair
 */
public class CardPairBase<T> implements Serializable {

  private T first;
  private T second;

  public CardPairBase(T firstCard, T secondCard) {
    this.first = firstCard;
    this.second = secondCard;
  }

  public T getFirst() {
    return this.first;
  }

  public T getSecond() {
    return this.second;
  }

  public T replaceFirst(T firstCard) {
    T toRet = this.first;
    this.first = firstCard;
    return toRet;
  }

  public T replaceSecond(T secondCard) {
    T toRet = this.second;
    this.second = secondCard;
    return toRet;
  }

  public void swap() {
    T temp = this.first;
    this.first = this.second;
    this.second = temp;
  }
}
