# Step-by-Step Implementation Guide

Follow these steps to add instrumentation to your Clox compiler.

---

## Phase 1: Preparation

### Step 1.1: Backup Your Code
```bash
# Before making changes, create a backup
cp debug.h debug.h.backup
cp debug.c debug.c.backup
cp vm.c vm.c.backup
```

### Step 1.2: Review Current Code
- Open `debug.h` and note the current function declarations
- Open `debug.c` and find where `disassembleChunk()` is implemented
- Open `vm.c` and locate the `interpret()` function

---

## Phase 2: Add Function Declarations to debug.h

### Step 2.1: Add Necessary Includes
At the top of `debug.h`, ensure you have:
```c
#ifndef clox_debug_h
#define clox_debug_h

#include "chunk.h"
#include "vm.h"          // ADD THIS if not present
```

### Step 2.2: Add Function Declarations
At the end of `debug.h` (before `#endif`), add:

```c
// Instrumentation functions
void disassembleConstants(Chunk* chunk);
void disassembleGlobals(Table* globals);
void disassembleStrings(Table* strings);
void disassembleObjectsHeap(Obj* objects);
int countObjects(Obj* objects);
```

**Complete file should look like:**
```c
#ifndef clox_debug_h
#define clox_debug_h

#include "chunk.h"
#include "vm.h"

void disassembleChunk(Chunk* chunk, const char* name);
int disassembleInstruction(Chunk* chunk, int offset);

void disassembleConstants(Chunk* chunk);
void disassembleGlobals(Table* globals);
void disassembleStrings(Table* strings);
void disassembleObjectsHeap(Obj* objects);
int countObjects(Obj* objects);

#endif
```

---

## Phase 3: Implement Functions in debug.c

### Step 3.1: Add Necessary Includes
At the top of `debug.c`, ensure you have:
```c
#include <stdio.h>
#include "debug.h"
#include "value.h"
#include "object.h"    // ADD THIS if not present
```

### Step 3.2: Implement countObjects()
Add this helper function first (before the display functions):

```c
int countObjects(Obj* objects) {
  int count = 0;
  for (Obj* obj = objects; obj != NULL; obj = obj->next) {
    count++;
  }
  return count;
}
```

### Step 3.3: Implement disassembleConstants()
Add this function:

```c
void disassembleConstants(Chunk* chunk) {
  printf("\n--- Constants Array ---\n");
  printf("  Count: %d, Capacity: %d\n",
         chunk->constants.count,
         chunk->constants.capacity);

  if (chunk->constants.count == 0) {
    printf("  (no constants)\n");
  } else {
    for (int i = 0; i < chunk->constants.count; i++) {
      printf("  [%3d] ", i);
      printValue(chunk->constants.values[i]);
      printf("\n");
    }
  }
}
```

**To verify**: 
- Does it print the correct count and capacity? ✓
- Does it iterate from 0 to count-1? ✓
- Does it use printValue() for each constant? ✓

### Step 3.4: Implement disassembleGlobals()
Add this function:

```c
void disassembleGlobals(Table* globals) {
  printf("\n--- Globals Table ---\n");
  printf("  Count: %d, Capacity: %d, Load: %.1f%%\n",
         globals->count,
         globals->capacity,
         globals->capacity > 0 ?
           (100.0 * globals->count / globals->capacity) : 0.0);

  if (globals->count == 0) {
    printf("  (no globals)\n");
  } else {
    for (int i = 0; i < globals->capacity; i++) {
      Entry* entry = &globals->entries[i];
      if (entry->key != NULL) {
        printf("  %-15s => ", entry->key->chars);
        printValue(entry->value);
        printf("\n");
      }
    }
  }
}
```

**To verify**:
- Does it print count and capacity? ✓
- Does it iterate through ALL capacity slots (not just count)? ✓
- Does it check if `entry->key != NULL` before using the entry? ✓
- Does it print key->chars and the value? ✓

### Step 3.5: Implement disassembleStrings()
Add this function:

```c
void disassembleStrings(Table* strings) {
  printf("\n--- Interned Strings Table ---\n");
  printf("  Count: %d, Capacity: %d, Load: %.1f%%\n",
         strings->count,
         strings->capacity,
         strings->capacity > 0 ?
           (100.0 * strings->count / strings->capacity) : 0.0);

  if (strings->count == 0) {
    printf("  (no interned strings)\n");
  } else {
    for (int i = 0; i < strings->capacity; i++) {
      Entry* entry = &strings->entries[i];
      if (entry->key != NULL) {
        ObjString* str = entry->key;
        printf("  [%-3d chars] hash=%08x  \"%s\"\n",
               str->length,
               str->hash,
               str->chars);
      }
    }
  }
}
```

**To verify**:
- Similar structure to disassembleGlobals()? ✓
- Casts entry->key to ObjString*? ✓
- Prints string length, hash, and content? ✓

### Step 3.6: Implement disassembleObjectsHeap()
Add this function:

```c
void disassembleObjectsHeap(Obj* objects) {
  printf("\n--- Objects Heap (Linked List) ---\n");

  int count = 0;
  for (Obj* obj = objects; obj != NULL; obj = obj->next) {
    count++;

    printf("  [%2d] ", count);

    switch (obj->type) {
      case OBJ_STRING: {
        ObjString* string = (ObjString*)obj;
        printf("STRING (%d chars): \"%s\"",
               string->length,
               string->chars);
        break;
      }
      default: {
        printf("UNKNOWN (type=%d)", obj->type);
        break;
      }
    }

    printf("\n");
  }

  if (count == 0) {
    printf("  (no objects)\n");
  } else {
    printf("  Total: %d objects\n", count);
  }
}
```

**To verify**:
- Does it traverse the linked list correctly? ✓
- Does it increment count for each object? ✓
- Does it check obj->type and cast appropriately? ✓
- Does it handle OBJ_STRING correctly? ✓
- Does it print a total count? ✓

---

## Phase 4: Modify the interpret() Function in vm.c

### Step 4.1: Locate the Current Function
Find this in `vm.c`:
```c
InterpretResult interpret(const char* source) {
  Chunk chunk;
  initChunk(&chunk);

  if (!compile(source, &chunk)) {
    freeChunk(&chunk);
    return INTERPRET_COMPILE_ERROR;
  }

  vm.chunk = &chunk;
  vm.ip = vm.chunk->code;

  InterpretResult result = run();

  freeChunk(&chunk);
  return result;
}
```

### Step 4.2: Add Instrumentation After Compilation
Replace the function with:

```c
InterpretResult interpret(const char* source) {
  Chunk chunk;
  initChunk(&chunk);

  if (!compile(source, &chunk)) {
    freeChunk(&chunk);
    return INTERPRET_COMPILE_ERROR;
  }

  // *** INSTRUMENTATION POINT 1: AFTER COMPILATION ***
  printf("\n");
  printf("╔════════════════════════════════════════╗\n");
  printf("║    INSTRUMENTATION: AFTER COMPILE     ║\n");
  printf("╚════════════════════════════════════════╝\n");

  disassembleChunk(&chunk, "script");
  disassembleConstants(&chunk);
  disassembleGlobals(&vm.globals);
  disassembleStrings(&vm.strings);
  disassembleObjectsHeap(vm.objects);

  vm.chunk = &chunk;
  vm.ip = vm.chunk->code;

  InterpretResult result = run();

  // *** INSTRUMENTATION POINT 2: AFTER EXECUTION ***
  printf("\n");
  printf("╔════════════════════════════════════════╗\n");
  printf("║    INSTRUMENTATION: AFTER EXECUTE     ║\n");
  printf("╚════════════════════════════════════════╝\n");

  disassembleConstants(&chunk);
  disassembleGlobals(&vm.globals);
  disassembleStrings(&vm.strings);
  disassembleObjectsHeap(vm.objects);

  printf("\n");

  freeChunk(&chunk);
  return result;
}
```

**Checklist**:
- ✓ Displays after compile() completes
- ✓ Displays before run() starts
- ✓ Displays after run() completes
- ✓ Includes all 4 data structures
- ✓ Uses clear labels for readability

---

## Phase 5: Compilation and Testing

### Step 5.1: Rebuild the Project
```bash
cd clox-ch-22/clox
make clean
make
```

### Step 5.2: Verify No Compilation Errors
Expected output:
```
cc -Wall -Wextra -std=c99 -O0 -g ... (various files) ...
```

No errors should appear. If you see errors:
1. Check that all functions are declared in `debug.h`
2. Check that all functions are implemented in `debug.c`
3. Verify `#include` statements are correct
4. Check for typos in function names

### Step 5.3: Create a Test File
Create `test.lox`:
```lox
var x = 42;
var y = "hello";
print x;
print y;
```

### Step 5.4: Run the Test
```bash
./build/clox test.lox
```

### Step 5.5: Verify Output
You should see:

```
═══════════════════════════════════════════
║    INSTRUMENTATION: AFTER COMPILE     ║
╚════════════════════════════════════════╝

== script ==
0000    DEFINE_GLOBAL    0 (42)
...

--- Constants Array ---
  Count: 2, Capacity: 8
  [  0] 42
  [  1] hello

--- Globals Table ---
  Count: 0, Capacity: 0, Load: 0.0%
  (no globals)

--- Interned Strings Table ---
  Count: 4, Capacity: 8, Load: 50.0%
  ...

--- Objects Heap (Linked List) ---
  Total: ... objects

42
hello

╔════════════════════════════════════════╗
║    INSTRUMENTATION: AFTER EXECUTE     ║
╚════════════════════════════════════════╝

--- Constants Array ---
  Count: 2, Capacity: 8
  [  0] 42
  [  1] hello

--- Globals Table ---
  Count: 2, Capacity: 4, Load: 50.0%
  x                => 42
  y                => hello

--- Interned Strings Table ---
  (same as before)

--- Objects Heap (Linked List) ---
  (same as before)
```

---

## Phase 6: Troubleshooting

### Issue: "undefined reference to disassembleConstants"
**Solution**: Make sure you declared the function in `debug.h` and implemented it in `debug.c`

### Issue: "field 'capacity' is unavailable for type 'ValueArray'"
**Solution**: Check `value.h` - you may be using an older version. Ensure `ValueArray` has a `capacity` field.

### Issue: "globals table showing garbage values"
**Solution**: Make sure to check `entry->key != NULL` before accessing the entry

### Issue: "not printing all globals"
**Solution**: Remember to iterate through ALL `capacity` slots, not just `count` slots

### Issue: "segmentation fault when printing objects"
**Solution**: Make sure you're checking `obj != NULL` before dereferencing in the for loop

### Issue: Can't see any output
**Solution**: 
1. Make sure stdout is flushed: add `fflush(stdout);` after printf calls
2. Verify the binary was rebuilt: run `make clean && make`
3. Check if the file exists: `ls -la test.lox`

---

## Phase 7: Optional Enhancements

### Enhancement 1: Add a "Before" Snapshot
Before compilation:
```c
printf("\n╔════════════════════════════════════════╗\n");
printf("║    BEFORE INTERPRETATION              ║\n");
printf("╚════════════════════════════════════════╝\n");
disassembleConstants(&chunk);  // Empty
disassembleGlobals(&vm.globals);
disassembleStrings(&vm.strings);
disassembleObjectsHeap(vm.objects);
```

### Enhancement 2: Add Memory Statistics
```c
size_t heap_bytes = countObjects(vm.objects) * sizeof(ObjString);
size_t globals_bytes = vm.globals.capacity * sizeof(Entry);
size_t strings_bytes = vm.strings.capacity * sizeof(Entry);
printf("Memory usage: %zu bytes\n", heap_bytes + globals_bytes + strings_bytes);
```

### Enhancement 3: Add Timing Information
```c
#include <time.h>

clock_t compile_start = clock();
// ... compile ...
clock_t compile_end = clock();

double compile_time = (double)(compile_end - compile_start) / CLOCKS_PER_SEC;
printf("Compilation took: %.6f seconds\n", compile_time);
```

### Enhancement 4: Conditional Instrumentation with Defines
Add to `common.h`:
```c
#define DEBUG_INSTRUMENTATION 1
```

Then wrap display calls:
```c
#ifdef DEBUG_INSTRUMENTATION
  disassembleConstants(&chunk);
  disassembleGlobals(&vm.globals);
  // ...
#endif
```

---

## Validation Checklist

- [ ] All function declarations added to `debug.h`
- [ ] All functions implemented in `debug.c`
- [ ] `interpret()` function modified in `vm.c`
- [ ] Project compiles without errors
- [ ] Test file runs and produces output
- [ ] "After Compile" section shows constants
- [ ] "After Compile" section shows globals (should be empty)
- [ ] "After Compile" section shows interned strings
- [ ] "After Compile" section shows objects heap
- [ ] "After Execute" section shows updated globals
- [ ] Bytecode disassembly still works
- [ ] Program output (print statements) appears between the two instrumentation sections

---

## Next Steps

Once instrumentation is working:

1. **Experiment with Different Programs**
   - Try programs with more variables
   - Try programs with loops and conditionals
   - Try programs with different data types

2. **Analyze the Output**
   - Notice which strings are interned
   - Observe how globals change during execution
   - See how the objects heap grows

3. **Add More Instrumentation**
   - Show stack contents in run()
   - Show instruction execution details
   - Track memory allocations

4. **Create Visualizations**
   - Save output to a file for analysis
   - Create graphs of memory usage
   - Compare before/after states
