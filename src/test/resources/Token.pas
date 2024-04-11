unit Lox;

uses TokenType;

class Token;
private
    TypeOfToken: const TokenType;
    Lexeme:  const String;
    Literal: const Object;
    Line:    const Integer;

public
    /// Creates a new Token.
    ///
    constructor Init(TypeOfToken: TokenType; Lexeme: String; Literal: Object; Line: Integer);

    // Overrides ToString.
    //
    function ToString : String;
    begin
        ToString := TypeOfToken + ' ' + Lexeme + ' ' + Literal;
    end;
end class;

