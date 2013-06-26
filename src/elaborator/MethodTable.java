package elaborator;

import java.util.Iterator;

import util.Id_name;

public class MethodTable {
	private java.util.Hashtable<Id_name, ast.type.T> table;

	public MethodTable() {
		this.table = new java.util.Hashtable<Id_name, ast.type.T>();
	}

	// Duplication is not allowed
	public void put(java.util.LinkedList<ast.dec.T> formals,
			java.util.LinkedList<ast.dec.T> locals) {
		Id_name name;
		for (ast.dec.T dec : formals) {
			ast.dec.Dec decc = (ast.dec.Dec) dec;
			if (this.table.get(decc.id) != null) {
				System.out.println("duplicated parameter: " + decc.id);
				// System.exit(1);
			}
			name = new Id_name(decc.id, 0);
			this.table.put(name, decc.type);
		}

		for (ast.dec.T dec : locals) {
			ast.dec.Dec decc = (ast.dec.Dec) dec;
			if (this.table.get(decc.id) != null) {
				System.out.println("duplicated variable: " + decc.id);
				// System.exit(1);
			}
			name = new Id_name(decc.id, 0);
			this.table.put(name, decc.type);
		}

	}

	// return null for non-existing keys
	public ast.type.T get(String id) {
		return this.table.get(id);
	}

	public void dump() {

		for (Iterator<Id_name> itr = this.table.keySet().iterator(); itr
				.hasNext();) {
			Id_name key = (Id_name) itr.next();
			ast.type.T type = this.table.get(key);
			if (key.getValue() == 0) {
				System.out.println("Warning: variable" + key.getId()
						+ " declared at line 10 never used");
			}
			System.out.println("\t" + type + "  " + key.getId());
		}
		System.out.println("");
		// new Todo();
	}

	@Override
	public String toString() {
		return this.table.toString();
	}
}
