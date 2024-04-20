type
    // Token Type
   TokenType = 
       // Single-character tokens.
       (TOKEN_LEFT_PAREN, TOKEN_RIGHT_PAREN, TOKEN_LEFT_BRACE, TOKEN_RIGHT_BRACE,
        TOKEN_COMMA, TOKEN_DOT, TOKEN_MINUS, TOKEN_PLUS, TOKEN_SEMICOLON, TOKEN_SLASH, TOKEN_STAR,

        // One or two character tokens
        TOKEN_BANG, TOKEN_BANG_EQUAL, TOKEN_EQUAL, TOKEN_EQUAL_EQUAL,
        TOKEN_GREATER, TOKEN_GREATER_EQUAL, TOKEN_LESS, TOKEN_LESS_EQUAL,

        // literals
        TOKEN_IDENTIFIER, TOKEN_STRING, TOKEN_NUMBER,

        // Keywords
        TOKEN_AND, TOKEN_CLASS, TOKEN_ELSE, TOKEN_FALSE, TOKEN_FUN, TOKEN_FOR, TOKEN_IF, TOKEN_NIL, TOKEN_OR,
        TOKEN_PRINT, TOKEN_RETURN, TOKEN_SUPER, TOKEN_THIS, TOKEN_TRUE, TOKEN_VAR, TOKEN_WHILE,

        EOF);

var  Keywords := [
    'and':TOKEN_AND, 'class':TOKEN_CLASS, 'else':TOKEN_ELSE, 'false':TOKEN_FALSE, 'for':TOKEN_FOR, 'function':TOKEN_FUN,
    'if':TOKEN_IF, 'nil':TOKEN_NIL, 'or':TOKEN_OR, 'print':TOKEN_PRINT, 'return':TOKEN_RETURN, 'super':TOKEN_SUPER, 
    'this':TOKEN_THIS, 'true':TOKEN_TRUE, 'var':TOKEN_VAR, 'while':TOKEN_WHILE];

/// Token
///
class Token;
begin
    constructor Init(TypeOfToken, Lexeme, Literal, LineNumber);
    begin
        this.TypeOfToken := TypeOfToken;
        this.Lexeme := Lexeme;
        this.LineNumber := LineNumber;
    end
end 

/// Scanner
///
class Scanner;
begin
    /// Creates a new Scanner
    ///
    constructor Init(Source);
    begin
        this.Source  := Source;
        this.Current := 0;
        this.Start   := 0;
        this.Line    := 1;

        this.Tokens := List();
    end

    /// Scans Tokens
    ///
    function ScanTokens();
    begin
        while not IsAtEnd() do
        begin
           Start := Current;
           ScanToken();
        end

        Tokens.Add(Token(EOF, '', nil, Line));
        exit Tokens;
    end

    // Scan Token
    //
    procedure ScanToken();
    begin
        var C := Advance();   
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

            '!' : if Match('=') then AddToken(TOKEN_BANG_EQUAL); else AddToken(TOKEN_BANG);
            '=' : if Match('=') then AddToken(TOKEN_EQUAL_EQUAL); else AddToken(TOKEN_EQUAL);
            '<' : if Match('=') then AddToken(TOKEN_LESS_EQUAL); else AddToken(TOKEN_LESS);
            '>' : if Match('=') then AddToken(TOKEN_GREATER_EQUAL); else AddToken(TOKEN_GREATER);

            // Comments
            '/':
                if Match('/') then 
                   while Peek() <> #13 and not IsAtEnd() do Advance();
                else 
                   AddToken(TOKEN_SLASH);

            // Skip whitespace
            #9, #10, ' ' : Exit;

            #13 : 
                begin
                   Line := Line + 1;
                   Exit;
                end
            '"' : ScanString();
        else
            if IsAlpha(C) then
                ScanIdentifier();
            else if IsDigit(C) then
                ScanNumber();
            else 
                WriteLn('Unrecognized character: ' + C);
        end  
    end

    // Scans an Identifier.
    //
    procedure ScanIdentifier();
    begin
        while IsAlphaNumeric(Peek()) do Advance();

        var Text := Copy(Source, Start, Current);   
        var TypeOfToken := TOKEN_IDENTIFIER;

        if Keywords.Contains(Text) then
        begin
            TypeOfToken := Keywords.Get(Text); 
        end

        AddToken(TypeOfToken);        
    end

    // Scans a Number
    //
    procedure ScanNumber();
    begin
        while IsDigit(Peek()) do Advance();

        // look for a fractional part.
        if Peek() = '.' and IsDigit(PeekNext()) then Advance();

        while IsDigit(Peek()) do Advance();

        AddToken2(TOKEN_NUMBER, Copy(Source, Start, Current));
    end

    // Scans a String
    //
    procedure ScanString();
    begin
        while Peek() <> '"' and not IsAtEnd() do
        begin
            if Peek() = #13 then Line := Line + 1;
            Advance();
        end

        if IsAtEnd() then
        begin
            WriteLn('Unterminated string.');
            Exit;
        end

        Advance();

        // Trim the surrounding quotes
        var Value := Copy(Source, Start + 1, Current - 1);

        AddToken2(TOKEN_STRING, Value);
    end

    // Does the character match?  
    //
    function Match(Expected);
    begin
        if IsAtEnd() then exit False;
        if Source[Current] <> Expected then exit False;

        Current := Current + 1;
        exit True;
    end

    // Returns the current character.
    //
    function Peek();
    begin
        if IsAtEnd() then exit #0;
        
        exit Source[Current];
    end

    // Returns the next character.
    //
    function PeekNext();
    begin
       if Current + 1 > Length(Source) then exit #0;
       
       exit Source[Current + 1];
    end

    // Adds a Token.
    //
    procedure AddToken(TypeOfToken);
    begin
        AddToken2(TypeOfToken, nil);
    end

    // Adds a Token.
    //
    procedure AddToken2(TypeOfToken, Literal);
    begin
        var Text := Copy(Source, Start, Current);
        Tokens.Add(Token(TypeOfToken, Text, Literal, Line));
    end

    // Is at the end?
    //
    function IsAtEnd();
    begin
        exit Current >= Length(Source);
    end

    // Advance the current character.
    //
    function Advance();
    begin
        var ReturnValue := Source[Current];
        Current := Current + 1;

        exit ReturnValue;
    end

    // Is the character alphabetic?
    //
    function IsAlpha(c);
    begin
        exit (c >= 'a' and c <= 'z') or
             (c >= 'A' and c <= 'Z') or 
             (c = '_');
    end

    // Is the character alphanumeric?
    //
    function IsAlphaNumeric(c);
    begin
        exit IsAlpha(c) or IsDigit(c);
    end

    // Is the character a digit?
    //
    IsDigit(c);
    begin
       exit c >= '0' and c <= '9';
    end
end

/// Main
///
begin
    var TheScanner := Scanner('var Test = "ABC";  // comments');
    var Tokens := TheScanner.ScanTokens();

    for var I := 0; I < Tokens.Length; I := I + 1 do
    begin
        var TheToken := Tokens[I];
        var TheType := TheToken.TypeOfToken;
   
        if TheType = TOKEN_IDENTIFIER or TheType = TOKEN_NUMBER or TheType = TOKEN_STRING then
            WriteLn(TheType + ': ' + TheToken.Lexeme);
        else 
           WriteLn(TheType);
    end
end