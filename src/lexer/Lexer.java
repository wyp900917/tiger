package lexer;

import java.io.InputStream;

import lexer.Token.Kind;
import util.Todo;
import util.Todo.ErrorKind;

public class Lexer {
	String fname; // the input file name to be compiled
	InputStream fstream; // input stream for the above file

	public Lexer(String fname, InputStream fstream) {
		this.fname = fname;
		this.fstream = fstream;
	}

	String[] words = new String[] { "boolean", "class", "else", "extends",
			"false", "if", "int", "length", "main", "new", "out", "println",
			"public", "return", "static", "String", "System", "this", "true",
			"void", "while" };
	Kind[] kinds = new Kind[] { Kind.TOKEN_BOOLEAN, Kind.TOKEN_CLASS,
			Kind.TOKEN_ELSE, Kind.TOKEN_EXTENDS, Kind.TOKEN_FALSE,
			Kind.TOKEN_IF, Kind.TOKEN_INT, Kind.TOKEN_LENGTH, Kind.TOKEN_MAIN,
			Kind.TOKEN_NEW, Kind.TOKEN_OUT, Kind.TOKEN_PRINTLN,
			Kind.TOKEN_PUBLIC, Kind.TOKEN_RETURN, Kind.TOKEN_STATIC,
			Kind.TOKEN_STRING, Kind.TOKEN_SYSTEM, Kind.TOKEN_THIS,
			Kind.TOKEN_TRUE, Kind.TOKEN_VOID, Kind.TOKEN_WHILE };
	int tokenval;
	int lineno = 1;
	int columnno = 0;
	String lexbuf = "";

	// When called, return the next token (refer to the code "Token.java")
	// from the input stream.
	// Return TOKEN_EOF when reaching the end of the input stream.
	// The value for "lineNum" is now "null",
	// you should modify this to an appropriate
	// line number for the "EOF" token.
	private Token nextTokenInternal() throws Exception {
		int c;
		int cur_column;
		c = this.fstream.read();
		columnno++;
		while (true) {
			// skip all kinds of "blanks"
			while (' ' == c || '\t' == c || 13 == c || 10 == c) {
				if (10 == c) {//换行字符ASICC码=10
					lineno++;
					columnno = 0;
				}
				if ('\t' == c) {
					columnno += 7;
				}
				c = this.fstream.read();
				columnno++;//每次读一个字符，列号加1
			}
			if (-1 == c)//文件结束标志
				return new Token(Kind.TOKEN_EOF, lineno, columnno);
			if (isdigit(c)) {//判断当前字符是否为数字字符
				columnno++;
				cur_column = columnno;
				tokenval = c - '0';
				this.fstream.mark(1);//标记当前读取的字符数为1
				c = this.fstream.read();
				while (isdigit(c)) {
					columnno++;
					tokenval = tokenval * 10 + c - '0';
					this.fstream.mark(1);
					c = this.fstream.read();
				}
				if (isalpha(c)) {//判断当前字符是否为字母
					new Todo(ErrorKind.非法标识符, lineno, columnno);
					return null;
				}
				this.fstream.reset();//将文件指针返回到上一次标记处
				columnno--;
				return new Token(Kind.TOKEN_NUM, lineno, cur_column,
						Integer.toString(tokenval));
			}
			if (isalpha(c)) {//判断当前字符是否为字母
				cur_column = columnno;
				lexbuf = "";
				while (isalpha(c) || isdigit(c)) {
					columnno++;
					lexbuf += String.valueOf((char) c);
					this.fstream.mark(1);
					c = this.fstream.read();
				}
				this.fstream.reset();
				columnno--;
				int p = lookup(lexbuf);
				if (-1 != p) {
					return new Token(kinds[p], lineno, cur_column);
				} else {
					return new Token(Kind.TOKEN_ID, lineno, cur_column,lexbuf);
				}
			}
			switch (c) {
			case '+':
				return new Token(Kind.TOKEN_ADD, lineno, columnno);
			case '&':
				if (this.fstream.read() == '&') {
					return new Token(Kind.TOKEN_AND, lineno, ++columnno);
				}
			case '=':
				return new Token(Kind.TOKEN_ASSIGN, lineno, columnno);
			case ',':
				return new Token(Kind.TOKEN_COMMER, lineno, columnno);
			case '.':
				return new Token(Kind.TOKEN_DOT, lineno, columnno);
			case '{':
				return new Token(Kind.TOKEN_LBRACE, lineno, columnno);
			case '[':
				return new Token(Kind.TOKEN_LBRACK, lineno, columnno);
			case '(':
				return new Token(Kind.TOKEN_LPAREN, lineno, columnno);
			case '<':
				return new Token(Kind.TOKEN_LT, lineno, columnno);
			case '!':
				return new Token(Kind.TOKEN_NOT, lineno, columnno);
			case '}':
				return new Token(Kind.TOKEN_RBRACE, lineno, columnno);
			case ']':
				return new Token(Kind.TOKEN_RBRACK, lineno, columnno);
			case ')':
				return new Token(Kind.TOKEN_RPAREN, lineno, columnno);
			case ';':
				return new Token(Kind.TOKEN_SEMI, lineno, columnno);
			case '-':
				return new Token(Kind.TOKEN_SUB, lineno, columnno);
			case '*':
				return new Token(Kind.TOKEN_TIMES, lineno, columnno);
			case '/':
				int cc = this.fstream.read();
				if ('/' == cc) {
					cc = this.fstream.read();
					while (cc != 10 && cc != -1) {
						cc = this.fstream.read();
					}
					if (cc == 10) {
						lineno++;
						columnno = 0;
					}
					c = this.fstream.read();
					columnno++;
					continue;
				} else if (cc == '*') {
					int circle = 1;
					cc = this.fstream.read();
					while (0 != circle) {
						while (cc != '*') {
							cc = this.fstream.read();
							columnno++;
							if (cc == 10) {
								lineno++;
								columnno = 0;
							}
							if ('\t' == c) {
								columnno += 7;
							}
						}
						cc = this.fstream.read();
						columnno++;
						if (cc == '/') {
							c = this.fstream.read();
							columnno++;
							circle--;
						}
					}
					continue;
				} else {
					new Todo(ErrorKind.非法标识符, lineno, columnno);
					return null;
				}

			default:
				/* /////*gfghgfg */
				// Lab 1, exercise 2: supply missing code to
				// lex other kinds of tokens.
				// Hint: think carefully about the basic
				// data structure and algorithms. The code
				// is not that much and may be less than 50 lines. If you
				// find you are writing a lot of code, you
				// are on the wrong way.
				new Todo(ErrorKind.非法字符, lineno, columnno);
				return null;
			}
		}

	}

	public boolean isdigit(int c) {
		return c - '0' >= 0 && c - '9' <= 0 ? true : false;
	}

	public boolean isalpha(int c) {
		return c - 'a' >= 0 && c - 'z' <= 0 || c - 'A' >= 0 && c - 'Z' <= 0
				|| c == '_' ? true : false;
	}

	public int lookup(String lexbuf) {
		for (int i = 0; i < words.length; i++) {
			if (words[i].equals(lexbuf)) {
				return i;
			}
		}
		return -1;
	}

	public Token nextToken() {
		Token t = null;

		try {
			t = this.nextTokenInternal();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		if (control.Control.lex)
			System.out.println(t.toString());
		return t;
	}
}
