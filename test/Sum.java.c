// This is automatically generated by the Tiger compiler.
// Do NOT modify!

// structures
struct intArray
{
  int length;
  int *array;
};

struct Sum
{
  struct Sum_vtable *vptr;
};
struct Doit
{
  struct Doit_vtable *vptr;
};
// vtables structures
struct Sum_vtable
{
};

struct Doit_vtable
{
  int (*doit)(struct Doit *, int);
};


// methods declarations
int Doit_doit(struct Doit *this, int n);

// vtables
struct Sum_vtable Sum_vtable_ = 
{
};

struct Doit_vtable Doit_vtable_ = 
{
  Doit_doit
};


// methods
int Doit_doit(struct Doit *this, int n)
{
  int  sum;
  int  i;

  i = 0;
  sum = 0;
  while (i < n)
  {
    sum = sum + i;
    i = i + 1;
  }
  return sum;
}

// main method
int Tiger_main ()
{
  struct Doit *temp_1;
  System_out_println ((temp_1=((struct Doit*)(Tiger_new (&Doit_vtable_, sizeof(struct Doit)))), temp_1->vptr->doit(temp_1, 101)));
}




