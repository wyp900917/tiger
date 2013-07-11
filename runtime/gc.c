#include <stdio.h>
#include <stdlib.h>
#include <string.h>
struct intArray {
    int *array;
    int length;
};

// "new" a new object, do necessary initializations, and
// return the pointer (reference).
void *Tiger_new (void *vtable, int size)
{
  // You should write 4 statements for this function.
  // #1: "malloc" a chunk of memory of size "size":
  void *temp = (void *)malloc(size);
  // #2: clear this chunk of memory (zero off it):
  memset(temp, '\0', size);
  // #3: set up the "vtable" pointer properly:
  memcpy(temp, &vtable, sizeof(void *));
  // #4: return the pointer 
  return temp;
}

int *Tiger_array_new(int size) {
    struct intArray *as = (struct intArray*)malloc(sizeof(struct intArray));
    as->array = (int *)malloc(sizeof(int) * size);
    as->length = size;
    return as->array;
}
