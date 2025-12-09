// tests/test_opcodes.c
#include <stdio.h>
#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"

// tiny helpers to make programs easy to write
static void emitConst(Chunk* c, double d, int line) {
  int idx = addConstant(c, d);
  writeChunk(c, OP_CONSTANT, line);
  writeChunk(c, idx, line);
}
static void emit(Chunk* c, uint8_t op, int line) { writeChunk(c, op, line); }

static void runCase(const char* name, void (*program)(Chunk* c), const char* expectNote) {
  Chunk chunk;
  initChunk(&chunk);
  program(&chunk);
  emit(&chunk, OP_RETURN, 123);

  printf("\n=== %s ===\n", name);
  if (expectNote) printf("Expect: %s\n", expectNote);
  disassembleChunk(&chunk, name);
  interpret(&chunk); // prints the top of stack because OP_RETURN in your VM prints it
  freeChunk(&chunk);
}

// ---------- test programs ----------

// Case 1: MIN/MAX/INC/DEC/SWAP demo
// push 10, push 42, MIN -> 10
// INC -> 11
// push 3, SWAP (11,3 -> 3 under top? your OP_SWAP swaps top two => (11,3) becomes (3,11)?)
// Using the OP_SWAP from our earlier suggestion (swap top two), stack (11,3) -> (3,11) if you pop/push style
// The version in vm we suggested kept order: after swap: top is previous second.
// Then DEC (on top) 3 -> 2
// MAX(11,2) -> 11
static void prog_min_max_inc_dec_swap(Chunk* c) {
  emitConst(c, 10.0, 123);
  emitConst(c, 42.0, 123);
  emit(c, OP_MIN, 123);         // -> 10
  emit(c, OP_INCREMENT, 123);   // -> 11
  emitConst(c, 3.0, 123);       // stack: 11, 3
  emit(c, OP_SWAP, 123);        // swap top two
  emit(c, OP_DECREMENT, 123);   // dec top -> 2
  emit(c, OP_MAX, 123);         // max(11,2) -> 11
}

// Case 2: INC/DEC sanity — (((7)+1)+1)-1 = 8
static void prog_inc_dec_sanity(Chunk* c) {
  emitConst(c, 7.0, 123);
  emit(c, OP_INCREMENT, 123);
  emit(c, OP_INCREMENT, 123);
  emit(c, OP_DECREMENT, 123);
}

// Case 3: SWAP checked via subtraction
// push 1, push 2, SWAP -> top two swapped; SUBTRACT => 2 - 1 = 1
static void prog_swap_then_subtract(Chunk* c) {
  emitConst(c, 1.0, 123);
  emitConst(c, 2.0, 123);
  emit(c, OP_SWAP, 123);
  emit(c, OP_SUBTRACT, 123);
}

int main(void) {
  initVM();

  runCase("MIN/MAX/INC/DEC/SWAP", prog_min_max_inc_dec_swap, "Final result should be 11");
  runCase("INC/DEC sanity",       prog_inc_dec_sanity,       "Final result should be 8");
  runCase("SWAP then SUBTRACT",   prog_swap_then_subtract,   "Final result should be 1");

  freeVM();
  return 0;
}
