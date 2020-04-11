package APL.types.arrs;

import APL.errors.DomainError;
import APL.types.*;

public class Rank0Arr extends Arr {
  private final static int[] SHAPE = new int[0];
  public final Value item;
  
  public Rank0Arr(Value item) {
    super(SHAPE, 1, 0);
    this.item = item;
  }
  
  @Override
  public int[] asIntArr() {
    return new int[]{item.asInt()};
  }
  
  @Override
  public int asInt() {
    throw new DomainError("Using a shape 1 array as integer", this);
  }
  
  @Override
  public Value get(int i) {
    return item;
  }
  
  @Override
  public String asString() {
    if (item instanceof Char) return String.valueOf(((Char) item).chr);
    throw new DomainError("array with non-char element used as string");
  }
  
  public Value prototype() {
    return item.prototype();
  }
  
  public Value safePrototype() {
    return item.safePrototype();
  }
  
  @Override
  public Value ofShape(int[] sh) {
    assert ia == Arr.prod(sh);
    return new SingleItemArr(item, sh);
  }
  
  @Override
  public Value[] valuesCopy() {
    return new Value[]{item};
  }
}