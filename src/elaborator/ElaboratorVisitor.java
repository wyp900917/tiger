package elaborator;

import java.util.Hashtable;

public class ElaboratorVisitor implements ast.Visitor {
	public ClassTable classTable; // symbol table for class
	public Hashtable<String,MethodTable> methodTable; // symbol table for each method
	public String currentClass; // the class name being elaborated
	public String currentMethod;
	public ast.type.T type; // type of the expression being elaborated

	public ElaboratorVisitor() {
		this.classTable = new ClassTable();
		this.methodTable = new Hashtable<String,MethodTable>();
		this.currentClass = null;
		this.currentMethod = null;
		this.type = null;
	}

	private void error(ast.type.T t1, ast.type.T t2, String info) {
		System.out.println("type mismatch:");
		if (t1 == null && t2 == null) {
			System.out.println("\t"+info);
		} else
			System.out.println("\tInformation:" + t1.toString()
					+ " not match " + t2.toString());
		return;
		// System.exit(1);
	}

	// /////////////////////////////////////////////////////
	// expressions
	@Override
	public void visit(ast.exp.Add e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!this.type.toString().equals(leftty.toString())) {
			error(leftty, this.type, null);
		}
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.And e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!this.type.toString().equals(leftty.toString()))
			error(leftty, this.type, null);
		this.type = new ast.type.Boolean();
		return;
	}

	@Override
	public void visit(ast.exp.ArraySelect e) {
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.Call e) {
		ast.type.T leftty;
		ast.type.Class ty = null;

		e.exp.accept(this);
		leftty = this.type;
		if (leftty instanceof ast.type.Class) {
			ty = (ast.type.Class) leftty;
			e.type = ty.id;
		} else
			error(leftty, ty, null);
		MethodType mty = this.classTable.getm(ty.id, e.id);
		java.util.LinkedList<ast.type.T> argsty = new java.util.LinkedList<ast.type.T>();
		for (ast.exp.T a : e.args) {
			a.accept(this);
			argsty.addLast(this.type);
		}
		if (mty.argsType.size() != argsty.size())
			error(null, null, e.id + ":该函数参数数目不匹配！");
		else {
			for (int i = 0; i < argsty.size(); i++) {
				ast.dec.Dec dec = (ast.dec.Dec) mty.argsType.get(i);
				if (dec.type.toString().equals(argsty.get(i).toString()))
					;
				else
					error(dec.type, argsty.get(i), null);
			}
		}

		this.type = mty.retType;
		e.at = argsty;
		e.rt = this.type;
		return;
	}

	@Override
	public void visit(ast.exp.False e) {
		this.type = new ast.type.Boolean();
		return;
	}

	@Override
	public void visit(ast.exp.Id e) {
		// first look up the id in method table
		ast.type.T type = this.methodTable.get(this.currentMethod).get(e.id);
		// if search failed, then s.id must be a class field.
		if (type == null) {
			type = this.classTable.get(this.currentClass, e.id);
			// mark this id as a field id, this fact will be
			// useful in later phase.
			e.isField = true;
		}
		if (type == null)
			error(null, null, e.id + ":该标识符未定义！");
		this.type = type;
		// record this type on this node for future use.
		e.type = type;
		return;
	}

	@Override
	public void visit(ast.exp.Length e) {

	}

	@Override
	public void visit(ast.exp.Lt e) {
		e.left.accept(this);
		ast.type.T ty = this.type;
		e.right.accept(this);
		if (!this.type.toString().equals(ty.toString()))
			error(ty, this.type, null);
		this.type = new ast.type.Boolean();
		return;
	}

	@Override
	public void visit(ast.exp.NewIntArray e) {
		this.type = new ast.type.IntArray();
		return;
	}

	@Override
	public void visit(ast.exp.NewObject e) {
		this.type = new ast.type.Class(e.id);
		return;
	}

	@Override
	public void visit(ast.exp.Not e) {
		e.exp.accept(this);
		if (!this.type.toString().equals("@boolean"))
			error(this.type, new ast.type.Boolean(), null);
		this.type = new ast.type.Boolean();
		return;
	}

	@Override
	public void visit(ast.exp.Num e) {
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.Sub e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!this.type.toString().equals(leftty.toString()))
			error(leftty, this.type, null);
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.This e) {
		this.type = new ast.type.Class(this.currentClass);
		return;
	}

	@Override
	public void visit(ast.exp.Times e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!this.type.toString().equals(leftty.toString()))
			error(leftty, this.type, null);
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.True e) {
		this.type = new ast.type.Boolean();
		return;
	}

	// statements
	@Override
	public void visit(ast.stm.Assign s) {
		// first look up the id in method table
		ast.type.T type = this.methodTable.get(this.currentMethod).get(s.id);
		// if search failed, then s.id must
		if (type == null)
			type = this.classTable.get(this.currentClass, s.id);
		if (type == null)
			error(null, null, s.id + ":该标识符未定义！");
		s.exp.accept(this);
		if (!this.type.toString().equals(type.toString()))
			error(this.type, type, null);
		s.type = type;
		return;
	}

	@Override
	public void visit(ast.stm.AssignArray s) {
		ast.type.T type = this.methodTable.get(this.currentMethod).get(s.id);
		if (type == null)
			type = this.classTable.get(this.currentClass, s.id);
		if (type == null)
			error(null, null, s.id + ":该标识符未定义！");
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.stm.Block s) {

	}

	@Override
	public void visit(ast.stm.If s) {
		s.condition.accept(this);
		if (!this.type.toString().equals("@boolean"))
			error(this.type, new ast.type.Boolean(), null);
		s.thenn.accept(this);
		s.elsee.accept(this);
		return;
	}

	@Override
	public void visit(ast.stm.Print s) {
		s.exp.accept(this);
		if (!this.type.toString().equals("@int"))
			error(this.type, new ast.type.Int(), null);
		return;
	}

	@Override
	public void visit(ast.stm.While s) {
		s.condition.accept(this);
		if (!this.type.toString().equals("@boolean")) {
			error(this.type, new ast.type.Boolean(), null);
		}
		s.body.accept(this);
		return;
	}

	// type
	@Override
	public void visit(ast.type.Boolean t) {
		this.type = new ast.type.Boolean();
		return;
	}

	@Override
	public void visit(ast.type.Class t) {
		ast.type.T type = this.classTable.get(this.currentClass, t.id);
		if (type == null) {
			error(null, null, t.id + ":该标识符未定义！");
		}
		this.type = type;
		return;
	}

	@Override
	public void visit(ast.type.Int t) {
		// System.out.println("aaaa");
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.type.IntArray t) {
		this.type = new ast.type.IntArray();
		return;
	}

	// dec
	@Override
	public void visit(ast.dec.Dec d) {
		this.classTable.put(currentClass, d.id, d.type);
		return;
	}

	// method
	@Override
	public void visit(ast.method.Method m) {
		// construct the method table
		MethodTable mt = new MethodTable();
		mt.put(m.formals, m.locals);
		this.methodTable.put(m.id, mt);

		if (control.Control.elabMethodTable) {
			System.out.println("method " + m.id + "() has these variables:");
			this.methodTable.get(m.id).dump();
		}
		for (ast.stm.T s : m.stms) {
			this.currentMethod = m.id;
			s.accept(this);
		}
		
		m.retExp.accept(this);
		return;
	}

	// class
	@Override
	public void visit(ast.classs.Class c) {
		this.currentClass = c.id;

		for (ast.method.T m : c.methods) {
			m.accept(this);
		}
		return;
	}

	// main class
	@Override
	public void visit(ast.mainClass.MainClass c) {
		this.currentClass = c.id;
		// "main" has an argument "arg" of type "String[]", but
		// one has no chance to use it. So it's safe to skip it...
		for (ast.stm.T stm : c.stms) {
			stm.accept(this);
		}
		return;
	}

	// ////////////////////////////////////////////////////////
	// step 1: build class table
	// class table for Main class
	private void buildMainClass(ast.mainClass.MainClass main) {
		this.classTable.put(main.id, new ClassBinding(null));
	}

	// class table for normal classes
	private void buildClass(ast.classs.Class c) {
		this.classTable.put(c.id, new ClassBinding(c.extendss));
		for (ast.dec.T dec : c.decs) {
			ast.dec.Dec d = (ast.dec.Dec) dec;
			this.classTable.put(c.id, d.id, d.type);
		}
		for (ast.method.T method : c.methods) {
			ast.method.Method m = (ast.method.Method) method;
			this.classTable.put(c.id, m.id,
					new MethodType(m.retType, m.formals));
		}
	}

	// step 1: end
	// ///////////////////////////////////////////////////

	// program
	@Override
	public void visit(ast.program.Program p) {
		// ////////////////////////////////////////////////
		// step 1: build a symbol table for class (the class table)
		// a class table is a mapping from class names to class bindings
		// classTable: className -> ClassBinding{extends, fields, methods}
		buildMainClass((ast.mainClass.MainClass) p.mainClass);
		for (ast.classs.T c : p.classes) {
			buildClass((ast.classs.Class) c);
		}

		// we can double check that the class table is OK!
		if (control.Control.elabClassTable) {
			this.classTable.dump();
		}

		// ////////////////////////////////////////////////
		// step 2: elaborate each class in turn, under the class table
		// built above.
		p.mainClass.accept(this);
		for (ast.classs.T c : p.classes) {
			c.accept(this);
		}

	}
}
