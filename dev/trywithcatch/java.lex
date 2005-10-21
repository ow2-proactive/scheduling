import java_cup.runtime.Symbol;

%%

%cup
%unicode
%char
%column
%xstate COMMENT_C COMMENT_CPP STRING

%{
  private boolean useNextIdent;

  private Symbol newSymbol(int id) {
    int left = yychar;
    int right = left + yylength();
    return new Symbol(id, left, yycolumn, new Terminal(left, right, yycolumn, yytext()));
  }

  private Symbol newIdent() {
    /*
     * We put '.' in the identifier to include the package name.
     * We only support ASCII characters.
     */
    if (useNextIdent) {
      useNextIdent = false;
      return new Symbol(sym.IDENT, yychar, yychar + yylength(), yytext());
    }

    return null;
  }
%}

%%

<YYINITIAL> {
  "try" { return newSymbol(sym.TRY); }
  "catch" { useNextIdent = true; return newSymbol(sym.CATCH); }
  "finally" { return newSymbol(sym.FINALLY); }
  "{" { return newSymbol(sym.BLOCK_START); }
  "}" { return newSymbol(sym.BLOCK_END); }

  "/*" { yybegin(COMMENT_C); }
  "//" { yybegin(COMMENT_CPP); }
  \" { yybegin(STRING); }

  [a-z.]*"ProActive.tryWithCatch" { return newSymbol(sym.TRY_WITH_CATCH); }
  [a-z.]*"ProActive.endTryWithCatch" { return newSymbol(sym.END_TRY_WITH_CATCH); }
  [a-z.]*"ProActive.removeTryWithCatch" { return newSymbol(sym.END_TRY_WITH_CATCH); }

  [A-Za-z_$][A-Za-z_0-9.]* { Symbol s = newIdent(); if (s != null) return s; }

  .|\n {}
}

<COMMENT_C> {
  "*/" { yybegin(YYINITIAL); }
  .|\n {}
}

<COMMENT_CPP> {
  "\n" { yybegin(YYINITIAL); }
  . {}
}

<STRING> {
  [^\"]|\n { }
  "\\\"" { }
  "\""   { yybegin(YYINITIAL); }
}
