package codegen.C;

import control.Control;

public class PrettyPrintVisitor implements Visitor {
	private int indentLevel;
	private boolean isNestIf;
	private java.io.BufferedWriter writer;
	// used for lookup:
	private java.util.LinkedList<codegen.C.dec.T> formals;
	private java.util.LinkedList<codegen.C.dec.T> locals;
	private java.util.LinkedList<codegen.C.classs.T> classes;
	private java.util.LinkedList<codegen.C.Tuple> fields;

	public PrettyPrintVisitor() {
		this.isNestIf = false;
		this.indentLevel = 2;
	}

	private void indent() {
		this.indentLevel += 2;
	}

	private void unIndent() {
		this.indentLevel -= 2;
	}

	private void printSpaces() {
		int i = this.indentLevel;
		while (i-- != 0)
			this.say(" ");
	}

	private void sayln(String s) {
		say(s);
		try {
			this.writer.write("\n");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void say(String s) {
		try {
			this.writer.write(s);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// /////////////////////////////////////////////////////
	// expressions
	@Override
	public void visit(codegen.C.exp.Add e) {
		e.left.accept(this);
		this.say(" + ");
		e.right.accept(this);
		return;
	}

	@Override
	public void visit(codegen.C.exp.And e) {
		e.left.accept(this);
		this.say(" && ");
		e.right.accept(this);
		return;
	}

	@Override
	public void visit(codegen.C.exp.ArraySelect e) {
		e.array.accept(this);
		this.say("->array[");
		e.index.accept(this);
		this.say("]");
		return;
	}

	@Override
	public void visit(codegen.C.exp.Call e) {
		if (e.assign != null) {
			this.say("(" + e.assign + "=");
			e.exp.accept(this);
			this.say(", ");
			this.say(e.assign + "->vptr->" + e.id + "(" + e.assign);
			int size = e.args.size();
			if (size == 0) {
				this.say("))");
				return;
			}
			for (codegen.C.exp.T x : e.args) {
				this.say(", ");
				x.accept(this);
			}
			this.say("))");
		} else {
			e.exp.accept(this);
			this.say("->vptr->" + e.id + "(");
			e.exp.accept(this);
			int size = e.args.size();
			if (size == 0) {
				this.say(")");
				return;
			}
			for (codegen.C.exp.T x : e.args) {
				this.say(", ");
				x.accept(this);
			}
			this.say(")");
		}
		return;
	}

	@Override
	public void visit(codegen.C.exp.Id e) {
		boolean isFormal = false;
		boolean isLocal = false;
		boolean isField = false;
		for (codegen.C.dec.T d : this.formals) {
			codegen.C.dec.Dec dd = (codegen.C.dec.Dec) d;
			if (e.id.equals(dd.id)) {
				isFormal = true;
				break;
			}
		}
		if (isFormal) {
			this.say(e.id);
			return;
		}
		for (codegen.C.dec.T d : this.locals) {
			codegen.C.dec.Dec dd = (codegen.C.dec.Dec) d;
			if (e.id.equals(dd.id)) {
				isLocal = true;
				break;
			}
		}
		if (isLocal) {
			this.say(e.id);
			return;
		}
		for (codegen.C.Tuple t : this.fields) {
			if (e.id.equals(t.id)) {
				isField = true;
				break;
			}
		}
		if (isField) {
			this.say("this->" + e.id);
			return;
		} else
			this.say("unknown error when print codegen.C.exp.Id");
	}

	@Override
	public void visit(codegen.C.exp.Length e) {
		this.say("(");
		e.array.accept(this);
		this.say(")");
		this.say("->length");
		return;
	}

	@Override
	public void visit(codegen.C.exp.Lt e) {
		e.left.accept(this);
		this.say(" < ");
		e.right.accept(this);
		return;
	}

	@Override
	public void visit(codegen.C.exp.NewIntArray e) {
		this.say(" no action for the NewIntArray expression!");
		return;
	}

	@Override
	public void visit(codegen.C.exp.NewObject e) {
		this.say("((struct " + e.id + "*)(Tiger_new (&" + e.id
				+ "_vtable_, sizeof(struct " + e.id + "))))");
		return;
	}

	@Override
	public void visit(codegen.C.exp.Not e) {
		this.say("!(");
		e.exp.accept(this);
		this.say(")");
		return;
	}

	@Override
	public void visit(codegen.C.exp.Num e) {
		this.say(Integer.toString(e.num));
		return;
	}

	@Override
	public void visit(codegen.C.exp.Sub e) {
		e.left.accept(this);
		this.say(" - ");
		e.right.accept(this);
		return;
	}

	@Override
	public void visit(codegen.C.exp.This e) {
		this.say("this");
	}

	@Override
	public void visit(codegen.C.exp.Times e) {
		e.left.accept(this);
		this.say(" * ");
		e.right.accept(this);
		return;
	}

	// statements
	@Override
	public void visit(codegen.C.stm.Assign s) {
		this.printSpaces();
		// wrapping the string into codegen.C.exp.Id object and then visit this
		// object
		(new codegen.C.exp.Id(s.id)).accept(this);
		this.say(" = ");
		if (s.exp instanceof codegen.C.exp.NewIntArray) {
			codegen.C.exp.NewIntArray e = (codegen.C.exp.NewIntArray) s.exp;
			this.sayln("(struct intArray *)malloc(sizeof(struct intArray));");
			this.printSpaces();
			(new codegen.C.exp.Id(s.id)).accept(this);
			this.say("->length = ");
			e.exp.accept(this);
			this.sayln(";");
			this.printSpaces();
			(new codegen.C.exp.Id(s.id)).accept(this);
			this.say("->array = ");
			this.say("(int *)malloc((");
			e.exp.accept(this);
			this.sayln(")*sizeof(int));");
		} else {
			s.exp.accept(this);
			this.sayln(";");
		}
		return;
	}

	@Override
	public void visit(codegen.C.stm.AssignArray s) {
		this.printSpaces();
		(new codegen.C.exp.Id(s.id)).accept(this);
		this.say("->array[");
		s.index.accept(this);
		this.say("] = ");
		s.exp.accept(this);
		this.sayln(";");
		return;
	}

	@Override
	public void visit(codegen.C.stm.Block s) {
		this.unIndent();
		this.printSpaces();
		this.sayln("{");
		this.indent();
		for (codegen.C.stm.T stm : s.stms)
			stm.accept(this);
		this.unIndent();
		this.printSpaces();
		this.sayln("}");
		this.indent();
		return;
	}

	@Override
	public void visit(codegen.C.stm.If s) {
		if (this.isNestIf) {
			this.say(" ");
			this.isNestIf = false;
		} else
			this.printSpaces();
		this.say("if (");
		s.condition.accept(this);
		this.sayln(")");
		this.indent();
		s.thenn.accept(this);
		this.unIndent();
		this.printSpaces();
		if (s.elsee instanceof codegen.C.stm.If) {
			this.say("else");
			this.isNestIf = true;
			s.elsee.accept(this);
		} else {
			this.sayln("else");
			this.indent();
			s.elsee.accept(this);
			this.unIndent();
		}
		return;
	}

	@Override
	public void visit(codegen.C.stm.Print s) {
		this.printSpaces();
		this.say("System_out_println (");
		s.exp.accept(this);
		this.sayln(");");
		return;
	}

	@Override
	public void visit(codegen.C.stm.While s) {
		this.printSpaces();
		this.say("while (");
		s.condition.accept(this);
		this.sayln(")");
		this.indent();
		s.body.accept(this);
		this.unIndent();
		return;
	}

	// type
	@Override
	public void visit(codegen.C.type.Class t) {
		this.say("struct " + t.id + " *");
	}

	@Override
	public void visit(codegen.C.type.Int t) {
		this.say("int ");
	}

	@Override
	public void visit(codegen.C.type.IntArray t) {
		this.say("struct intArray *");
	}

	// dec
	@Override
	public void visit(codegen.C.dec.Dec d) {
		printSpaces();
		d.type.accept(this);
		this.say(" " + d.id + ";\n");
		return;
	}

	// method
	@Override
	public void visit(codegen.C.method.Method m) {
		this.formals = m.formals;
		this.locals = m.locals;
		for (codegen.C.classs.T c : this.classes) {
			codegen.C.classs.Class cc = (codegen.C.classs.Class) c;
			if (cc.id.equals(m.classId)) {
				this.fields = cc.decs;
				break;
			}
		}
		m.retType.accept(this);
		this.say(m.classId + "_" + m.id + "(");
		int count = m.formals.size();
		for (codegen.C.dec.T d : m.formals) {
			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
			count--;
			dec.type.accept(this);
			this.say(dec.id);
			if (count > 0)
				this.say(", ");
		}
		this.sayln(")");
		this.sayln("{");

		for (codegen.C.dec.T d : m.locals) {
			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
			dec.accept(this);
		}
		if (m.locals.size() > 0)
			this.sayln("");

		for (codegen.C.stm.T s : m.stms)
			s.accept(this);
		this.say("  return ");
		m.retExp.accept(this);
		this.sayln(";");
		this.sayln("}");
		return;
	}

	@Override
	public void visit(codegen.C.mainMethod.MainMethod m) {
		this.sayln("int Tiger_main ()");
		this.sayln("{");
		for (codegen.C.dec.T dec : m.locals) {
			this.say("  ");
			codegen.C.dec.Dec d = (codegen.C.dec.Dec) dec;
			d.type.accept(this);
			this.sayln(d.id + ";");
		}
		m.stm.accept(this);
		this.sayln("}\n");
		return;
	}

	// vtables
	@Override
	public void visit(codegen.C.vtable.Vtable v) {
		this.sayln("struct " + v.id + "_vtable");
		this.sayln("{");
		for (codegen.C.Ftuple t : v.ms) {
			this.say("  ");
			t.ret.accept(this);
			this.say("(*" + t.id + ")(");
			this.say("struct " + v.id + " *");
			for (codegen.C.dec.T d : t.args) {
				codegen.C.dec.Dec dd = (codegen.C.dec.Dec) d;
				this.say(", ");
				this.say(dd.type.toString());
				if (dd.type instanceof codegen.C.type.Class) {
					this.say(" *");
				}
			}
			this.sayln(");");
		}
		this.sayln("};\n");
		return;
	}

	private void outputVtable(codegen.C.vtable.Vtable v) {
		this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_ = ");
		this.sayln("{");
		int count = v.ms.size();
		for (codegen.C.Ftuple t : v.ms) {
			this.say("  ");
			this.say(t.classs + "_" + t.id);
			if ((--count) > 0)
				this.say(",");
			this.sayln("");
		}
		this.sayln("};\n");
		return;
	}

	// class
	@Override
	public void visit(codegen.C.classs.Class c) {
		this.sayln("struct " + c.id);
		this.sayln("{");
		this.sayln("  struct " + c.id + "_vtable *vptr;");
		for (codegen.C.Tuple t : c.decs) {
			this.say("  ");
			t.type.accept(this);
			this.say(" ");
			this.sayln(t.id + ";");
		}
		this.sayln("};");
		return;
	}

	// program
	@Override
	public void visit(codegen.C.program.Program p) {
		// we'd like to output to a file, rather than the "stdout".
		this.classes = p.classes;
		try {
			String outputName = null;
			if (Control.outputName != null)
				outputName = Control.outputName;
			else if (Control.fileName != null)
				outputName = Control.fileName + ".c";
			else
				outputName = "a.c";

			this.writer = new java.io.BufferedWriter(
					new java.io.OutputStreamWriter(
							new java.io.FileOutputStream(outputName)));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		this.sayln("// This is automatically generated by the Tiger compiler.");
		this.sayln("// Do NOT modify!\n");

		this.sayln("// structures");
		this.sayln("struct intArray");
		this.sayln("{");
		this.sayln("  int length;");
		this.sayln("  int *array;");
		this.sayln("};");
		this.sayln("");
		for (codegen.C.classs.T c : p.classes) {
			c.accept(this);
		}

		this.sayln("// vtables structures");
		for (codegen.C.vtable.T v : p.vtables) {
			v.accept(this);
		}
		this.sayln("");

		this.sayln("// methods declarations");
		for (codegen.C.method.T m : p.methods) {
			codegen.C.method.Method method = (codegen.C.method.Method) m;
			method.retType.accept(this);
			this.say(method.classId + "_" + method.id + "(");
			int count = method.formals.size();
			for (codegen.C.dec.T d : method.formals) {
				codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
				count--;
				dec.type.accept(this);
				this.say(dec.id);
				if (count > 0)
					this.say(", ");
			}
			this.sayln(");");
		}
		this.sayln("");

		this.sayln("// vtables");
		for (codegen.C.vtable.T v : p.vtables) {
			outputVtable((codegen.C.vtable.Vtable) v);
		}
		this.sayln("");

		this.sayln("// methods");
		for (codegen.C.method.T m : p.methods) {
			m.accept(this);
		}
		this.sayln("");

		this.sayln("// main method");
		p.mainMethod.accept(this);
		this.sayln("");

		this.say("\n\n");

		try {
			this.writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

}
