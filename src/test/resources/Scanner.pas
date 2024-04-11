unit Lox;
uses Token, TokenType;

const
    Keywords: Map = ['and':TOKEN_AND, 'class':TOKEN_CLASS, 'else':TOKEN_ELSE, 'false':TOKEN_FALSE, 'for':TOKEN_FOR, 'function':TOKEN_FUN,
        'if':TOKEN_IF, 'nil':TOKEN_NIL, 'or':TOKEN_OR, 'print':TOKEN_PRINT, 'return':TOKEN_RETURN, 'super':TOKEN_SUPER, 
        'this':TOKEN_THIS, 'true':TOKEN_TRUE, 'var':TOKEN_VAR, 'while':TOKEN_WHILE];

/// Scanner.
///
class Scanner;
private
    Source:  String;
    Tokens:  List of Token;
    Start:   Integer = 0;
    Current: Integer = 0;
    Line:    Integer = 0;

public
    /// Creates a new Scanner.
    ///
    constructor Init(Source: String);
    begin
        this.Source := Source;
    end;

    /// Scans Tokens
    ///
    function ScanTokens : List of Token;
    begin
        while not IsAtEnd? do
           Start := Current;
           ScanToken;
        end;
        Tokens += Token(EOF, '', nil, Line);
        ScanTokens := Tokens;   
    end;

private
    // Scan Token
    //
    procedure ScanToken;
    begin
        var C: Char = Advance;
        case C of
            '(' : AddToken(TOKEN_LEFT_PAREN);
            ')' : AddToken(TOKEN_RIGHT_PAREN);
            '{' : AddToken(TOKEN_LEFT_BRACE);
            '}' : AddToken(TOKEN_RIGHT_BRACE);
            ',' : AddToken(TOKEN_COMMA);
            '.' : AddToken(TOKEN_DOT);
            '-' : AddToken(TOKEN_MINUS);
            '+' : AddToken(TOKEN_PLUS);
            ';' : AddToken(TOKEN_SEMICOLON);
            '*' : AddToken(TOKEN_STAR);

            '!' : AddToken(if Match('=' then TOKEN_BANG_EQUAL else TOKEN_BANG));
            '=' : AddToken(if Match('=' then TOKEN_EQUAL_EQUAL else TOKEN_EQUAL));
            '<' : AddToken(if Match('=' then TOKEN_LESS_EQUAL else TOKEN_LESS));
            '>' : AddToken(if Match('=' then TOKEN_GREATER_EQUAL else TOKEN_GREATER));

            '/':
                if (Match('/')) then 
                   while Peek <> CR and not IsAtEnd do Advance;
                else 
                   AddToken(TOKEN_SLASH);
                end if;

            // Skip whitespace
            Space, Lf, Tab : goto next;
            
            Tick: ScanString;
        else
            if IsDigit? C then
                ScanNumber;
            else if IsAlpha? C then
                ScanIdentifier;
            else 
                Lox.Error(Line, 'Unexpected character.');
            end if;
        end case;
    end;

    // Scans an Identifier.
    //
    procedure ScanIdentifier;
    begin
        while IsAlphaNumeric? Peek do Advance;

        const Text = Copy(Source, start, current);
        var TypeOfToken = keywords[Text];

        if (TypeOfToken = nil) then 
            TypeOfToken = TOKEN_IDENTIFIER;
        end;

        AddToken(TypeOfToken);        
    end;

    // Scans a Number
    //
    procedure Number;
    begin
        while IsDigit? Peek do Advance;

        // look for a fractional part.
        if Peek = '.' and IsDigit? PeakNext then Advance;

        while IsDigit? Peek then Advance;

        AddToken(TOKEN_NUMBER, Val(Copy(Source, Start, Current)));
    end;

    // Scans a String
    //
    procedure String;
    begin
        while Peek <> Tick and not IsAtEnd do
            if Peek = Eol then Line++;
            Advance;
        end;

        if IsAtEnd then
        begin
            Lox.Error(Line, 'Unterminated string.');
            exit;
        end;

        Advance; // the closing "

        // Trim the surrounding quotes
        const Value = Copy(Source, Start + 1, Current - 1);

        AddToken(TOKEN_STRING, value);
    end;

    // Does the character match?  
    //
    function Match(expected: Char) : Boolean;
    begin
        if IsAtEnd? then exit False;
        if Source[Current] <> Expected then exit False;

        Current++;
        exit True;
    end;

    // Returns the current character.
    //
    function Peek : Char;
    begin
        if IsAtEnd? then exit #0;
        
        Peek := Source[Current];
    end;

    // Returns the next character.
    //
    function PeekNext : Char;
    begin
       if Current + 1 > Length(Source) then exit #0;
       
       PeekNext := Source[Current + 1];
    end;
    
    // Is the character alphabetic?
    //
    function IsAlpha?(c: Char) : Boolean;
    begin
        IsAlpha? := (c >= 'a' And c <= 'z') Or
            (c >= 'A' And c <= 'Z') Or c == '_';
    end;

    // Is the character alphanumeric?
    //
    function IsAlphaNumeric?(c : Char) : Boolean;
    begin
        IsAlphaNumeric? := IsAlpha(c) Or IsDigit(c);
    end;

    // Is the character a digit?
    //
    IsDigit?(c: Char) : Boolean;
    begin
       IsDigit? := c >= '0' And c <= '9';
    end;

    // Is at the end of the source?
    //
    function IsAtEnd? : Boolean;
    begin
       IsAtEnd := Current >= Length(Source);
    end;

    // Advance the current character.
    //
    function Advance : Char;
    begin
       Advance := Source[Current++];
    end;

    // Adds a Token with no literal. 
    //
    procedure AddToken(TypeOfToken: TokenType);
    begin
       AddToken(TypeOfToken, Nil);
    end;

    // Adds a Token.
    //
    procedure AddToken(TypeOfToken: TokenType; Literal: Object);
    begin
        const Text := Copy(Source, Start, Current);
        Tokens += Token(TypeOfToken, Text, Literal, Line);
    end;
end class;