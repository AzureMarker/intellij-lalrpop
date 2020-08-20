package com.mdrobnak.lalrpop.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.mdrobnak.lalrpop.psi.LalrpopTypes.*;

%%

%{
  /**
   * Count brackets in Rust code to know when the code block ends
   */
  int rust_bracket_count = 0;

  public LalrpopLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class LalrpopLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%x RUST_CODE

WHITE_SPACE=\s+

Comment = {TraditionalComment} | {EndOfLineComment}
TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "//" .* [^\r\n]*

StrLiteral = \" [^\"]* \"
CharLiteral = \' [^\']* \'
RegexLiteral = r \" ([^\"]* [^\\])? \"

Path = (::)? {Id} (:: {Id})* (::\*)?
Id = [a-zA-Z][a-zA-Z0-9_]*
Lifetime = \' {Id}
ShebangAttribute = #\!\[.*\]

// Eat everything except for brackets and punctuation, which are matched by the
// other RUST_CODE rules.
RustCode = [^(\[{)\]},;]+

%%
<YYINITIAL> {
  {WHITE_SPACE}      { return WHITE_SPACE; }
  {Comment}          { return COMMENT; }

  "grammar"          { return GRAMMAR; }
  ":"                { return COLON; }
  ";"                { return SEMICOLON; }
  ","                { return COMMA; }
  ".."               { return DOTDOT; }
  "_"                { return UNDERSCORE; }
  "use"              { return USE; }
  "pub"              { return PUB; }
  "if"               { return IF; }
  "mut"              { return MUT; }
  "dyn"              { return DYN; }
  "extern"           { return EXTERN; }
  "match"            { return MATCH; }
  "else"             { return ELSE; }
  "enum"             { return ENUM; }
  "type"             { return TYPE; }
  "=="               { return EQUALS_EQUALS; }
  "!="               { return NOT_EQUALS; }
  "="                { return EQUALS; }
  "~~"               { return MATCH_OP; }
  "!~"               { return NOT_MATCH_OP; }
  "#"                { return POUND; }
  "("                { return LPAREN; }
  ")"                { return RPAREN; }
  "["                { return LBRACKET; }
  "]"                { return RBRACKET; }
  "{"                { return LBRACE; }
  "}"                { return RBRACE; }
  "<"                { return LESSTHAN; }
  ">"                { return GREATERTHAN; }
  "->"               { return RSINGLEARROW; }
  "=>@L"             { return LOOKAHEAD_ACTION; }
  "=>@R"             { return LOOKBEHIND_ACTION; }
  "=>?"              { yybegin(RUST_CODE); return FALLIBLE_ACTION; }
  "=>"               { yybegin(RUST_CODE); return USER_ACTION; }
  "@L"               { return LOOKAHEAD; }
  "@R"               { return LOOKBEHIND; }
  "+"                { return PLUS; }
  "*"                { return MULTIPLY; }
  "?"                { return QUESTION; }
  "!"                { return NOT; }
  "&"                { return AND; }

  {Id}               { return ID; }
  {Path}             { return PATH_REF; }
  {Lifetime}         { return LIFETIME; }
  {ShebangAttribute} { return SHEBANG_ATTRIBUTE; }
  {StrLiteral}       { return STR_LITERAL; }
  {CharLiteral}      { return CHAR_LITERAL; }
  {RegexLiteral}     { return REGEX_LITERAL; }
}

<RUST_CODE> {
  "(" | "[" | "{"        { rust_bracket_count++; continue; }
  ")" | "]" | "}"        {
          if (rust_bracket_count == 0) {
              // There were no opening brackets in the Rust code, so this
              // character is part of the LALRPOP code.
              yybegin(YYINITIAL);
              yypushback(1);
              continue;
          }

          rust_bracket_count--;
          if (rust_bracket_count == 0) {
              // At the end of the Rust code, so switch state
              yybegin(YYINITIAL);
              return CODE;
          }

          continue;
      }
  "," | ";"             {
          if (rust_bracket_count == 0) {
              // There were no opening brackets in the Rust code, so this
              // character is part of the LALRPOP code.
              yybegin(YYINITIAL);
              yypushback(1);
              return CODE;
          }
          continue;
      }

  {RustCode}             { continue; }
}

[^] { return BAD_CHARACTER; }
