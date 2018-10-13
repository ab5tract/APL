package APL.types.functions.builtins.fns;

import APL.Indexer;
import APL.errors.RankError;
import APL.types.*;
import APL.types.functions.Builtin;

import java.util.Arrays;

public class FindBuiltin extends Builtin {
  public FindBuiltin() {
    super("⍷");
    valid = 0x010;
  }
  
  public Obj call(Value a, Value w) {
    if (a.rank != w.rank) throw new RankError("argument ranks for ⍷ should be equal ("+a.rank+" ≠ "+w.rank+")", this, w);
    boolean[] res = new boolean[w.ia];
    w: for (int[] inW : new Indexer(Indexer.add(Indexer.sub(w.shape, a.shape), 1), 0)) {
      for (int[] inA : new Indexer(a.shape, 0)) {
        Value vA = a.simpleAt(inA);
        Value vW = w.simpleAt(Indexer.add(inA, inW));
        if (!vA.equals(vW)) continue w;
      }
      res[Indexer.fromShape(w.shape, inW, 0)] = true;
    }
    Value[] arr = new Value[w.ia];
    for (int i = 0; i < w.ia; i++) arr[i] = res[i]? Num.ONE : Num.ZERO;
    return new Arr(arr, w.shape);
  }
}