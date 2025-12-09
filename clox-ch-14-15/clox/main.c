#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"

int main(int argc, const char* argv[]) {
  initVM();

  Chunk chunk;
  initChunk(&chunk);

  int constant = addConstant(&chunk, 1.2);
  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, constant, 123);

  constant = addConstant(&chunk, 3.4);
  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, constant, 123);

  writeChunk(&chunk, OP_ADD, 123);

  constant = addConstant(&chunk, 5.6);
  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, constant, 123);

  writeChunk(&chunk, OP_DIVIDE, 123);
  writeChunk(&chunk, OP_NEGATE, 123);

  writeChunk(&chunk, OP_RETURN, 123);

  disassembleChunk(&chunk, "test chunk");
  interpret(&chunk);
  freeVM();
  freeChunk(&chunk);
  //tests for the main
  int a = addConstant(&chunk, 10.0);
  int b = addConstant(&chunk, 42.0);
  writeChunk(&chunk, OP_CONSTANT, 123); writeChunk(&chunk, a, 123);
  writeChunk(&chunk, OP_CONSTANT, 123); writeChunk(&chunk, b, 123);
  writeChunk(&chunk, OP_MIN, 123);         // stack: 10
  writeChunk(&chunk, OP_INCREMENT, 123);   // stack: 11
  writeChunk(&chunk, OP_CONSTANT, 123); writeChunk(&chunk, addConstant(&chunk, 3.0), 123);
  writeChunk(&chunk, OP_SWAP, 123);        // stack: 3, 11 -> 11, 3
  writeChunk(&chunk, OP_DECREMENT, 123);   // stack: 11, 2
  writeChunk(&chunk, OP_MAX, 123);         // stack: 11
  writeChunk(&chunk, OP_RETURN, 123);

  return 0;
}
