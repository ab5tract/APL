package APL.tokenizer.types;

import java.util.List;

public class BracketTok extends TokArr<LineTok> {
  
  public BracketTok(String line, int spos, int epos, List<LineTok> tokens) {
    super(line, spos, tokens);
    end(epos);
  }
  
  @Override public String toRepr() {
    StringBuilder s = new StringBuilder("[");
    boolean tail = false;
    for (var v : tokens) {
      if (tail) s.append(" ⋄ ");
      s.append(v.toRepr());
      tail = true;
    }
    s.append("]");
    return s.toString();
  }
}