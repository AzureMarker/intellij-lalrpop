package com.mdrobnak.lalrpop.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.mdrobnak.lalrpop.psi.LpElementTypes.*;

%%

%{
  /**
   * Count brackets in Rust code to know when the code block ends
   */
  int rust_bracket_count = 0;

  /**
   * Count hashs in regex literals, i.e. r##""## has two hashes
   */
  int regex_hash_count = 0;

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
%x PRE_RUST_IMPORT
%x RUST_IMPORT
%x PRE_RUST_CODE
%x RUST_CODE
%x IN_REGEX_LITERAL

WHITE_SPACE=\s+

Comment = {TraditionalComment} | {EndOfLineComment}
TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "//" .* [^\r\n]*

StrLiteral = \" ([^\\\"] | \\. )* \"

Path = (::)? {Id} (:: {Id})* (::\*)?
Id = [a-zA-Z][a-zA-Z0-9_]*
Lifetime = \' {Id} | \' _
ShebangAttribute = #\!\[.*\]

RustImport = [^;]+
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
  "_"                { return UNDERSCORE; }
  "use"              { yybegin(PRE_RUST_IMPORT); return USE; }
  "pub"              { return PUB; }
  "if"               { return IF; }
  "mut"              { return MUT; }
  "dyn"              { return DYN; }
  "extern"           { return EXTERN; }
  "match"            { return MATCH; }
  "else"             { return ELSE; }
  "enum"             { return ENUM; }
  "type"             { return TYPE; }
  "where"            { return WHERE; }
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
  "=>?"              { yybegin(PRE_RUST_CODE); return FALLIBLE_ACTION; }
  "=>"               { yybegin(PRE_RUST_CODE); return USER_ACTION; }
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
  "r" #* \"          { yybegin(IN_REGEX_LITERAL); regex_hash_count = yylength() - 2; }
}

// This small state handles the whitespace between "use" and the Rust import
<PRE_RUST_IMPORT> {
  {WHITE_SPACE}      { return WHITE_SPACE; }
  .                  { yybegin(RUST_IMPORT); yypushback(1); }
}

<RUST_IMPORT> {
  {RustImport}       { yybegin(YYINITIAL); return IMPORT_CODE; }
}

// This small state handles the whitespace between the =>/=>? and the Rust code
<PRE_RUST_CODE> {
  {WHITE_SPACE}      { return WHITE_SPACE; }
  .                  { yybegin(RUST_CODE); yypushback(1); }
}

<RUST_CODE> {
  "(" | "[" | "{"    { rust_bracket_count++; }
  ")" | "]" | "}"    {
          if (rust_bracket_count == 0) {
              // There were no opening brackets in the Rust code, so this
              // character is part of the LALRPOP code.
              yybegin(YYINITIAL);
              yypushback(1);
              return CODE;
          }

          rust_bracket_count--;
      }
  "," | ";"          {
          if (rust_bracket_count == 0) {
              // There were no opening brackets in the Rust code, so this
              // character is part of the LALRPOP code.
              yybegin(YYINITIAL);
              yypushback(1);
              return CODE;
          }
      }

  {RustCode}         { }
}

<IN_REGEX_LITERAL> {
  \" #*              {
          int trailing_hashes = yylength() - 1;
          if (trailing_hashes == regex_hash_count) {
              yybegin(YYINITIAL);
              return REGEX_LITERAL;
          }
      }

  .                  { }
}

. { return BAD_CHARACTER; }
