package lexer;

import java.io.InputStream;

import lexer.Token.Kind;
import util.Todo;

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
				if (10 == c) {
					lineno++;
					columnno = 0;
				}
				if ('\t' == c) {
					columnno += 7;
				}
				c = this.fstream.read();
				columnno++;
			}
			if (-1 == c)
				return new Token(Kind.TOKEN_EOF, lineno, columnno);
			if (isdigit(c)) {
				columnno++;
				cur_column = columnno;
				tokenval = c - '0';
				this.fstream.mark(1);
				c = this.fstream.read();
				while (isdigit(c)) {
					columnno++;
					tokenval = tokenval * 10 + c - '0';
					this.fstream.mark(1);
					c = this.fstream.read();
				}
				if (isalpha(c)) {
					new Todo();
					return null;
				}
				this.fstream.reset();
				columnno--;
				return new Token(Kind.TOKEN_NUM, lineno, cur_column,
						Integer.toString(tokenval));
			}
			if (isalpha(c)) {
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
					return new Token(Kind.TOKEN_ID, lineno, cur_column);
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
					cc = this.fstream.read();
					int circle = 1;
					while (circle != 0) {
						while (cc != '*' && cc != '/') {
							cc = this.fstream.read();
							if (cc == 10) {
								lineno++;
								columnno = 0;
							}
						}
						if (cc == '/' && this.fstream.read() == '*') {
							circle++;
							cc = this.fstream.read();
						}
						if (cc == '*' && this.fstream.read() == '/') {
							circle--;
							cc = this.fstream.read();
						}
					}
					c = this.fstream.read();
					columnno++;
					continue;
				} else {
					new Todo();
					return null;
				}

			default:
				/* /////*gfghgfg*/
				// Lab 1, exercise 2: supply missing code to
				// lex other kinds of tokens.
				// Hint: think carefully about the basic
				// data structure and algorithms. The code
				// is not that much and may be less than 50 lines. If you
				// find you are writing a lot of code, you
				// are on the wrong way.
				new Todo();
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
