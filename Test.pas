uses Token;
uses TokenType;
uses Scanner;

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