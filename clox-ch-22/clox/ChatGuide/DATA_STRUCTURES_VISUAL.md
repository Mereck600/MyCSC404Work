# Data Structure Relationships & Visual Guide

## VM Memory Layout

```
┌─────────────────────────────────────────────────┐
│              VM Data Structures                 │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌──────────────────────────────────┐          │
│  │  CHUNK (Compilation Unit)        │          │
│  ├──────────────────────────────────┤          │
│  │ code[]      - Bytecode           │          │
│  │ lines[]     - Line numbers       │          │
│  │ constants[] - ValueArray         │          │◄── TARGET 1
│  │             count: 3             │          │    Constants
│  │             capacity: 8          │          │
│  │             values: [42, "Hi", 3.14]        │
│  └──────────────────────────────────┘          │
│                                                 │
│  ┌──────────────────────────────────┐          │
│  │  GLOBALS TABLE (Hash Table)      │          │◄── TARGET 2
│  ├──────────────────────────────────┤          │    Globals
│  │ count: 3                         │          │
│  │ capacity: 8                      │          │
│  │ entries[]: [                     │          │
│  │   { "x" -> 42 }                  │          │
│  │   { "y" -> "Hello" }             │          │
│  │   { "z" -> 3.14 }                │          │
│  │   ...                            │          │
│  │ ]                                │          │
│  └──────────────────────────────────┘          │
│                                                 │
│  ┌──────────────────────────────────┐          │
│  │  STRINGS TABLE (Hash Table)      │          │◄── TARGET 3
│  ├──────────────────────────────────┤          │    Interned
│  │ count: 8                         │          │    Strings
│  │ capacity: 16                     │          │
│  │ entries[]: [                     │          │
│  │   { "Hello" -> ObjString* }      │          │
│  │   { "x" -> ObjString* }          │          │
│  │   { "y" -> ObjString* }          │          │
│  │   { "z" -> ObjString* }          │          │
│  │   ...                            │          │
│  │ ]                                │          │
│  └──────────────────────────────────┘          │
│                                                 │
│  ┌──────────────────────────────────┐          │
│  │  OBJECTS HEAP (Linked List)      │          │◄── TARGET 4
│  ├──────────────────────────────────┤          │    Objects
│  │ objects: ────────┐               │          │
│  └────────│─────────┘               │          │
│           │                         │          │
│           ▼                         │          │
│  ┌──────────────────────┐           │          │
│  │ ObjString            │           │          │
│  │ type: OBJ_STRING     │           │          │
│  │ next: ───────────┐   │           │          │
│  │ chars: "Hello"   │   │           │          │
│  │ length: 5        │   │           │          │
│  │ hash: 0xdeadbeef │   │           │          │
│  └────────┬─────────┘   │           │          │
│           │             │           │          │
│           ▼             │           │          │
│  ┌──────────────────────┐           │          │
│  │ ObjString            │           │          │
│  │ type: OBJ_STRING     │           │          │
│  │ next: ───────────┐   │           │          │
│  │ chars: "x"       │   │           │          │
│  │ length: 1        │   │           │          │
│  │ hash: 0xcafebabe │   │           │          │
│  └────────┬─────────┘   │           │          │
│           │             │           │          │
│           ...           │           │          │
│           │             │           │          │
│           ▼             │           │          │
│  ┌──────────────────────┐           │          │
│  │ ObjString            │           │          │
│  │ type: OBJ_STRING     │           │          │
│  │ next: NULL           │           │          │
│  │ chars: "world"       │           │          │
│  │ length: 5            │           │          │
│  │ hash: 0x12345678     │           │          │
│  └──────────────────────┘           │          │
│                                     │          │
└─────────────────────────────────────┴──────────┘
```

---

## Hash Table Structure (Globals and Strings)

```
Table Structure:
┌──────────────┐
│ count: 3     │  ← Number of entries currently stored
│ capacity: 8  │  ← Size of entries array (always power of 2)
│ entries: ────┼──→ Entry entries[8];
└──────────────┘

Entry Array (Open Addressing with Linear Probing):
┌─────────────────────────────────┐
│ [0] { key: NULL, value: ??? }   │  ← Empty slot
│ [1] { key: "x", value: 42 }     │  ← Active entry
│ [2] { key: NULL, value: ??? }   │  ← Empty slot
│ [3] { key: "z", value: 3.14 }   │  ← Active entry
│ [4] { key: "y", value: "Hi" }   │  ← Active entry (collision?)
│ [5] { key: NULL, value: ??? }   │  ← Tombstone (deleted entry)
│ [6] { key: NULL, value: ??? }   │  ← Empty slot
│ [7] { key: NULL, value: ??? }   │  ← Empty slot
└─────────────────────────────────┘
     ↑                    ↑
     Entry is ACTIVE      Entry is EMPTY/DELETED
     if key != NULL       if key == NULL
```

**Important**: When iterating through a hash table, you must check EVERY slot from 0 to capacity-1, because:
- Some slots have key==NULL but are empty (never filled)
- Some slots have key==NULL but are tombstones (deleted entries)
- Only slots with key!=NULL are active entries

---

## Value Array Structure (Constants)

```
ValueArray Structure:
┌──────────────┐
│ count: 3     │  ← Number of values actually stored
│ capacity: 8  │  ← Allocated size (grows dynamically)
│ values: ─────┼──→ Value values[8];
└──────────────┘

Value Array (Contiguous):
┌───────────────────────────┐
│ [0] { type: VAL_NUMBER,   │
│      as: { number: 42.0 } │
│ [1] { type: VAL_OBJ,      │
│      as: { obj: 0x12345 } │─┐ Points to ObjString("Hello")
│ [2] { type: VAL_NUMBER,   │ │
│      as: { number: 3.14 } │ │
│ [3] (unused)              │ │
│ ...                       │ │
└───────────────────────────┘ │
                              ▼
                      ┌──────────────┐
                      │ ObjString    │
                      │ "Hello"      │
                      └──────────────┘
```

**Key differences from hash table**:
- Values are CONTIGUOUS in memory (no holes)
- Only first `count` elements are valid
- Used for constants and function parameters

---

## Linked List Structure (Objects Heap)

```
vm.objects
    │
    ▼
┌────────────────────┐
│ Obj (ObjString)    │
├────────────────────┤
│ type: OBJ_STRING   │
│ next: ─────────────┼──┐
│ (cast to)          │  │
│ ObjString {        │  │
│   chars: "Hello"   │  │
│   length: 5        │  │
│   hash: 0xAAAA     │  │
│ }                  │  │
└────────────────────┘  │
                        ▼
                ┌────────────────────┐
                │ Obj (ObjString)    │
                ├────────────────────┤
                │ type: OBJ_STRING   │
                │ next: ─────────────┼──┐
                │ (cast to)          │  │
                │ ObjString {        │  │
                │   chars: "x"       │  │
                │   length: 1        │  │
                │   hash: 0xBBBB     │  │
                │ }                  │  │
                └────────────────────┘  │
                                        ▼
                                ┌────────────────────┐
                                │ Obj (ObjString)    │
                                ├────────────────────┤
                                │ type: OBJ_STRING   │
                                │ next: NULL         │
                                │ (cast to)          │
                                │ ObjString {        │
                                │   chars: "world"   │
                                │   length: 5        │
                                │   hash: 0xCCCC     │
                                │ }                  │
                                └────────────────────┘

Traversal Loop:
for (Obj* obj = vm.objects; obj != NULL; obj = obj->next) {
    // Process obj
    if (obj->type == OBJ_STRING) {
        ObjString* str = (ObjString*)obj;
        // Use str->chars, str->length, str->hash
    }
}
```

---

## Compilation Process with Instrumentation Points

```
┌─────────────────────────────────────────┐
│         INTERPRET(source)               │
└────────────┬────────────────────────────┘
             │
             ▼
   ┌─────────────────────┐
   │  compile(source)    │  ← Bytecode generation
   │                     │    Builds chunk.code
   │                     │    Builds chunk.constants
   │                     │    Interns strings
   │                     │
   └──────────┬──────────┘
              │
              ▼
   ┏━━━━━━━━━━━━━━━━━━━━━━━┓  ◄────── INSTRUMENTATION 1
   ┃ AFTER COMPILE         ┃
   ┃ Display:              ┃
   ┃ • Bytecode (existing) ┃
   ┃ • Constants array     ┃
   ┃ • Globals table       ┃
   ┃ • Strings table       ┃
   ┃ • Objects heap        ┃
   ┗━━━━━━━━━━━━━━━━━━━━━━━┛
              │
              ▼
   ┌─────────────────────┐
   │  run()              │  ← Execute bytecode
   │                     │    Stack operations
   │                     │    Variable assignments
   │                     │    Print statements
   │                     │
   └──────────┬──────────┘
              │
              ▼
   ┏━━━━━━━━━━━━━━━━━━━━━━━┓  ◄────── INSTRUMENTATION 2
   ┃ AFTER EXECUTE         ┃
   ┃ Display:              ┃
   ┃ • Constants array     ┃
   ┃ • Globals table       ┃
   ┃   (may have changed)  ┃
   ┃ • Strings table       ┃
   ┃ • Objects heap        ┃
   ┗━━━━━━━━━━━━━━━━━━━━━━━┛
              │
              ▼
        ┌──────────┐
        │ RETURN   │
        └──────────┘
```

---

## Data Flow Example

**Source Code:**
```lox
var x = 42;
print x;
```

**After Compilation:**

```
CONSTANTS ARRAY:
  [0] = 42 (NUMBER)

GLOBALS TABLE:
  (empty - not yet assigned)

STRINGS TABLE:
  "x" (identifier for variable x)

OBJECTS HEAP:
  ObjString("x")

BYTECODE:
  OP_CONSTANT 0      ← Load constant 42
  OP_DEFINE_GLOBAL 1 ← Define global named "x"
  OP_GET_GLOBAL 1    ← Get value of "x"
  OP_PRINT           ← Print top of stack
  OP_RETURN          ← Done
```

**After Execution:**

```
CONSTANTS ARRAY:
  [0] = 42 (NUMBER)
  (unchanged - constants are read-only)

GLOBALS TABLE:
  "x" => 42
  (now populated with variables assigned during execution)

STRINGS TABLE:
  "x" (identifier)
  (unchanged - same interned strings)

OBJECTS HEAP:
  ObjString("x")
  (unchanged - same objects)
```

---

## Type Hierarchy

```
Value (20 bytes on typical system)
├── Type: VAL_NIL
├── Type: VAL_BOOL
│   └── as.boolean (bool)
├── Type: VAL_NUMBER
│   └── as.number (double)
└── Type: VAL_OBJ
    └── as.obj (Obj*)
        └── Obj
            ├── type: VAL_OBJ_STRING
            ├── next: Obj*
            └── specific data (depends on type)
                └── ObjString
                    ├── type: OBJ_STRING
                    ├── next: Obj*
                    ├── chars: char*
                    ├── length: int
                    └── hash: uint32_t
```

---

## File Dependencies for Implementation

```
Changes needed in:

debug.h
  ├── #include "chunk.h"
  ├── #include "vm.h"
  └── new function declarations

debug.c
  ├── #include "debug.h"
  ├── #include "object.h"
  ├── #include "value.h"
  └── implementations of new functions

vm.c
  ├── Already has #include "debug.h"
  └── Modify interpret() function
      └── Call new display functions
```

---

## Quick Reference: Accessing Each Data Structure

### Constants Array
```c
// Access the constants array
ValueArray* constants = &chunk->constants;

// Iterate through constants
for (int i = 0; i < constants->count; i++) {
    Value v = constants->values[i];
    printValue(v);
}

// Check value type
if (IS_NUMBER(constants->values[i])) { ... }
if (IS_STRING(constants->values[i])) { ... }
if (IS_OBJ(constants->values[i])) { ... }
```

### Globals Table
```c
// Access the globals table
Table* globals = &vm.globals;

// Iterate through entries (must check ALL capacity slots)
for (int i = 0; i < globals->capacity; i++) {
    Entry* entry = &globals->entries[i];
    if (entry->key != NULL) {
        // Active entry
        printf("%s => ", entry->key->chars);
        printValue(entry->value);
    }
}
```

### Strings Table
```c
// Access the strings table (same structure as globals)
Table* strings = &vm.strings;

// Iterate through entries
for (int i = 0; i < strings->capacity; i++) {
    Entry* entry = &strings->entries[i];
    if (entry->key != NULL) {
        ObjString* str = entry->key;
        printf("String: %s (len=%d)\n", str->chars, str->length);
    }
}
```

### Objects Heap
```c
// Access the objects heap
Obj* objects = vm.objects;

// Traverse the linked list
for (Obj* obj = objects; obj != NULL; obj = obj->next) {
    if (obj->type == OBJ_STRING) {
        ObjString* str = (ObjString*)obj;
        printf("Object: %s\n", str->chars);
    }
}
```

---

## Common Debugging Patterns

### Pattern 1: Find a specific global
```c
Value value;
ObjString* name = copyString("x", 1);
if (tableGet(&vm.globals, name, &value)) {
    printf("Global 'x' = ");
    printValue(value);
}
```

### Pattern 2: Count total allocated memory
```c
size_t bytesConstants = chunk->constants.capacity * sizeof(Value);
size_t bytesGlobals = vm.globals.capacity * sizeof(Entry);
size_t bytesStrings = vm.strings.capacity * sizeof(Entry);
size_t totalBytes = bytesConstants + bytesGlobals + bytesStrings;
printf("Total allocated: %zu bytes\n", totalBytes);
```

### Pattern 3: Find memory fragmentation
```c
double globalLoad = vm.globals.count / (double)vm.globals.capacity;
double stringLoad = vm.strings.count / (double)vm.strings.capacity;
printf("Global table load: %.1f%%\n", globalLoad * 100);
printf("String table load: %.1f%%\n", stringLoad * 100);
```

### Pattern 4: Verify string interning
```c
// All identical string literals should be the same object
ObjString* a = copyString("hello", 5);
ObjString* b = copyString("hello", 5);
// a == b should be true (same pointer due to interning)
```
