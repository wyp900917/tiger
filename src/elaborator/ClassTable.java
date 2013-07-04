package elaborator;

import java.util.Iterator;

public class ClassTable {
	// map each class name (a string), to the class bindings.
	private java.util.Hashtable<String, ClassBinding> table;

	public ClassTable() {
		this.table = new java.util.Hashtable<String, ClassBinding>();
	}

	// Duplication is not allowed
	public void put(String c, ClassBinding cb) {
		if (this.table.get(c) != null) {
			System.out.println("duplicated class: " + c);
			System.exit(1);
		}
		this.table.put(c, cb);
	}

	// put a field into this table
	// Duplication is not allowed
	public void put(String c, String id, ast.type.T type) {
		ClassBinding cb = this.table.get(c);
		cb.put(id, type);
		return;
	}

	// put a method into this table
	// Duplication is not allowed.
	// Also note that MiniJava does NOT allow overloading.
	public void put(String c, String id, MethodType type) {
		ClassBinding cb = this.table.get(c);
		cb.put(id, type);
		return;
	}

	// return null for non-existing class
	public ClassBinding get(String className) {
		return this.table.get(className);
	}

	// get type of some field
	// return null for non-existing field.
	public ast.type.T get(String className, String xid) {
		ClassBinding cb = this.table.get(className);
		ast.type.T type = cb.fields.get(xid);
		while (type == null) { // search all parent classes until found or fail
			if (cb.extendss == null)
				return type;

			cb = this.table.get(cb.extendss);
			type = cb.fields.get(xid);
		}
		return type;
	}

	// get type of some method
	// return null for non-existing method
	public MethodType getm(String className, String mid) {
		ClassBinding cb = this.table.get(className);
		MethodType type = cb.methods.get(mid);
		while (type == null) { // search all parent classes until found or fail
			if (cb.extendss == null)
				return type;

			cb = this.table.get(cb.extendss);
			type = cb.methods.get(mid);
		}
		return type;
	}

	public void dump() {
		for (Iterator<String> cls_table = this.table.keySet().iterator(); cls_table
				.hasNext();) {
			String cls_id = (String) cls_table.next();
			ClassBinding cls_bind = this.table.get(cls_id);
			System.out.println("Class:" + cls_id);
			System.out.println("\tfields: ");
			for (Iterator<?> fields = cls_bind.fields.keySet().iterator(); fields
					.hasNext();) {
				String key = (String) fields.next();
				ast.type.T type = cls_bind.fields.get(key);
				System.out.println("\t\t" + type + "  " + key);
			}
			System.out.println("");
			System.out.println("\tmethods: ");
			for (Iterator<?> methods = cls_bind.methods.keySet().iterator(); methods
					.hasNext();) {
				String key = (String) methods.next();
				MethodType type = cls_bind.methods.get(key);
				System.out.println("\t\t" + key + "():  " + type);
			}
			System.out
					.println("------------------------------------------------------------------------\n");
		}
		// new Todo();
	}

	public boolean isSubClass(String subClass, String superClass) {
		ClassBinding cb = this.table.get(subClass);
		// search all super-classes until found or fail
		if (cb == null) {
			return false;
		}
		while (!(cb.extendss.equals(superClass))) {
			if (cb.extendss == null) // there is no more super class
				return false;
			cb = this.table.get(cb.extendss); // go on
			if(cb == null)
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.table.toString();
	}
}
