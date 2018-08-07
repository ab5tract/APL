package APL;

import java.util.*;
import APL.types.*;
import APL.errors.*;
import APL.types.functions.*;
import APL.types.functions.builtins.*;
import APL.types.functions.builtins.fns.*;
import APL.types.functions.builtins.mops.*;
import APL.types.functions.builtins.dops.*;
import APL.types.functions.userDefined.UserDefined;

import static APL.Main.*;

class Exec {
  private Scope sc;
  private ArrayList<Token> ps;

  Exec(Token ln, Scope sc) {
    assert (ln.type == TType.expr || ln.type == TType.usr);
    ps = ln.tokens;
    this.sc = sc;
  }

  private void printlvl(Object... args) {
    if (!Main.debug) return;
    for (int i = 0; i < Main.printlvl; i++) print("  ");
    Main.printdbg(args);
  }
  private Stack remaining;
  Obj exec() {
    if (sc.alphaDefined && ps.size() >= 2 && "⍺".equals(ps.get(0).repr) && ps.get(1).type == TType.set) {
      if (Main.debug) printlvl("skipping cuz it's ⍺←");
      return null;
    }
    var o = new Stack<Token>();
    remaining = o;
    o.addAll(ps);
    StringBuilder repr = new StringBuilder();
    for (Token t : ps) repr.append(t.toRepr()).append(" ");
    if (Main.debug) printlvl("NEW:");
    Main.printlvl++;
    if (Main.debug) printlvl("code:", repr);
    if (Main.debug) printlvl();
    var done = new LinkedList<Obj>();
    ArrayList<Value> arr = null;
    while (o.size() > 0) {
      Token t = o.pop();
      Obj c = valueOf(t);
      // if (Main.debug) printlvl("token", t.toRepr(), "-> object", c);
      if (c.isObj()) {
        if (arr != null) {
          arr.add((Value) c);
        } else {
          arr = new ArrayList<>();
          arr.add((Value) c);
        }
      } else {
        if (arr != null) {
          if (arr.size() == 1) done.add(arr.get(0));
          else done.add(new Arr(arr, true));
          update(done, false);
          arr = null;
        }
        if (t.type == TType.op) done.add(c);
        else if (t.type == TType.set) done.add(c);
        else if (t.type == TType.name) done.add(c);
        else if (t.type == TType.expr) done.add(c);
        else if (t.type == TType.usr) done.add(c);
        else throw new Error("unknown type: " + t.type + " (no idea if this should be a thing)");
        update(done, false);
      }
    }
    if (arr != null) {
      if (arr.size() == 1) done.add(arr.get(0));
      else done.add(new Arr(arr, true));
    }
    update(done, true);
    Main.printlvl--;
    if (Main.debug) printlvl("END:", rev(done));
    if (done.size() != 1) throw new SyntaxError("couldn't join everything up into a single expression");
    return done.get(0);
  }

  private String rev(LinkedList l) {
    StringBuilder res = new StringBuilder("[");
    ListIterator li = l.listIterator(l.size());
    while (li.hasPrevious()) {
      if (res.length() != 1) res.append(", ");
      res.append(li.previous());
    }
    return res + "]";
  }

  private void update(LinkedList<Obj> done, boolean end) {
    boolean run = true;
    if (done.size() == 1 && done.get(0) == null) return;
    while (run) {
      if (done.size() >= 3 && "∘".equals(done.getLast().repr) && ".".equals(done.get(done.size() - 2).repr)) {
        done.removeLast();
        done.removeLast();
        var fn = (Fun)done.removeLast();
        done.addLast(new TableBuiltin().derive(fn));
      }
      if (Main.debug) printlvl("UPDATE", rev(done));
      run = false;
      if (is(done, "D!|NFN", end, false)) {
        if (Main.debug) printlvl("NFN");
        if (Main.debug) printlvl("before:", rev(done));
        var w = (Value) done.poll();
        var f = (Fun) done.poll();
        var a = (Value) done.poll();
        assert f != null;
        var res = f.call(a, w);
        if (res == null && remaining.size() > 0) throw new SyntaxError("trying to use result of function which returned null");
        done.addFirst(res);
        if (res == null) return;
        run = true;
      }
      if (is(done, "[FM←]|FN", end, false)) {
        if (Main.debug) printlvl("FN");
        if (Main.debug) printlvl("before:", rev(done));
        var w = (Value) done.poll();
        var f = (Fun) done.poll();
        assert f != null;
        var res = f.call(w);
        if (res == null && remaining.size() > 0) throw new SyntaxError("trying to use result of function which returned null");
        done.addFirst(res);
        if (res == null) return;
        run = true;
      }
      if (is(done, "D!|N←.,D!|F←F,D!|D←D,D!|M←M", end, false)) { // "D!|.←." to allow changing type
        if (Main.debug) printlvl("N←.");
        if (Main.debug) printlvl("before:", rev(done));
        var w = done.poll();
        var s = (SetBuiltin) done.poll(); // ←
        var a = done.poll();
        assert s != null;
        done.addFirst(s.call(a, w));
        run = true;
      }
      if (is(done, "D!|NF←N", end, false)) {
        if (Main.debug) printlvl("NF←.");
        if (Main.debug) printlvl("before:", rev(done));
        var w = (Value) done.poll();
        var s = (SetBuiltin) done.poll(); // ←
        var f = (Fun) done.poll(); // ←
        Value a = (Value) done.poll();
        assert s != null;
        done.addFirst(s.call(f, a, w));
        run = true;
      }
      if (is(done, "!D|[FN]M", end, true)) {
        if (Main.debug) printlvl("FM");
        if (Main.debug) printlvl("before:", rev(done));
        var o = (Mop) done.remove(lastPtr);
        var f = (Fun) done.remove(lastPtr);
        done.add(lastPtr, o.derive(f));
        run = true;
      }
      if (is(done, "[FN]D[FN]", end, true)) {
        if (Main.debug) printlvl("FDF");
        if (Main.debug) printlvl("before:", rev(done));
        var aa = done.removeLast();
        var o = (Dop) done.removeLast();
        var ww = done.removeLast();
        done.addLast(o.derive(aa, ww));
        run = true;
      }
      if (run) {
        if (Main.debug) printlvl("after:", rev(done));
      }
    }
  }

  private int lastPtr = -1;

  private boolean is(LinkedList<Obj> l, String pt, boolean everythingDone, boolean reverse) {
    if (pt.contains(",")) {
      for (String s : pt.split(",")) {
        if (is(l, s, everythingDone, reverse)) return true;
      }
    }
    if (everythingDone && is(l, pt, false, reverse)) return true;
    if (reverse && everythingDone && pt.contains("|")) {
      return is(l, pt.split("\\|")[1], false, true);
    }
    int len = pt.length();
    int ptr = reverse ? l.size() - 1 : 0;
    int ptrinc = reverse ? -1 : 1;
    boolean pass = false;
    for (int i = reverse ? 0 : len - 1; (reverse ? i < len : i >= 0); i -= ptrinc) {
      char p = pt.charAt(i);
      String any;
      boolean inv = false;
      if (p == '|') {
        pass = everythingDone;
        i -= ptrinc;
        p = pt.charAt(i);
      }
      if (ptr >= l.size() || ptr < 0) return pass;
      if (p == '!') {
        inv = true;
        i -= ptrinc;
        p = pt.charAt(i);
      }
      if (p == ']') { // regular
        int si = i;
        while (pt.charAt(i) != '[') i--;
        any = pt.substring(i + 1, si);
        i--;
      } else if (p == '[') { // reverse
        int si = i;
        while (pt.charAt(i) != ']') i++;
        any = pt.substring(si + 1, i);
      } else any = String.valueOf(p);
      if (p == '.') {
        ptr += ptrinc;
        continue;
      }
      Obj v = l.get(ptr);
      lastPtr = ptr;
      char type;
      switch (v.type) {
        case array:
          type = 'N';
          break;
        case fn:
        case bfn:
          type = 'F';
          break;
        case set:
          type = '←';
          break;
        case mop:
        case bmop:
          type = 'M';
          break;
        case dop:
        case bdop:
          type = 'D';
          break;
        case var:
          type = 'V';
          break;
        default:
          throw up;
      }
      //printdbg(any, type, any.contains(str(type)));
      if ((!any.contains(String.valueOf(type))) ^ inv) return false;
      ptr += ptrinc;
    }
    return true;
  }

  private Obj valueOf(Token t) {
    Obj o = valueOfRaw(t);
    o.token = t;
    return o;
  }
  
  private Obj valueOfRaw(Token t) {
    if (t.type == TType.op) {
      switch (t.repr.charAt(0)) {

        // fns
        case '+': return new PlusBuiltin();
        case '⊂': return new LShoeBuiltin();
        case '⊃': return new RShoeBuiltin(sc);
        case '-': return new MinusBuiltin();
        case '÷': return new DivBuiltin();
        case '×': return new MulBuiltin();
        case '⍳': return new IotaBuiltin(sc);
        case '⍴': return new RhoBuiltin();
        case ',': return new CatBuiltin();
        case '≢': return new TallyBuiltin();
        case '≡': return new DepthBuiltin();
        case '⊢': return new RTackBuiltin();
        case '↑': return new UpArrowBuiltin(sc);
        case '↓': return new DownArrowBuiltin(sc);
        
        // comparisons
        case '<': return new LTBuiltin();
        case '≤': return new LEBuiltin();
        case '=': return new EQBuiltin();
        case '≥': return new GEBuiltin();
        case '>': return new GTBuiltin();
        case '≠': return new NEBuiltin();

        // mops
        case '/': return new ReduceBuiltin();
        case '¨': return new EachBuiltin();
        case '⍨': return new SelfieBuiltin();
        case '⌾': return new TableBuiltin();

        // dops
        case '∘': return new JotBuiltin();
        case '.': return new DotBuiltin();
        case '⍣': return new RepeatBuiltin();


        case '⍬': return new Arr();
        case '⎕': return new Logger();
        case '⍺': return sc.get("⍺");
        case '⍵': return sc.get("⍵");
        case '⍶': return sc.get("⍶");
        case '⍹': return sc.get("⍹");
        default: throw new NYIError("no built-in " + t.repr + " defined in exec");
      }
    } else if (t.type == TType.number) return new Num(t.repr);
    else if (t.type == TType.str) return t.repr.length() == 1 ? new Char(t.repr) : Main.toAPL(t.repr, t);
    else if (t.type == TType.set) return new SetBuiltin();
    else if (t.type == TType.name) return sc.getVar(t.repr);
    else if (t.type == TType.expr) return Main.execTok(t, sc);
    else if (t.type == TType.usr) return UserDefined.of(t, sc);
    else throw new NYIError("Unknown type: " + t.type);
  }
}