package codegen.C.dec;

public abstract class T implements codegen.C.Acceptable {
	public int lineNumber;

	protected T() {
		lineNumber = parser.Parser.current.lineNum;
	}
}
