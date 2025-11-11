# Instrumentation Guide: Displaying Compiler and VM Data Structures

## Overview
This guide explains how to add instrumentation to the Chapter 22 Clox compiler to display internal data structures at key points in the compilation and execution pipeline.

---

## Data Structures to Instrument

### 1. **Constants Array** (chunk.h)
Located in the `Chunk` structure:
```c
typedef struct {
  int count;
  int capacity;
  uint8_t* code;
  int* lines;
  ValueArray constants;  // <-- TARGET
} Chunk;
```

**Access**: `chunk->constants` (a `ValueArray`)
- `constants.count` - number of constants
- `constants.capacity` - allocated space
- `constants.values[i]` - each Value in the array

---

### 2. **Globals Table** (vm.h)
Located in the `VM` structure:
```c
typedef struct {
  Chunk* chunk;
  uint8_t* ip;
  Value stack[STACK_MAX];
  Value* stackTop;
  Table globals;      // <-- TARGET (21.2)
  Table strings;      // interned strings (20.5)
  Obj* objects;
} VM;
```

**Access**: `vm.globals` (a `Table`)
- `globals.count` - number of global variables
- `globals.capacity` - hash table capacity
- `globals.entries[i]` - array of key-value pairs

---

### 3. **Interned Strings Table** (vm.h)
Also in the `VM` structure:
```c
Table strings;  // <-- TARGET
```

**Access**: `vm.strings` (a `Table`)
- Same structure as globals table
- Stores all interned ObjString objects for efficient string comparison

---

### 4. **Objects Heap Linked List** (vm.h)
Located in the `VM` structure:
```c
typedef struct {
  // ...
  Obj* objects;  // <-- TARGET (Chapter 19.5)
} VM;
```

**Access**: `vm.objects` (linked list head)
- Traverse via `obj->next` until NULL
- Check `obj->type` (currently only `OBJ_STRING`)
- Cast to appropriate type based on type field

---

## Implementation Strategy

### Step 1: Create Instrumentation Functions in `debug.h`

Add function declarations:
```c
// Display all constants in a chunk
void disassembleConstants(Chunk* chunk);

// Display all globals
void disassembleGlobals(Table* globals);

// Display all interned strings
void disassembleStrings(Table* strings);

// Display entire objects heap
void disassembleObjectsHeap(Obj* objects);

// Display all VM state
void disassembleVMState();
```

### Step 2: Implement Functions in `debug.c`

#### Function 1: Display Constants Array
```c
void disassembleConstants(Chunk* chunk) {
  printf("=== Constants Array ===\n");
  printf("Count: %d, Capacity: %d\n", 
         chunk->constants.count, 
         chunk->constants.capacity);
  
  for (int i = 0; i < chunk->constants.count; i++) {
    printf("[%d] ", i);
    printValue(chunk->constants.values[i]);
    printf("\n");
  }
  
  if (chunk->constants.count == 0) {
    printf("(empty)\n");
  }
  printf("\n");
}
```

**What it shows**:
- Number and capacity of constants
- Each constant value (numbers, strings, nil, booleans)
- Index for reference

#### Function 2: Display Globals Table
```c
void disassembleGlobals(Table* globals) {
  printf("=== Globals Table ===\n");
  printf("Count: %d, Capacity: %d\n", 
         globals->count, 
         globals->capacity);
  
  if (globals->count == 0) {
    printf("(empty)\n");
  } else {
    for (int i = 0; i < globals->capacity; i++) {
      Entry* entry = &globals->entries[i];
      if (entry->key != NULL) {
        printf("  \"%s\" => ", entry->key->chars);
        printValue(entry->value);
        printf("\n");
      }
    }
  }
  printf("\n");
}
```

**What it shows**:
- Number of global variables defined
- Hash table load factor info
- Each global variable name and its current value

#### Function 3: Display Interned Strings Table
```c
void disassembleStrings(Table* strings) {
  printf("=== Interned Strings Table ===\n");
  printf("Count: %d, Capacity: %d\n", 
         strings->count, 
         strings->capacity);
  
  if (strings->count == 0) {
    printf("(empty)\n");
  } else {
    for (int i = 0; i < strings->capacity; i++) {
      Entry* entry = &strings->entries[i];
      if (entry->key != NULL) {
        printf("  [%d chars] \"%s\" (hash: %u)\n",
               entry->key->length,
               entry->key->chars,
               entry->key->hash);
      }
    }
  }
  printf("\n");
}
```

**What it shows**:
- How many strings are interned
- String content and length
- Hash values (useful for understanding hash distribution)

#### Function 4: Display Objects Heap
```c
void disassembleObjectsHeap(Obj* objects) {
  printf("=== Objects Heap (Linked List) ===\n");
  
  int count = 0;
  for (Obj* obj = objects; obj != NULL; obj = obj->next) {
    count++;
    printf("  [%d] Object (type: ", count);
    
    switch (obj->type) {
      case OBJ_STRING: {
        ObjString* string = (ObjString*)obj;
        printf("STRING) \"%s\" (%d chars)\n",
               string->chars,
               string->length);
        break;
      }
      default:
        printf("UNKNOWN)\n");
        break;
    }
  }
  
  if (count == 0) {
    printf("(empty)\n");
  } else {
    printf("Total objects: %d\n", count);
  }
  printf("\n");
}
```

**What it shows**:
- All dynamically allocated objects
- Object type
- Object-specific details (e.g., string content)
- Total count

#### Function 5: Master Display Function
```c
void disassembleVMState() {
  printf("\n========== VM STATE SNAPSHOT ==========\n");
  disassembleConstants(vm.chunk);
  disassembleGlobals(&vm.globals);
  disassembleStrings(&vm.strings);
  disassembleObjectsHeap(vm.objects);
  printf("========= END VM STATE SNAPSHOT =========\n\n");
}
```

---

### Step 3: Modify `interpret()` Function in `vm.c`

Replace the `interpret()` function to call instrumentation at key points:

```c
InterpretResult interpret(const char* source) {
  Chunk chunk;
  initChunk(&chunk);

  if (!compile(source, &chunk)) {
    freeChunk(&chunk);
    return INTERPRET_COMPILE_ERROR;
  }

  // *** INSTRUMENTATION POINT 1: After Compilation ***
  printf("\n");
  printf("=======================================\n");
  printf("AFTER COMPILATION\n");
  printf("=======================================\n");
  disassembleChunk(&chunk, "script");  // Already exists
  disassembleConstants(&chunk);
  disassembleGlobals(&vm.globals);
  disassembleStrings(&vm.strings);
  disassembleObjectsHeap(vm.objects);
  printf("\n");

  vm.chunk = &chunk;
  vm.ip = vm.chunk->code;

  InterpretResult result = run();

  // *** INSTRUMENTATION POINT 2: After Execution ***
  printf("\n");
  printf("=======================================\n");
  printf("AFTER EXECUTION\n");
  printf("=======================================\n");
  disassembleChunk(&chunk, "script");  // Already exists
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

## Key Points to Remember

### 1. **Table Structure** (`table.h`)
```c
typedef struct {
  ObjString* key;
  Value value;
} Entry;

typedef struct {
  int count;              // Actual number of entries
  int capacity;           // Size of entries array
  Entry* entries;         // Hash table array
} Table;
```
- Tables use open addressing (linear probing) for hash collisions
- You must iterate through the entire `capacity` to find all entries
- `key == NULL` means that slot is empty

### 2. **Value Macros** (`value.h`)
Use these for type checking and casting:
```c
IS_NUMBER(value)      // Check if value is a number
IS_OBJ(value)         // Check if value is an object
IS_STRING(value)      // Check if value is a string
IS_NIL(value)         // Check if value is nil
IS_BOOL(value)        // Check if value is a boolean

AS_NUMBER(value)      // Get double value
AS_OBJ(value)         // Get Obj* pointer
AS_STRING(value)      // Get ObjString* pointer
AS_BOOL(value)        // Get bool value
```

### 3. **String Objects** (`object.h`)
```c
struct ObjString {
  Obj obj;              // Base object
  int length;           // String length
  char* chars;          // String content
  uint32_t hash;        // Hash value
};
```

### 4. **Object Linked List** (`object.h`)
```c
struct Obj {
  ObjType type;         // Type discriminator
  struct Obj* next;     // Next in linked list
};
```
- All allocated objects are part of a singly-linked list
- Head is at `vm.objects`
- Used for garbage collection (mark-and-sweep)

---

## Alternative Display Formats

### Option A: Compact Display
```c
// Single-line summaries
printf("Constants: %d/%d | Globals: %d/%d | Strings: %d/%d | Objects: %d\n",
       chunk->constants.count, chunk->constants.capacity,
       vm.globals.count, vm.globals.capacity,
       vm.strings.count, vm.strings.capacity,
       countObjects(vm.objects));
```

### Option B: JSON-style Output
```c
printf("{\n");
printf("  \"constants\": %d,\n", chunk->constants.count);
printf("  \"globals\": %d,\n", vm.globals.count);
printf("  \"strings\": %d,\n", vm.strings.count);
printf("  \"objects\": %d\n", countObjects(vm.objects));
printf("}\n");
```

### Option C: CSV Format
```c
printf("point,constants,globals,strings,objects\n");
printf("after_compile,%d,%d,%d,%d\n",
       chunk->constants.count, vm.globals.count,
       vm.strings.count, countObjects(vm.objects));
```

---

## Testing

Test with a simple Lox program like:

```lox
var x = 5;
var y = "hello";
var z = x + 10;
print x;
print y;
print z;
```

Expected output after compilation:
- Constants: `5`, `10`, `"hello"`
- Globals: `x`, `y`, `z`
- Strings: `"hello"` (and any strings used in identifiers)
- Objects: Various string objects

Expected output after execution:
- Same structures (unless variables were garbage collected)
- Globals show final values

---

## Files to Modify

1. **`debug.h`** - Add function declarations
2. **`debug.c`** - Implement the instrumentation functions
3. **`vm.c`** - Modify `interpret()` to call instrumentation functions

---

## Summary

By following this guide, you'll be able to see:
- ✅ All compiled constants
- ✅ All defined global variables and their values
- ✅ All interned strings in the string pool
- ✅ All objects in the heap
- ✅ How these structures change from compilation to execution

This instrumentation is invaluable for:
- Understanding the compilation process
- Debugging VM behavior
- Learning how interpreters work
- Identifying memory leaks or inefficiencies
