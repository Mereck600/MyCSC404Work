//#include <table.h>
#ifndef clox_debug_h
#define clox_debug_h

#include "chunk.h"

#include "table.h"

void disassembleChunk(Chunk* chunk, const char* name);
int disassembleInstruction(Chunk* chunk, int offset);

void traceEnter(const char* name);
void traceExit();

#endif
// Display functions for compiler instrumentation
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