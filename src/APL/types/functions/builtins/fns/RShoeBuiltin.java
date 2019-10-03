package APL.types.functions.builtins.fns;

import APL.Main;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

import java.util.ArrayList;

public class RShoeBuiltin extends Builtin {
  @Override public String repr() {
    return "⊃";
  }
  
  
  
  public Obj call(Value w) {
    if (!Main.enclosePrimitives && w instanceof Primitive) return w;
    return new Rank0Arr(w);
  }
  
  @Override public Obj call(Value a, Value w) {
    if (w.rank != 1) throw new DomainError("⍵ of ⊃ should be of rank 1");
    if (a.rank != 1) throw new DomainError("⍺ of ⊃ should be of rank 1");
    if (a.ia+1 != w.ia) throw new LengthError("for ⊃, (1+≢⍺) ≡ ≢⍵ is required");
    int[] aa = a.asIntVec();
    ArrayList<Value> parts = new ArrayList<>();
    
    if (w.quickDoubleArr()) {
      double[] vals = w.asDoubleArr();
      ArrayList<Double> cpart = new ArrayList<>();
      for (int i = 0; i < aa.length; i++) {
        int am = aa[i];
        cpart.add(vals[i]);
        if (am > 0) {
          parts.add(new DoubleArr(cpart));
          for (int j = 0; j < am - 1; j++) parts.add(EmptyArr.SHAPE0);
          cpart.clear();
        }
      }
      cpart.add(vals[vals.length - 1]);
      parts.add(new DoubleArr(cpart));
      return Arr.create(parts.toArray(new Value[0]));
    } else {
      Value[] vals = w.values();
      ArrayList<Value> cpart = new ArrayList<>();
      for (int i = 0; i < aa.length; i++) {
        int am = aa[i];
        cpart.add(vals[i]);
        if (am > 0) {
          parts.add(Arr.create(cpart.toArray(new Value[0])));
          for (int j = 0; j < am - 1; j++) parts.add(EmptyArr.SHAPE0);
          cpart.clear();
        }
      }
      cpart.add(vals[vals.length - 1]);
      parts.add(Arr.create(cpart.toArray(new Value[0])));
      return Arr.create(parts.toArray(new Value[0]));
    }
  }
}