package APL.types.functions.builtins.dops;

import APL.types.Fun;
import APL.types.Obj;
import APL.types.Value;
import APL.types.functions.*;

public class DualBuiltin extends Dop {
  @Override public String repr() {
    return "⍢";
  }
  
  
  
  public Obj call(Obj aa, Obj ww, Value w, DerivedDop derv) {
    Fun under = (Fun) ww;
    return under.callInv( (Value) ((Fun)aa).call((Value) under.call(w)));
  }
  
  public Obj call(Obj aa, Obj ww, Value a, Value w, DerivedDop derv) {
    Fun under = (Fun) ww;
    return under.callInv( (Value) ((Fun)aa).call((Value) under.call(a), (Value) under.call(w)));
  }
}