# Quick Reference & Common Patterns

## TL;DR Summary

You need to add instrumentation to display 4 data structures at 2 points in time:

### The 4 Data Structures
1. **Constants Array** (`chunk->constants`) - All literal values used in code
2. **Globals Table** (`vm.globals`) - All global variables and their values
3. **Strings Table** (`vm.strings`) - All interned strings for efficient comparison
4. **Objects Heap** (`vm.objects`) - Linked list of all dynamically allocated objects

### The 2 Instrumentation Points
1. **After Compilation** - In `interpret()` after `compile()` succeeds
2. **After Execution** - In `interpret()` after `run()` completes

### The 3 Files to Modify
1. **debug.h** - Add function declarations
2. **debug.c** - Implement the display functions
3. **vm.c** - Call display functions in `interpret()`

---

## Code Snippets (Copy & Paste Ready)

### For debug.h - Add Before #endif

```c
// Display functions for compiler instrumentation
void disassembleConstants(Chunk* chunk);
void disassembleGlobals(Table* globals);
void disassembleStrings(Table* strings);
void disassembleObjectsHeap(Obj* objects);
int countObjects(Obj* objects);
```

### For debug.c - Add at End

```c
int countObjects(Obj* objects) {
  int count = 0;
  for (Obj* obj = objects; obj != NULL; obj = obj->next) {
    count++;
  }
  return count;
}

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

### For vm.c - Replace interpret() Function

```c
InterpretResult interpret(const char* source) {
  Chunk chunk;
  initChunk(&chunk);

  if (!compile(source, &chunk)) {
    freeChunk(&chunk);
    return INTERPRET_COMPILE_ERROR;
  }

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

---

## Common Questions & Answers

### Q: Why do we iterate through ALL `capacity` slots in the hash tables?
**A:** Because the hash table uses open addressing (linear probing). When there's a hash collision, entries are placed in subsequent empty slots. So you must check every slot, not just the first `count` slots.

### Q: What does `entry->key == NULL` mean?
**A:** Either the slot was never filled, or it contained an entry that was deleted. Either way, if `key` is NULL, that slot doesn't contain an active global/string.

### Q: Why are strings "interned"?
**A:** To optimize string comparison. Instead of comparing character-by-character every time, identical strings are stored once in the strings table, and the same pointer is reused. So you can compare strings with `==` instead of `strcmp()`.

### Q: Can I modify values in these data structures during instrumentation?
**A:** No, these are just display/diagnostic functions. They read the data but don't modify it.

### Q: What if I see different constants before and after execution?
**A:** That's normal - in Chapter 22, constants don't change because they're compiled into the bytecode. But in later chapters with more features, this might change.

### Q: What if globals are empty after compilation?
**A:** That's expected! Global variables are only added to the globals table when they're assigned during execution with `OP_DEFINE_GLOBAL`.

### Q: How do I know if memory is leaking?
**A:** Compare the object count and heap size between runs. If it keeps growing with the same input, you may have a leak. The objects heap should only grow when new strings are created.

### Q: Can I use this for any Lox program or just simple ones?
**A:** Any program! The instrumentation will work for all Lox code in Chapter 22, whether simple or complex.

---

## Testing Workflow

### Test 1: Simple Constants
```lox
print 42;
print 3.14;
print "hello";
```
**Check**: Constants array should have all 3 values

### Test 2: Global Variables
```lox
var x = 10;
var y = 20;
print x;
print y;
```
**Check**: 
- After compile: Globals should be empty
- After execute: Globals should have x and y

### Test 3: String Interning
```lox
var msg = "hello";
print msg;
print "hello";
```
**Check**: The string "hello" should only appear once in the objects heap (interned)

### Test 4: Mixed Types
```lox
var num = 42;
var str = "test";
var result = num;
print num;
print str;
print result;
```
**Check**: Constants should have 42, "test". Globals should have num, str, result after execution.

---

## Performance Considerations

### The functions are O(n) where n is:
- **disassembleConstants**: Number of constants
- **disassembleGlobals**: Capacity of globals table (not just count!)
- **disassembleStrings**: Capacity of strings table (not just count!)
- **disassembleObjectsHeap**: Number of objects in heap

### This is fine because:
- The tables and heap are typically small during development
- This is only for debugging/instrumentation
- In real use, you'd disable this in release builds

### If it's too slow:
- Reduce the number of print statements
- Use smaller test programs
- Add conditional compilation with `#ifdef DEBUG`

---

## Output Interpretation Guide

### Constants Array Output
```
--- Constants Array ---
  Count: 3, Capacity: 8
  [  0] 42
  [  1] 3.14
  [  2] hello
```
**Means**: 3 constants are defined in the program

### Globals Table Output
```
--- Globals Table ---
  Count: 2, Capacity: 4, Load: 50.0%
  x                => 42
  y                => 3.14
```
**Means**: 2 global variables defined, table is 50% full

### Strings Table Output
```
--- Interned Strings Table ---
  Count: 4, Capacity: 8, Load: 50.0%
  [  1 chars] hash=12345678  "x"
  [  3 chars] hash=abcdefgh  "msg"
  [  5 chars] hash=87654321  "hello"
  [  5 chars] hash=deadbeef  "print"
```
**Means**: 4 unique strings are interned (identifiers and string literals)

### Objects Heap Output
```
--- Objects Heap (Linked List) ---
  [ 1] STRING (5 chars): "hello"
  [ 2] STRING (1 chars): "x"
  [ 3] STRING (1 chars): "y"
  [ 4] STRING (3 chars): "msg"
  Total: 4 objects
```
**Means**: 4 string objects allocated in the heap

---

## Debugging Tips

### To see which strings are identifiers vs literals:
Look at interned strings. Identifiers (variable names) will be short (1-2 chars typically). Literals can be longer.

### To verify string interning works:
Run a program with multiple identical strings and confirm only one appears in the objects heap.

### To track memory growth:
Add a program that creates many variables and note how the table capacity grows (usually doubles when load factor exceeds 75%).

### To understand bytecode:
Compare the bytecode output with the constants array. OP_CONSTANT followed by an index should correspond to that index in the constants array.

### To trace execution:
You can add `DEBUG_TRACE_EXECUTION` conditional compilation flag in `vm.c` to see each instruction being executed, then compare with the state in instrumentation output.

---

## Modifications for Chapter 22+ Features

If you're extending to later chapters with additional object types:

### Add to disassembleObjectsHeap():
```c
case OBJ_STRING: {
  // Already have this
  break;
}
case OBJ_FUNCTION: {
  ObjFunction* func = (ObjFunction*)obj;
  printf("FUNCTION (%s)", func->name->chars);
  break;
}
case OBJ_CLOSURE: {
  ObjClosure* closure = (ObjClosure*)obj;
  printf("CLOSURE (function: %s)", closure->function->name->chars);
  break;
}
// Add more cases as needed
```

### Add new table display functions as needed:
```c
void disassembleClasses(Table* classes) {
  // Display all defined classes
}

void displayUpvalues(ObjClosure* closure) {
  // Display closure upvalues
}
```

---

## Integration with Your Repository

### Save these guides for reference:
- `INSTRUMENTATION_GUIDE.md` - High-level overview
- `IMPLEMENTATION_EXAMPLES.md` - Copy-paste code snippets
- `DATA_STRUCTURES_VISUAL.md` - Visual diagrams and relationships
- `STEP_BY_STEP_IMPLEMENTATION.md` - Detailed step-by-step guide
- `QUICK_REFERENCE.md` - This file

### Version control:
```bash
git add INSTRUMENTATION_GUIDE.md
git add IMPLEMENTATION_EXAMPLES.md
git add DATA_STRUCTURES_VISUAL.md
git add STEP_BY_STEP_IMPLEMENTATION.md
git add QUICK_REFERENCE.md
git commit -m "Add compiler instrumentation guides"
```

---

## Final Checklist

Before you consider this complete:

- [ ] Can compile without errors
- [ ] Can run a simple Lox program
- [ ] See output between "AFTER COMPILE" and "AFTER EXECUTE"
- [ ] Understand what each section shows
- [ ] Can explain why globals are empty after compile but filled after execute
- [ ] Can identify which strings are interned
- [ ] Can count the objects in the heap
- [ ] Have tested with at least 3 different programs

---

## What You've Accomplished

By adding this instrumentation, you now have visibility into:

✅ What constants were compiled
✅ What variables were defined and their values
✅ How strings are interned for efficiency
✅ How objects are allocated and managed
✅ How these structures change during execution
✅ Potential memory issues or inefficiencies
✅ A deep understanding of how interpreters work

This is a powerful debugging and learning tool!
