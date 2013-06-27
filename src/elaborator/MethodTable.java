package elaborator;

import java.util.Iterator;

public class MethodTable {
	private java.util.Hashtable<String, ast.type.T> table;

	public MethodTable() {
		this.table = new java.util.Hashtable<String, ast.type.T>();
	}

	// Duplication is not allowed
	public void put(java.util.LinkedList<ast.dec.T> formals,
			java.util.LinkedList<ast.dec.T> locals) {
		for (ast.dec.T dec : formals) {
			ast.dec.Dec decc = (ast.dec.Dec) dec;
			if (this.table.get(decc.id) != null) {
				System.out.println("duplicated parameter: " + decc.id);
				//System.exit(1);
			}
			this.table.put(decc.id, decc.type);
		}

		for (ast.dec.T dec : locals) {
			ast.dec.Dec decc = (ast.dec.Dec) dec;
			if (this.table.get(decc.id) != null) {
				System.out.println("duplicated variable: " + decc.id);
				//System.exit(1);
			}
			this.table.put(decc.id, decc.type);
		}

	}
	// return null for non-existing keys
	public ast.type.T get(String id) {
		return this.table.get(id);
	}
	public void dump() {

		for (Iterator<String> itr = this.table.keySet().iterator(); itr
				.hasNext();) {
			String key = (String) itr.next();
			ast.type.T type = this.table.get(key);
			System.out.println("\t" + type + "  " + key);
		}
		// new Todo();
	}

	
	public java.util.Hashtable<String, ast.type.T> getTable() {
		return table;
	}

	@Override
	public String toString() {
		return this.table.toString();
	}
}
