package ast.dec;

public abstract class T implements ast.Acceptable {
	public int lineNumber;

	protected T() {
		lineNumber = parser.Parser.current.lineNum;
	}
}
