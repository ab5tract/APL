package APL.types.functions.builtins.fns;

import APL.types.Num;
import APL.types.Obj;
import APL.types.Value;
import APL.types.functions.Builtin;

public class FloorBuiltin extends Builtin {
  public FloorBuiltin() {
    super("⌊");
    valid = 0x11;
  }
  public Obj call(Value w) {
    return scalar(v -> ((Num)w).floor(), w);
  }
  public Obj call(Value a0, Value w0) {
    return scalar((a, w) -> Num.max((Num)a, (Num)w), a0, w0);
  }
}