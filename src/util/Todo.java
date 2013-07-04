package util;

public class Todo {

	public enum ErrorKind {
		非法字符, // 非法字符
		行号为空, // 行号为空
		非法标识符, // 非法标识符
		ERROR_
	}

	public ErrorKind error;// kind of error
	public Integer lineNum;// on which line of the source file this token
							// appears
	public Integer columnNum;// on which column of the source file this token
								// appears

	public Todo(ErrorKind error, Integer lineNum, Integer columnNum) {
		this.error = error;
		this.lineNum = lineNum;
		this.columnNum = columnNum;
		System.out.println(this.toString());
		throw new java.lang.Error();
	}

	@Override
	public String toString() {
		String s = "ERROR:" + this.error + "\tat line " + this.lineNum
				+ ",column " + this.columnNum;
		return s;
	}
}
