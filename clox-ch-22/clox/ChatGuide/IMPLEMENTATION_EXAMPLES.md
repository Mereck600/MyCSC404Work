# Implementation Examples

This file contains complete, ready-to-use code examples for instrumenting the Clox compiler.

---

## Complete `debug.h` Additions

Add these declarations to the existing `debug.h`:

```c
#ifndef clox_debug_h
#define clox_debug_h

#include "chunk.h"
#include "vm.h"

// Existing functions
void disassembleChunk(Chunk* chunk, const char* name);
int disassembleInstruction(Chunk* chunk, int offset);

// NEW INSTRUMENTATION FUNCTIONS
/**
 * Display all constants in the chunk's constant pool.
 * Shows: count, capacity, and all constant values with indices.
 */
void disassembleConstants(Chunk* chunk);

/**
 * Display all global variables and their current values.
 * Shows: count, capacity, and all name-value pairs.
 */
void disassembleGlobals(Table* globals);

/**
 * Display all interned strings in the string pool.
 * Shows: count, capacity, string content, length, and hash.
 */
void disassembleStrings(Table* strings);

/**
 * Display all objects in the heap's linked list.
 * Shows: object type and type-specific details (e.g., string content).
 */
void disassembleObjectsHeap(Obj* objects);

/**
 * Helper: Count objects in the heap.
 * Returns the total number of objects.
 */
int countObjects(Obj* objects);

#endif
```

---

## Complete `debug.c` Implementation

Replace or extend your `debug.c` with these functions:

```c
#include <stdio.h>
#include "debug.h"
#include "value.h"
#include "object.h"

// ... existing disassembleChunk() and disassembleInstruction() ...

/**
 * Helper function: Count objects in the heap.
 */
int countObjects(Obj* objects) {
  int count = 0;
  for (Obj* obj = objects; obj != NULL; obj = obj->next) {
    count++;
  }
  return count;
}

/**
 * Display all constants in the chunk.
 */
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

/**
 * Display all global variables.
 */
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
    // Iterate through all entries (some may be empty/deleted)
    for (int i = 0; i < globals->capacity; i++) {
      Entry* entry = &globals->entries[i];
      // Non-NULL key indicates an active entry
      if (entry->key != NULL) {
        printf("  %-15s => ", entry->key->chars);
        printValue(entry->value);
        printf("\n");
      }
    }
  }
}

/**
 * Display all interned strings.
 */
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

/**
 * Display all objects in the heap.
 */
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

---

## Modified `vm.c` - Updated `interpret()` Function

Replace your existing `interpret()` function with this version:

```c
InterpretResult interpret(const char* source) {
  Chunk chunk;
  initChunk(&chunk);

  // =========================================
  // STEP 1: COMPILE
  // =========================================
  if (!compile(source, &chunk)) {
    freeChunk(&chunk);
    return INTERPRET_COMPILE_ERROR;
  }

  // *** INSTRUMENTATION POINT 1: AFTER COMPILATION ***
  printf("\n");
  printf("╔════════════════════════════════════════╗\n");
  printf("║    INSTRUMENTATION: AFTER COMPILE     ║\n");
  printf("╚════════════════════════════════════════╝\n");

  // Display the bytecode (already existed)
  disassembleChunk(&chunk, "script");

  // Display all data structures
  disassembleConstants(&chunk);
  disassembleGlobals(&vm.globals);
  disassembleStrings(&vm.strings);
  disassembleObjectsHeap(vm.objects);

  // =========================================
  // STEP 2: EXECUTE
  // =========================================
  vm.chunk = &chunk;
  vm.ip = vm.chunk->code;

  InterpretResult result = run();

  // *** INSTRUMENTATION POINT 2: AFTER EXECUTION ***
  printf("\n");
  printf("╔════════════════════════════════════════╗\n");
  printf("║    INSTRUMENTATION: AFTER EXECUTE     ║\n");
  printf("╚════════════════════════════════════════╝\n");

  // Display all data structures again
  disassembleConstants(&chunk);
  disassembleGlobals(&vm.globals);
  disassembleStrings(&vm.strings);
  disassembleObjectsHeap(vm.objects);

  printf("\n");

  // =========================================
  // CLEANUP
  // =========================================
  freeChunk(&chunk);
  return result;
}
```

---

## Minimal Version (Less Verbose)

If you want less output, use this compact version of `interpret()`:

```c
InterpretResult interpret(const char* source) {
  Chunk chunk;
  initChunk(&chunk);

  if (!compile(source, &chunk)) {
    freeChunk(&chunk);
    return INTERPRET_COMPILE_ERROR;
  }

  printf("\n=== AFTER COMPILATION ===\n");
  disassembleConstants(&chunk);
  disassembleGlobals(&vm.globals);
  disassembleStrings(&vm.strings);
  disassembleObjectsHeap(vm.objects);

  vm.chunk = &chunk;
  vm.ip = vm.chunk->code;

  InterpretResult result = run();

  printf("\n=== AFTER EXECUTION ===\n");
  disassembleConstants(&chunk);
  disassembleGlobals(&vm.globals);
  disassembleStrings(&vm.strings);
  disassembleObjectsHeap(vm.objects);

  freeChunk(&chunk);
  return result;
}
```

---

## Test Program

Create a file `test_instrumentation.lox`:

```lox
var x = 42;
var y = "Hello";
var z = 3.14;

print x;
print y;
print z;

var a = x + 100;
print a;

var greeting = y;
print greeting;
```

---

## Expected Output Example

When you run the instrumented compiler with the test program, you should see something like:

```
╔════════════════════════════════════════╗
║    INSTRUMENTATION: AFTER COMPILE     ║
╚════════════════════════════════════════╝

== script ==
0000    DEFINE_GLOBAL    0 (42)
0002    POP
0003    DEFINE_GLOBAL    1 ("Hello")
...

--- Constants Array ---
  Count: 5, Capacity: 8
  [  0] 42
  [  1] Hello
  [  2] 3.14
  [  3] 100
  [  4] H

--- Globals Table ---
  Count: 0, Capacity: 0, Load: 0.0%
  (no globals)

--- Interned Strings Table ---
  Count: 8, Capacity: 16, Load: 50.0%
  [  1 chars] hash=deadbeef  "H"
  [  5 chars] hash=cafebabe  "Hello"
  [  1 chars] hash=12345678  "x"
  [  1 chars] hash=87654321  "y"
  [  1 chars] hash=11111111  "z"
  [  1 chars] hash=22222222  "a"
  [  8 chars] hash=99999999  "greeting"

--- Objects Heap (Linked List) ---
  [ 1] STRING (5 chars): "Hello"
  [ 2] STRING (1 chars): "x"
  [ 3] STRING (1 chars): "y"
  ...
  Total: 8 objects

42
Hello
3.14
142
Hello

╔════════════════════════════════════════╗
║    INSTRUMENTATION: AFTER EXECUTE     ║
╚════════════════════════════════════════╝

--- Constants Array ---
  Count: 5, Capacity: 8
  [  0] 42
  [  1] Hello
  [  2] 3.14
  [  3] 100
  [  4] H

--- Globals Table ---
  Count: 5, Capacity: 8, Load: 62.5%
  x            => 42
  y            => Hello
  z            => 3.14
  a            => 142
  greeting     => Hello

--- Interned Strings Table ---
  Count: 8, Capacity: 16, Load: 50.0%
  ...same as before...

--- Objects Heap (Linked List) ---
  ...same as before...
```

---

## Integration Checklist

- [ ] Add declarations to `debug.h`
- [ ] Implement functions in `debug.c`
- [ ] Modify `interpret()` in `vm.c`
- [ ] Add `#include "debug.h"` to `vm.c` if not already there
- [ ] Rebuild the project
- [ ] Test with a simple Lox program
- [ ] Verify all four data structures display correctly
