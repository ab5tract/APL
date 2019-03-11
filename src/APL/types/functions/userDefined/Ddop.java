package APL.types.functions.userDefined;

import APL.*;
import APL.tokenizer.Token;
import APL.tokenizer.types.DfnTok;
import APL.types.*;
import APL.types.functions.*;

import static APL.Main.*;


public class Ddop extends Dop {
  private final DfnTok token;
  
  @Override public String repr() {
    return token.toRepr();
  }
  
  Ddop(DfnTok t, Scope sc) {
    super(sc);
    token = t;
  }
  public Obj call(Obj aa, Obj ww, Value w, DerivedDop derv) {
    printdbg("ddop call", w);
    Scope nsc = new Scope(sc);
    nsc.set("⍶", aa);
    nsc.set("⍹", ww);
    nsc.set("⍺", new Variable(nsc, "⍺"));
    nsc.set("⍵", w);
    nsc.set("∇", derv);
    var res = Main.execLines(token, nsc);
    if (res instanceof VarArr) return ((VarArr)res).materialize();
    if (res instanceof Settable) return ((Settable)res).get();
    return res;
  }
  public Obj call(Obj aa, Obj ww, Value a, Value w, DerivedDop derv) {
    printdbg("ddop call", a, w);
    Scope nsc = new Scope(sc);
    nsc.set("⍶", aa);
    nsc.set("⍹", ww);
    nsc.set("⍺", a);
    nsc.set("⍵", w);
    nsc.set("∇", derv);
    nsc.alphaDefined = true;
    var res = Main.execLines(token, nsc);
    if (res instanceof VarArr) return ((VarArr)res).materialize();
    if (res instanceof Settable) return ((Settable)res).get();
    return res;
  }
  public String toString() {
    return token.toRepr();
  }
}
