# Implementation Checklist

Use this document to track your progress through the instrumentation implementation.

---

## Pre-Implementation

### Understanding Phase
- [ ] Understand what data structures need to be displayed
- [ ] Understand where the data structures are located in the code
- [ ] Understand why we display at two points (after compile and after execute)
- [ ] Understand the difference between hash tables and arrays
- [ ] Understand string interning concept
- [ ] Understand the objects heap linked list structure

### Reading Phase
- [ ] Read one introductory document (choose one):
  - [ ] QUICK_REFERENCE.md
  - [ ] COMPLETE_SUMMARY.md
  - [ ] README_INSTRUMENTATION.md
- [ ] Read DATA_STRUCTURES_VISUAL.md
- [ ] Review IMPLEMENTATION_EXAMPLES.md for code snippets
- [ ] Read STEP_BY_STEP_IMPLEMENTATION.md Phase 1-2

### Backup Phase
- [ ] Backup debug.h (`debug.h.backup`)
- [ ] Backup debug.c (`debug.c.backup`)
- [ ] Backup vm.c (`vm.c.backup`)
- [ ] Commit current state to git (if using version control)

---

## Phase 1: Modify debug.h

### Include Section
- [ ] Open `clox-ch-22/clox/debug.h`
- [ ] Verify `#include "chunk.h"` exists
- [ ] Add `#include "vm.h"` if not present

### Add Function Declarations
- [ ] Add declaration for `void disassembleConstants(Chunk* chunk);`
- [ ] Add declaration for `void disassembleGlobals(Table* globals);`
- [ ] Add declaration for `void disassembleStrings(Table* strings);`
- [ ] Add declaration for `void disassembleObjectsHeap(Obj* objects);`
- [ ] Add declaration for `int countObjects(Obj* objects);`
- [ ] Place declarations before `#endif`

### Verify
- [ ] No syntax errors in file
- [ ] All declarations follow existing style
- [ ] File saves successfully

---

## Phase 2: Modify debug.c

### Include Section
- [ ] Open `clox-ch-22/clox/debug.c`
- [ ] Verify `#include <stdio.h>` exists
- [ ] Verify `#include "debug.h"` exists
- [ ] Add `#include "object.h"` if not present
- [ ] Verify `#include "value.h"` exists (should already be there via debug.h)

### Implement countObjects()
- [ ] Add `int countObjects(Obj* objects)` function
- [ ] Initialize count = 0
- [ ] Loop through linked list: `for (Obj* obj = objects; obj != NULL; obj = obj->next)`
- [ ] Increment count in loop
- [ ] Return count
- [ ] Test logic: should handle NULL (return 0) and non-NULL lists

### Implement disassembleConstants()
- [ ] Add function header: `void disassembleConstants(Chunk* chunk)`
- [ ] Print section header: "--- Constants Array ---"
- [ ] Print count and capacity: `chunk->constants.count` and `chunk->constants.capacity`
- [ ] Check if count is 0 (print "(no constants)" if true)
- [ ] Loop from 0 to count-1
- [ ] Print index and value using `printValue()`
- [ ] Test logic: should print each constant with its index

### Implement disassembleGlobals()
- [ ] Add function header: `void disassembleGlobals(Table* globals)`
- [ ] Print section header: "--- Globals Table ---"
- [ ] Print count, capacity, and load percentage
- [ ] Check if count is 0 (print "(no globals)" if true)
- [ ] Loop from 0 to capacity-1 (NOT count-1!)
- [ ] Check `entry->key != NULL` before using entry
- [ ] Print key->chars and value using `printValue()`
- [ ] Test logic: should iterate through ALL capacity slots
- [ ] Verify: NULL keys are skipped

### Implement disassembleStrings()
- [ ] Add function header: `void disassembleStrings(Table* strings)`
- [ ] Print section header: "--- Interned Strings Table ---"
- [ ] Print count, capacity, and load percentage
- [ ] Check if count is 0 (print "(no interned strings)" if true)
- [ ] Loop from 0 to capacity-1 (NOT count-1!)
- [ ] Check `entry->key != NULL` before using entry
- [ ] Cast entry->key to `ObjString*`
- [ ] Print string length, hash, and content
- [ ] Test logic: should show all interned strings with details

### Implement disassembleObjectsHeap()
- [ ] Add function header: `void disassembleObjectsHeap(Obj* objects)`
- [ ] Print section header: "--- Objects Heap (Linked List) ---"
- [ ] Initialize count = 0
- [ ] Loop through linked list: `for (Obj* obj = objects; obj != NULL; obj = obj->next)`
- [ ] Increment count
- [ ] Add switch statement on `obj->type`
- [ ] Handle `case OBJ_STRING:` - cast to ObjString*, print string details
- [ ] Handle `default:` - print "UNKNOWN"
- [ ] Check if count is 0 (print "(no objects)" if true)
- [ ] Print total count at end
- [ ] Test logic: should traverse entire list and classify objects correctly

### Verify
- [ ] No syntax errors in file
- [ ] All functions follow existing code style
- [ ] File saves successfully
- [ ] Functions match declarations in debug.h

---

## Phase 3: Modify vm.c

### Locate Function
- [ ] Open `clox-ch-22/clox/vm.c`
- [ ] Find the `interpret()` function
- [ ] Identify the current structure:
  - [ ] `initChunk(&chunk);`
  - [ ] `compile(source, &chunk);`
  - [ ] `vm.chunk = &chunk;`
  - [ ] `run();`
  - [ ] `freeChunk(&chunk);`

### Add First Instrumentation Point (After Compilation)
- [ ] After `compile()` succeeds, add newline: `printf("\n");`
- [ ] Add header line: `printf("╔════════════════════════════════════════╗\n");`
- [ ] Add title line: `printf("║    INSTRUMENTATION: AFTER COMPILE     ║\n");`
- [ ] Add footer line: `printf("╚════════════════════════════════════════╝\n");`
- [ ] Add call: `disassembleChunk(&chunk, "script");`
- [ ] Add call: `disassembleConstants(&chunk);`
- [ ] Add call: `disassembleGlobals(&vm.globals);`
- [ ] Add call: `disassembleStrings(&vm.strings);`
- [ ] Add call: `disassembleObjectsHeap(vm.objects);`

### Add Second Instrumentation Point (After Execution)
- [ ] After `run()` completes, add newline: `printf("\n");`
- [ ] Add header line: `printf("╔════════════════════════════════════════╗\n");`
- [ ] Add title line: `printf("║    INSTRUMENTATION: AFTER EXECUTE     ║\n");`
- [ ] Add footer line: `printf("╚════════════════════════════════════════╝\n");`
- [ ] Add call: `disassembleConstants(&chunk);`
- [ ] Add call: `disassembleGlobals(&vm.globals);`
- [ ] Add call: `disassembleStrings(&vm.strings);`
- [ ] Add call: `disassembleObjectsHeap(vm.objects);`
- [ ] Add final newline: `printf("\n");`

### Verify
- [ ] No syntax errors in file
- [ ] Function structure is preserved
- [ ] All cleanup code (freeChunk) still executes
- [ ] File saves successfully

---

## Phase 4: Compilation

### Prepare Build
- [ ] Open terminal in `clox-ch-22/clox` directory
- [ ] Verify `Makefile` exists
- [ ] Verify `build/` directory exists

### Clean Build
- [ ] Run: `make clean`
- [ ] Verify no errors
- [ ] Check that build artifacts were removed

### Compile
- [ ] Run: `make`
- [ ] Watch for compilation messages

### Verify No Errors
- [ ] [ ] No undefined reference errors
- [ ] [ ] No syntax errors
- [ ] [ ] No linking errors
- [ ] [ ] `build/clox` executable exists
- [ ] [ ] Check file size is reasonable (not tiny)

### If Errors Occur
- [ ] Run: `make clean`
- [ ] Run: `make` again (full rebuild)
- [ ] Check for typos in function names
- [ ] Verify all includes are correct
- [ ] Use STEP_BY_STEP_IMPLEMENTATION.md Phase 6 for troubleshooting

---

## Phase 5: Testing

### Test 1: Simple Constant
Create `test1.lox`:
```lox
print 42;
```
- [ ] Run: `./build/clox test1.lox`
- [ ] Verify output appears between instrumentation headers
- [ ] Verify "42" is printed to console
- [ ] Verify constants array shows [0] = 42
- [ ] Verify no errors occur

### Test 2: Variables
Create `test2.lox`:
```lox
var x = 10;
var y = 20;
print x;
print y;
```
- [ ] Run: `./build/clox test2.lox`
- [ ] After compile: Globals should be EMPTY
- [ ] After execute: Globals should show x=10, y=20
- [ ] Constants should show 10, 20
- [ ] Verify output appears correctly

### Test 3: Strings
Create `test3.lox`:
```lox
var msg = "hello";
print msg;
```
- [ ] Run: `./build/clox test3.lox`
- [ ] Interned strings should include "msg" and "hello"
- [ ] Objects heap should show string objects
- [ ] Verify "hello" is printed to console

### Test 4: Complex Program
Create `test4.lox`:
```lox
var x = 42;
var y = "hello";
var z = 3.14;
print x;
print y;
print z;
var a = x + 100;
print a;
```
- [ ] Run: `./build/clox test4.lox`
- [ ] Verify all structures display correctly
- [ ] Verify all print statements output
- [ ] Verify globals are populated after execution

### Test 5: Edge Cases
- [ ] Test with empty program (if supported)
- [ ] Test with only print statements (no variables)
- [ ] Test with multiple identical strings

---

## Phase 6: Verification

### Output Verification
After compile:
- [ ] Constants array shows count > 0
- [ ] Globals table shows count = 0 (usually)
- [ ] Strings table shows count > 0
- [ ] Objects heap shows objects

After execute:
- [ ] Constants array unchanged
- [ ] Globals table shows count > 0 (variables populated)
- [ ] Strings table unchanged
- [ ] Objects heap unchanged

### Structure Verification
- [ ] All section headers print correctly
- [ ] All data prints correctly
- [ ] No garbage values or corrupted data
- [ ] Output is readable and organized

### Functionality Verification
- [ ] Program output appears between instrumentation points
- [ ] Bytecode disassembly works (existing feature)
- [ ] All print statements execute
- [ ] No segmentation faults
- [ ] No memory errors

---

## Phase 7: Final Checklist

### Code Quality
- [ ] No compiler warnings
- [ ] No undefined references
- [ ] All functions properly declared
- [ ] All functions properly implemented
- [ ] Proper error handling (NULL checks)
- [ ] Consistent code style

### Documentation
- [ ] Code has comments explaining instrumentation points
- [ ] Function purposes are clear
- [ ] Variable names are meaningful

### Testing Completed
- [ ] At least 5 test programs run successfully
- [ ] Output is correct for all tests
- [ ] No memory leaks detected (run with valgrind if available)
- [ ] Performance is acceptable

### Integration
- [ ] Changes don't break existing functionality
- [ ] Bytecode disassembly still works
- [ ] Print statements still execute
- [ ] Program exits cleanly

---

## Troubleshooting Checklist

If something goes wrong:

### Compilation Errors
- [ ] Check debug.h for missing includes
- [ ] Check all function declarations match implementations
- [ ] Run `make clean && make` for full rebuild
- [ ] Look for typos in function names

### Runtime Errors (Segmentation Fault)
- [ ] Check for NULL pointer dereferences
- [ ] Verify `entry->key != NULL` check in hash table loops
- [ ] Verify `obj != NULL` check in linked list loop
- [ ] Add defensive checks before dereferencing pointers

### Missing/Wrong Output
- [ ] Verify instrumentation functions are being called
- [ ] Check that fflush(stdout) is not needed
- [ ] Verify test file contains valid Lox code
- [ ] Check that binary was rebuilt (not cached)

### Data Looks Wrong
- [ ] Verify you're iterating capacity, not count
- [ ] Verify printf format strings are correct
- [ ] Verify printValue() function exists and works
- [ ] Check table load factor calculations

---

## Success Criteria

You're done when:

- [ ] Code compiles without errors or warnings
- [ ] All 5 test programs run successfully
- [ ] Output shows all 4 data structures
- [ ] Output shows both instrumentation points
- [ ] Globals are empty after compile, populated after execute
- [ ] Constants remain the same before and after
- [ ] No segmentation faults
- [ ] No memory errors
- [ ] Output is readable and organized
- [ ] You can explain what each section shows

---

## Sign-Off

Once everything above is checked:

- [ ] Take a screenshot of successful test output
- [ ] Save test programs and output for documentation
- [ ] Commit changes to version control (if using git)
- [ ] Write brief summary of what you learned
- [ ] Celebrate! 🎉

---

## Next Steps

After successful implementation:

- [ ] Study the output from various programs
- [ ] Understand why data changes between points
- [ ] Prepare for extending to Chapter 23+ features
- [ ] Consider adding memory statistics
- [ ] Consider adding timing information
- [ ] Consider conditional compilation flags

---

## Estimated Timeline

- [ ] Pre-Implementation: 1-2 hours
- [ ] Phase 1 (debug.h): 10 minutes
- [ ] Phase 2 (debug.c): 30 minutes
- [ ] Phase 3 (vm.c): 10 minutes
- [ ] Phase 4 (Compilation): 10 minutes
- [ ] Phase 5 (Testing): 30 minutes
- [ ] Phase 6 (Verification): 15 minutes
- [ ] Phase 7 (Final Checks): 15 minutes

**Total: 2.5 - 3.5 hours**

---

Good luck! Check off each item as you complete it. 📋✅
