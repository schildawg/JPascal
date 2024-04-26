function WriteLn(S);
begin
   print S;
end

function DoEnum;
type
   Color = (Red, Orange, Yellow, Green, Blue, Indigo, Violet);
   Months = (January, February, March, April, May, June, July, August, September, October, November, December);

begin
   //for C in Red to Violet do
   //begin
   //   WriteLn('The color is: ' + C);
   //end

   var TheColor := Red;
   if TheColor = Red then
   begin
      WriteLn(Violet);
   end
end

function DoArray;
begin
   var Test := Array(3);

   WriteLn(Test.length);
   Test.set(1, 'new');
   WriteLn(array.get(1));
end

//function Ord(Enum);
//begin
//  Exit Enum.Value;
//end

//class Color;
//begin
//  Init(Name, Value);
//  begin
//    this.Name := Name;
//    this.Value := Value;
//  end
//end

//var Red := Color('Red', 1);
//var Green := Color('Green', 2);
//var Blue := Color('Blue', 3);

//Print Red.Name;
//Print Ord(Red);

DoEnum();