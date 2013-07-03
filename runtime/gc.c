#include "interface.h"
// "new" a new object, do necessary initializations, and
// return the pointer (reference).
void *Tiger_new(void *vtable, int size) {
	// You should write 4 statements for this function.
	  // #1: "malloc" a chunk of memory of size "size":
	  void *ret;
	  int **set;
	  ret = malloc(size);
	  if (ret == NULL)
	    exit(1);
	  // #2: clear this chunk of memory (zero off it):
	  ret = memset(ret, 0, size);
	  // #3: set up the "vtable" pointer properly:
	  set = (int **)ret;
	  *(set + 0) = (int *)vtable;
	  // #4: return the pointer
	  return ret;

}
