# Compiler and flags
#Adjusted to work with tests and on wsl this was edited by chat bc i hate make files
CC = gcc
CFLAGS = -Wall -Wextra -g -I.

# Link math lib on non-Windows (some MinGW builds don't need -lm, this is safe)
ifeq ($(OS),Windows_NT)
  MATHLIB =
else
  MATHLIB = -lm
endif

# Directories
BUILD_DIR := build

# ---- Sources ----
# Main app uses main.c
COMMON_SRCS := chunk.c memory.c debug.c value.c vm.c
MAIN_SRCS   := main.c $(COMMON_SRCS)

# Test runner excludes main.c and adds the test file
TEST_SRCS   := tests/testopcodes.c $(COMMON_SRCS)

# ---- Objects ----
MAIN_OBJS := $(addprefix $(BUILD_DIR)/, $(MAIN_SRCS:.c=.o))
TEST_OBJS := $(addprefix $(BUILD_DIR)/, $(TEST_SRCS:.c=.o))

# ---- Targets ----
TARGET     := $(BUILD_DIR)/clox
TEST_BIN   := $(BUILD_DIR)/op_tests

.PHONY: all clean test run run-tests

# Default: build both
all: $(TARGET) $(TEST_BIN)

# Ensure build dir exists
$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

# Pattern rule: compile any .c to build/…/.o and ensure subdirs exist
$(BUILD_DIR)/%.o: %.c
	@mkdir -p $(dir $@)
	$(CC) $(CFLAGS) -c $< -o $@

# Link the main app
$(TARGET): $(MAIN_OBJS)
	$(CC) $(CFLAGS) $^ -o $@ $(MATHLIB)

# Link the test runner (note: no main.o here!)
$(TEST_BIN): $(TEST_OBJS)
	$(CC) $(CFLAGS) $^ -o $@ $(MATHLIB)

# Conveniences
test: $(TEST_BIN)
	"$(TEST_BIN)"

run: $(TARGET)
	"$(TARGET)"

run-tests: test

clean:
	rm -rf $(BUILD_DIR)
