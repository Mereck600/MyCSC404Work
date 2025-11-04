# Extension 7

## part 1
First i begin my modifying the scanner.h by adding two new single cahrater tokens
Then i added the new tokens ": and ?" in scanner.c at the scanTokens section 
Then i changed the precedence level by adding a line to compiler.c I added the precedence higer than the or I am not fully sure if this is right i was a little confused where to put it. 
Then i added colon and ? to the unholy parse rules table we have. 
Lastly i had to add the new method in the bottom of scanner and I had to fix a few errors but this overall wasn't bad. 

Part 2:
Ok so my first thought was to use something like the stringify method from 121 because it takes something and turns it into a string. So I used the general idea from that and built out the 
string concatination methods. The logic for this is in the VM.c.
This part was hard. I originally tired to just use the concatate method but i realized this didnt work how i needed it to. I then added two helper functions valueToString and concatObjString.
What these tow helpers do is value to string takes any lox value and converts it to a string and then concate makes two strings into one. I did have copiolt help me implement the value to sting method. I was confused by the snprintf method it uses so looking it up [Resources](https://www.geeksforgeeks.org/c/snprintf-c-library/) i found that it essentially formats a series of characters and values in an array buffer. "str: It is a pointer to the buffer.
size: It is the maximum number of bytes (characters) that will be written to the buffer.format: C string that contains a format string that follows the same specifications as format in printf.
(...): The optional ( …) arguments are just the string formats like (“%d”, myint) as seen in printf." and returns "The number of characters that would have been written on the buffer, if 'n' had been sufficiently large. If an encoding error occurs, a negative number is returned."
TLDR: valueToString create a new heapString by using copyString method. Then concate is simple, create a new char buffer and copy the contents and retrun. 
