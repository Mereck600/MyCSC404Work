# Changes made to source
Non book changes... The code should be up to chapter 9 unless i missed something

### 1/2
- Added colon in Scanner bc I didnt last time
- Parse starts with decleration now 
- Built out the override methods into interpreter
 
 I tested this using a lox file until my code stopped getting errors and the output looks how i expect it to.
 To test use the test.lox file

 ### 3
-  To begin I add break to token type
- Then I add to scanner skeywords
- add to and run genast
- Add new if in the statement method in parser method
- Now i want to adjust while and for methods in parser class where I try to set body = statement and after i decrease the loopDepth 
- Next I go and add in new methods in the interpreter where i create a class that extends runtime exception to jump to the end of the loop 
- Since we desugar for loops into while loops I need to adjsut the while loop to account for the new break 

I tested this using break.lox and it works how I expect I have it throw err if its at top level I am not sure if this is how you want it
I also only did break not continue I am not sure if you wanted us to do continue too 