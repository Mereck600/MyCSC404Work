# Visual Implementation Overview

## The Big Picture

```
Your Goal: Display internal compiler/VM data structures
                        ↓
        ┌───────────────────────────────────┐
        │  Add 4 Display Functions          │
        ├───────────────────────────────────┤
        │ • disassembleConstants()          │
        │ • disassembleGlobals()            │
        │ • disassembleStrings()            │
        │ • disassembleObjectsHeap()        │
        └───────────────────────────────────┘
                        ↓
        ┌───────────────────────────────────┐
        │  Call Them at 2 Points            │
        ├───────────────────────────────────┤
        │ 1. After compilation              │
        │ 2. After execution                │
        └───────────────────────────────────┘
```

---

## What You're Displaying

```
┌─────────────────────────────────────────────────┐
│           Four Data Structures                  │
├─────────────────────────────────────────────────┤
│                                                 │
│  1. Constants Array                             │
│     Literal values: 42, 3.14, "hello"          │
│     ↓                                           │
│     Located in: chunk->constants               │
│     Type: ValueArray                           │
│                                                 │
│  2. Globals Table (Hash Table)                 │
│     Variables: x=42, y="hello"                 │
│     ↓                                           │
│     Located in: vm.globals                     │
│     Type: Table                                │
│                                                 │
│  3. Strings Table (Hash Table)                 │
│     Interned strings: "x", "hello", "print"   │
│     ↓                                           │
│     Located in: vm.strings                     │
│     Type: Table                                │
│                                                 │
│  4. Objects Heap (Linked List)                 │
│     All objects: [String][String][String]...  │
│     ↓                                           │
│     Located in: vm.objects                     │
│     Type: Obj* (linked list)                  │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## Three Files to Modify

```
BEFORE                          AFTER
──────────────────────────────────────────────────

debug.h                         debug.h
├─ existing                    ├─ existing
│  declarations                │  declarations
└─ (empty)                     └─ NEW: 5 function
                                    declarations

debug.c                         debug.c
├─ existing                    ├─ existing
│  functions                   │  functions
└─ (empty)                     └─ NEW: 5 function
                                    implementations

vm.c                            vm.c
├─ interpret() {              ├─ interpret() {
│    compile()                 │    compile()
│    run()                     │    ← NEW: display calls
│  }                           │    run()
└─ (no displays)               │    ← NEW: display calls
                               │  }
                               └─ displays added
```

---

## Implementation Flowchart

```
START
  │
  ├─ Read one of these files:
  │  • QUICK_REFERENCE.md (fastest)
  │  • COMPLETE_SUMMARY.md (thorough)
  │  • INSTRUMENTATION_GUIDE.md (detailed)
  │
  ├─ Study DATA_STRUCTURES_VISUAL.md
  │
  ├─ Open IMPLEMENTATION_EXAMPLES.md
  │
  ├─ Edit debug.h
  │  └─ Add 5 function declarations
  │
  ├─ Edit debug.c
  │  └─ Add 5 function implementations
  │
  ├─ Edit vm.c
  │  └─ Modify interpret()
  │
  ├─ Run: make clean && make
  │  └─ Any errors?
  │     ├─ YES: Check STEP_BY_STEP Phase 5.2
  │     └─ NO: Continue
  │
  ├─ Run: ./build/clox test.lox
  │  └─ See output?
  │     ├─ YES: You're done! ✅
  │     └─ NO: Check STEP_BY_STEP Phase 6
  │
END
```

---

## Output Timeline

```
Execution Timeline:
┌────────────────────────────────────────────────┐
│                                                │
│  interpret(source)                            │
│   ├─ compile()                                │
│   │                                           │
│   ├─ INSTRUMENTATION POINT 1 ◄────┐         │
│   │  Display all 4 data structures │         │
│   │                                │         │
│   ├─ run()                         │         │
│   │  (program executes & prints)  │         │
│   │                                │         │
│   ├─ INSTRUMENTATION POINT 2 ◄────┤         │
│   │  Display all 4 data structures │         │
│   │                                │         │
│   └─ return                        │         │
│                                    │         │
└────────────────────────────────────┴─────────┘

Expected Output Difference:
  Point 1: Globals empty, Constants full
  Point 2: Globals populated, Constants same
```

---

## Data Structure Details

### Constants Array
```
chunk->constants
├─ count: 3          (actual # of constants)
├─ capacity: 8       (allocated space)
└─ values[]: [
    [0] = 42
    [1] = 3.14
    [2] = "hello"
    [3-7] = (unused)
   ]

After Compilation: FULL
After Execution: SAME
```

### Globals Table
```
vm.globals
├─ count: 0          (actual # of globals)
├─ capacity: 0       (allocated space)
└─ entries[]:
    (empty initially)

After Compilation: EMPTY
After Execution: FULL (populated)
  x => 42
  y => "hello"
```

### Strings Table
```
vm.strings
├─ count: 4
├─ capacity: 8
└─ entries[]:
    [key="x"] => ObjString("x")
    [key="hello"] => ObjString("hello")
    (etc.)

After Compilation: FULL
After Execution: SAME
```

### Objects Heap
```
vm.objects → [ObjString] → [ObjString] → [ObjString] → NULL
             "hello"       "world"       "test"

After Compilation: FULL
After Execution: SAME
```

---

## Code Overview

### Function 1: countObjects()
```
Purpose: Count total objects in heap
Input: Obj* objects
Output: int count
Loop: While obj != NULL, increment count
```

### Function 2: disassembleConstants()
```
Purpose: Display all constants
Input: Chunk* chunk
Loop: For i=0 to count-1
      Print index and value
```

### Function 3: disassembleGlobals()
```
Purpose: Display all globals
Input: Table* globals
Loop: For i=0 to capacity-1
      If entry->key != NULL
      Print name and value
```

### Function 4: disassembleStrings()
```
Purpose: Display interned strings
Input: Table* strings
Loop: For i=0 to capacity-1
      If entry->key != NULL
      Print string details
```

### Function 5: disassembleObjectsHeap()
```
Purpose: Display all objects
Input: Obj* objects
Loop: For obj != NULL
      Cast to appropriate type
      Print object details
```

---

## Testing Strategy

```
Test Phase 1: Simple Constants
Input:
  print 42;
  
Expected:
  Constants: [42]
  Globals: (empty)
  Strings: (just identifiers)
  Objects: (just string objects)

Test Phase 2: Variables
Input:
  var x = 42;
  print x;
  
Expected:
  After compile: Globals (empty)
  After execute: Globals (x => 42)

Test Phase 3: String Interning
Input:
  var msg = "hello";
  print msg;
  print "hello";
  
Expected:
  Only one "hello" in objects heap
```

---

## Success Indicators

### ✅ Compilation Success
```
$> make clean && make
cc -Wall -Wextra -std=c99 -O0 -g...
... (no errors) ...
```

### ✅ Execution Success
```
$> ./build/clox test.lox

╔════════════════════════════════════════╗
║    INSTRUMENTATION: AFTER COMPILE     ║
╚════════════════════════════════════════╝

--- Constants Array ---
  Count: 1, Capacity: 8
  [  0] 42

--- Globals Table ---
  Count: 0, Capacity: 0, Load: 0.0%
  (no globals)

--- Interned Strings Table ---
  Count: 2, Capacity: 4, Load: 50.0%
  ...

--- Objects Heap (Linked List) ---
  Total: 2 objects

42

╔════════════════════════════════════════╗
║    INSTRUMENTATION: AFTER EXECUTE     ║
╚════════════════════════════════════════╝

--- Globals Table ---
  Count: 1, Capacity: 4, Load: 25.0%
  x                => 42

...
```

---

## Common Errors & Solutions

```
ERROR: Undefined reference to disassembleConstants
FIX:   • Check debug.h has declaration
       • Check debug.c has implementation
       • Rebuild with: make clean && make

ERROR: 'Table' has no member named 'capacity'
FIX:   • Check table.h for correct field names
       • May be using older version of code

ERROR: Segmentation fault
FIX:   • Check you're not dereferencing NULL
       • In tables, verify entry->key != NULL
       • In linked list, verify obj != NULL

ERROR: No output at all
FIX:   • Verify binary was rebuilt
       • Verify test file exists
       • Check stdout isn't buffered (add fflush)
```

---

## Documentation Map

```
                    You are here
                         ↓
          DOCUMENTATION_INDEX.md
                    (this file)
                         ↓
        ┌────────────────┼────────────────┐
        │                │                │
        ↓                ↓                ↓
   Quick Start    Deep Dive         Code Ready
   (1 hour)       (2 hours)         (30 min)
        │                │                │
        ├─→ QUICK_        ├─→ INSTRUMENTATION_ ├─→ IMPLEMENTATION_
        │   REFERENCE.md  │   GUIDE.md         │   EXAMPLES.md
        │                 ├─→ COMPLETE_       │
        ├─→ STEP_BY_      │   SUMMARY.md       ├─→ STEP_BY_STEP_
        │   STEP_         │                    │   IMPLEMENTATION.md
        │   IMPLEMENTATION├─→ DATA_STRUCTURES_ 
        │   .md           │   VISUAL.md        
        │                 │
        └─────────────────┴────────────────────┘
```

---

## Time Breakdown

```
Learning Phase:     1 hour
  ├─ Read conceptual docs
  ├─ Study diagrams
  └─ Understand data structures

Implementation:     1 hour
  ├─ Edit debug.h (10 min)
  ├─ Edit debug.c (30 min)
  ├─ Edit vm.c (10 min)
  └─ Compile (10 min)

Testing:           30 min
  ├─ Basic test (10 min)
  ├─ Complex test (10 min)
  └─ Troubleshoot if needed (10 min)

Total:             2.5 hours
```

---

## Your Next Action

Choose one:

**Option A: Quick Path** (1 hour)
```
1. Read QUICK_REFERENCE.md (10 min)
2. Copy code from IMPLEMENTATION_EXAMPLES.md (20 min)
3. Edit files and compile (20 min)
4. Test (10 min)
```

**Option B: Understanding First** (2 hours)
```
1. Read COMPLETE_SUMMARY.md (20 min)
2. Read INSTRUMENTATION_GUIDE.md (30 min)
3. Study DATA_STRUCTURES_VISUAL.md (30 min)
4. Follow STEP_BY_STEP_IMPLEMENTATION.md (40 min)
```

**Option C: Hands-On Learning** (1.5 hours)
```
1. Skim QUICK_REFERENCE.md (5 min)
2. Open IMPLEMENTATION_EXAMPLES.md (reference)
3. Open STEP_BY_STEP_IMPLEMENTATION.md (follow)
4. Code and test (60 min)
5. Use DATA_STRUCTURES_VISUAL.md if confused (reference)
```

---

## Summary

You have complete documentation for implementing compiler instrumentation that displays:
- ✅ Constants array
- ✅ Globals table
- ✅ Interned strings table
- ✅ Objects heap

At two key points:
- ✅ After compilation
- ✅ After execution

Using ready-to-use code examples and detailed step-by-step guides.

**Start with the path that matches your learning style above.**

Good luck! 🚀
