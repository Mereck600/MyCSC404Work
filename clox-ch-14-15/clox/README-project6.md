# For part 1:
I had to declare new op_codes in the chuck.h enum. Then I went over to the vm.c and added new cases for each instruction.
For most of these, they were simple but I did have to look up how to  eaisly get min values in C using the Math.h 
Here is an example of this at line 93 of vm.c
```
    case OP_SWAP: {
        Value top = pop();
        Value second = pop();
        push(top);
        push(second);
        break;
      }
```
Once I have defined the op_code in chunck.h, and implemented the opcodes in vm.c added to the switch in debug.c, I used chatGPT to generate a test file Prompt:"Generate a C test file for these opcodes". Run make clean, make, and then make test. After looking over the test cases and the output I think I implemented the op_codes correctly. I also had chatGPT to modify my makefile to account for this new test file. 

# For part 2:



## TODO:
    Make changes to vm.c on book code
    Make changes to main.c on other code
    only make changes to vm.c on part 2 
    Add snipets of code that i changed here
