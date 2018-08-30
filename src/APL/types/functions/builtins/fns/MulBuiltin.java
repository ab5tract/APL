package APL.types.functions.builtins.fns;

import APL.Main;
import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.Builtin;

public class MulBuiltin extends Builtin {
  public MulBuiltin() {
    super("×");
    valid = 0x011;
  }
  
  public Obj call(Value w) {
    return scalar(v -> Main.compareObj(w, Num.ZERO), w);
  }
  public Obj call(Value a0, Value w0) {
    return scalar((a, w) -> ((Num)a).times((Num)w), a0, w0);
  }
  
  public Obj callInvW(Value a, Value w) {
    try {
      return new DivBuiltin().call(w, a);
    } catch (DomainError e) {
      throw new DomainError("", this, e.cause);
    }
  }
}