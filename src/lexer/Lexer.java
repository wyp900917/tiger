package lexer;

import java.io.InputStream;

import util.Todo;

import lexer.Token.Kind;

public class Lexer
{
  String fname; // the input file name to be compiled
  InputStream fstream; // input stream for the above file

  public Lexer(String fname, InputStream fstream)
  {
    this.fname = fname;
    this.fstream = fstream;
  }

  // When called, return the next token (refer to the code "Token.java")
  // from the input stream.
  // Return TOKEN_EOF when reaching the end of the input stream.
  private Token nextTokenInternal() throws Exception
  {
    int c = this.fstream.read();
    if (-1 == c)
      // The value for "lineNum" is now "null",
      // you should modify this to an appropriate
      // line number for the "EOF" token.
      return new Token(Kind.TOKEN_EOF, null);

			default:
				/* /////*gfghgfg */
				// Lab 1, exercise 2: supply missing code to
				// lex other kinds of tokens.
				// Hint: think carefully about the basic
				// data structure and algorithms. The code
				// is not that much and may be less than 50 lines. If you
				// find you are writing a lot of code, you
				// are on the wrong way.
				new Todo(ErrorKind.·Ç·¨×Ö·û, lineno, columnno);
				return null;
			}
		}

    switch (c) {
    case '+':
      return new Token(Kind.TOKEN_ADD, null);
    default:
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

  public Token nextToken()
  {
    Token t = null;

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
