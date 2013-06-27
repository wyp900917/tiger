package parser;

import ast.stm.T;
import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

public class Parser {
	Lexer lexer;
	public static Token current;

	public Parser(String fname, java.io.InputStream fstream) {
		lexer = new Lexer(fname, fstream);
		current = lexer.nextToken();
	}

	// /////////////////////////////////////////////
	// utility methods to connect the lexer
	// and the parser.

	private void advance() {
		current = lexer.nextToken();
	}

	private String eatToken(Kind kind) {
		String id = null;
		if (kind == current.kind) {
			id = current.lexeme;
			advance();
		} else {
			System.out.print("at line " + current.lineNum + ",column "
					+ current.columnNum + '\t');
			System.out.print("Expects: " + kind.toString());
			System.out.println(",But got: " + current.kind.toString());
			System.exit(1);
		}
		return id;
	}

	private void error(Token current) {
		System.out.println("Syntax error: compilation aborting...\n");
		System.out.println("Error Information:" + current.kind.toString()
				+ "\tat line " + current.lineNum + ",column "
				+ current.columnNum);
		System.exit(1);
		return;
	}

	// ////////////////////////////////////////////////////////////
	// below are method for parsing.

	// A bunch of parsing methods to parse expressions. The messy
	// parts are to deal with precedence and associativity.

	// ExpList -> Exp ExpRest*
	// ->
	// ExpRest -> , Exp
	private java.util.LinkedList<ast.exp.T> parseExpList() {
		java.util.LinkedList<ast.exp.T> exp_list = new java.util.LinkedList<ast.exp.T>();
		if (current.kind == Kind.TOKEN_RPAREN)
			return exp_list;
		ast.exp.T exp = parseExp();
		exp_list.add(exp);
		while (current.kind == Kind.TOKEN_COMMER) {
			advance();
			exp = parseExp();
			exp_list.add(exp);
		}
		return exp_list;
	}

	// AtomExp -> (exp)
	// -> INTEGER_LITERAL
	// -> true
	// -> false
	// -> this
	// -> id
	// -> new int [exp]
	// -> new id ()
	private ast.exp.T parseAtomExp() {
		String id = null;
		switch (current.kind) {
		case TOKEN_LPAREN: // TOKEN_LPAREN:"("
			advance();
			ast.exp.T exps = parseExp();
			eatToken(Kind.TOKEN_RPAREN); // TOKEN_RPAREN:")"
			return exps;
		case TOKEN_NUM:
			String num = current.lexeme;
			advance();
			return new ast.exp.Num(Integer.valueOf(num));
		case TOKEN_FALSE:
			advance();
			return new ast.exp.False();
		case TOKEN_TRUE:
			advance();
			return new ast.exp.True();
		case TOKEN_THIS:
			advance();
			return new ast.exp.This();
		case TOKEN_ID:
			id = current.lexeme;
			advance();
			return new ast.exp.Id(id);
		case TOKEN_NEW: {
			advance();
			switch (current.kind) {
			case TOKEN_INT:
				advance();
				eatToken(Kind.TOKEN_LBRACK);
				ast.exp.T exp = parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				return new ast.exp.NewIntArray(exp);
			case TOKEN_ID:
				id = current.lexeme;
				advance();
				eatToken(Kind.TOKEN_LPAREN);
				eatToken(Kind.TOKEN_RPAREN);
				return new ast.exp.NewObject(id);
			default:
				error(current);
				return null;
			}
		}
		default:
			error(current);
			return null;
		}
	}

	// NotExp -> AtomExp
	// -> AtomExp .id (expList)
	// -> AtomExp [exp]
	// -> AtomExp .length
	private ast.exp.T parseNotExp() {
		ast.exp.T exp = parseAtomExp();
		while (current.kind == Kind.TOKEN_DOT // TOKEN_DOT:"."
				|| current.kind == Kind.TOKEN_LBRACK) { // TOKEN_LBRACK:"["
			if (current.kind == Kind.TOKEN_DOT) {
				advance();
				if (current.kind == Kind.TOKEN_LENGTH) {
					advance();
					return new ast.exp.Length(exp);
				}
				String id = eatToken(Kind.TOKEN_ID);
				eatToken(Kind.TOKEN_LPAREN);
				java.util.LinkedList<ast.exp.T> args = parseExpList();
				eatToken(Kind.TOKEN_RPAREN);
				return new ast.exp.Call(exp, id, args);
			} else {
				advance();
				ast.exp.T index = parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				return new ast.exp.ArraySelect(exp, index);
			}
		}
		return exp;
	}

	// TimesExp -> ! TimesExp
	// -> NotExp
	private ast.exp.T parseTimesExp() {
		//ast.exp.T not_exp = parseNotExp();
		ast.exp.T times_exp;
		if (current.kind != Kind.TOKEN_NOT) {
			return parseNotExp();
		} else {
			advance();
			times_exp = new ast.exp.Not(parseTimesExp());
		}
		return times_exp;
	}

	// AddSubExp -> TimesExp * TimesExp
	// -> TimesExp
	private ast.exp.T parseAddSubExp() {
		ast.exp.T times_exp = parseTimesExp();
		while (current.kind == Kind.TOKEN_TIMES) {
			advance();
			times_exp = new ast.exp.Times(times_exp, parseTimesExp());
		}
		return times_exp;
	}

	// LtExp -> AddSubExp + AddSubExp
	// -> AddSubExp - AddSubExp
	// -> AddSubExp
	private ast.exp.T parseLtExp() {
		ast.exp.T addsub_exp = parseAddSubExp();
		while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
			if (current.kind == Kind.TOKEN_ADD) {
				advance();
				addsub_exp = new ast.exp.Add(addsub_exp, parseAddSubExp());
			} else if (current.kind == Kind.TOKEN_SUB) {
				advance();
				addsub_exp = new ast.exp.Sub(addsub_exp, parseAddSubExp());
			}
		}
		return addsub_exp;
	}

	// AndExp -> LtExp < LtExp
	// -> LtExp
	private ast.exp.T parseAndExp() {
		ast.exp.T lt_exp = parseLtExp();
		while (current.kind == Kind.TOKEN_LT) {
			advance();
			lt_exp = new ast.exp.Lt(lt_exp, parseLtExp());
		}
		return lt_exp;
	}

	// Exp -> AndExp && AndExp
	// -> AndExp
	private ast.exp.T parseExp() {
		ast.exp.T and_exp = parseAndExp();
		while (current.kind == Kind.TOKEN_AND) {
			advance();
			and_exp = new ast.exp.And(and_exp, parseAndExp());
		}
		return and_exp;
	}

	// Statement -> { Statement* }
	// -> if ( Exp ) Statement else Statement
	// -> while ( Exp ) Statement
	// -> System.out.println ( Exp ) ;
	// -> id = Exp ;
	// -> id [ Exp ]= Exp ;
	private ast.stm.T parseStatement() {
		switch (current.kind) {
		case TOKEN_LBRACE: {
			java.util.LinkedList<T> stms = new java.util.LinkedList<T>();
			advance();
			while (current.kind == Kind.TOKEN_LBRACE
					|| current.kind == Kind.TOKEN_IF
					|| current.kind == Kind.TOKEN_WHILE
					|| current.kind == Kind.TOKEN_SYSTEM
					|| current.kind == Kind.TOKEN_ID) {
				ast.stm.T stm = parseStatement();
				stms.add(stm);
			}
			eatToken(Kind.TOKEN_RBRACE);
			return new ast.stm.Block(stms);
		}
		case TOKEN_IF: {
			advance();
			eatToken(Kind.TOKEN_LPAREN);
			ast.exp.T condition = parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			ast.stm.T thenn = parseStatement();
			eatToken(Kind.TOKEN_ELSE);
			ast.stm.T elsee = parseStatement();
			return new ast.stm.If(condition, thenn, elsee);
		}
		case TOKEN_WHILE: {
			advance();
			eatToken(Kind.TOKEN_LPAREN);
			ast.exp.T condition = parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			ast.stm.T body = parseStatement();
			return new ast.stm.While(condition, body);
		}
		case TOKEN_SYSTEM: {
			advance();
			eatToken(Kind.TOKEN_DOT);
			eatToken(Kind.TOKEN_OUT);
			eatToken(Kind.TOKEN_DOT);
			eatToken(Kind.TOKEN_PRINTLN);
			eatToken(Kind.TOKEN_LPAREN);
			ast.exp.T exp = parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			ast.stm.Print print = new ast.stm.Print(exp);
			eatToken(Kind.TOKEN_SEMI);
			return print;
		}
		case TOKEN_ID: {
			String id = current.lexeme;
			advance();
			switch (current.kind) {
			case TOKEN_ASSIGN: {
				advance();
				ast.exp.T exp = parseExp();
				ast.stm.Assign assign = new ast.stm.Assign(id, exp);
				eatToken(Kind.TOKEN_SEMI);
				return assign;
			}
			case TOKEN_LBRACK: {
				advance();
				ast.exp.T index = parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				eatToken(Kind.TOKEN_ASSIGN);
				ast.exp.T exp = parseExp();
				ast.stm.AssignArray assignarray = new ast.stm.AssignArray(id, index, exp);
				eatToken(Kind.TOKEN_SEMI);
				return assignarray;
			}
			}
		}
		default:
			error(current);
			return null;
		}
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a statement.

	}

	// Statements -> Statement Statements
	// ->
	private java.util.LinkedList<ast.stm.T> parseStatements() {
		java.util.LinkedList<ast.stm.T> stms = new java.util.LinkedList<ast.stm.T>();
		while (current.kind == Kind.TOKEN_LBRACE
				|| current.kind == Kind.TOKEN_IF
				|| current.kind == Kind.TOKEN_WHILE
				|| current.kind == Kind.TOKEN_SYSTEM
				|| current.kind == Kind.TOKEN_ID) {
			ast.stm.T stm = parseStatement();
			stms.add(stm);
		}
		return stms;
	}

	// Type -> int []
	// -> boolean
	// -> int
	// -> id
	private ast.type.T parseType() {
		switch (current.kind) {
		case TOKEN_INT: {
			advance();
			if (current.kind == Kind.TOKEN_LBRACK) {
				eatToken(Kind.TOKEN_LBRACK);
				eatToken(Kind.TOKEN_RBRACK);
				return new ast.type.IntArray();
			}
			return new ast.type.Int();
		}
		case TOKEN_BOOLEAN: {
			advance();
			return new ast.type.Boolean();
		}
		case TOKEN_ID: {
			String id = current.lexeme;
			advance();
			return new ast.type.Class(id);
		}
		default:
			error(current);
			return null;
		}
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a type.

	}

	// VarDecl -> Type id ;
	private ast.dec.Dec parseVarDecl() {
		// to parse the "Type" nonterminal in this method, instead of writing
		// a fresh one.
		ast.type.T type = parseType();
		String id = eatToken(Kind.TOKEN_ID);
		ast.dec.Dec dec = new ast.dec.Dec(type, id);
		eatToken(Kind.TOKEN_SEMI);
		return dec;
	}

	// VarDecls -> VarDecl VarDecls
	// ->
	private java.util.LinkedList<ast.dec.T> parseVarDecls() {
		java.util.LinkedList<ast.dec.T> decs = new java.util.LinkedList<ast.dec.T>();
		while (current.kind == Kind.TOKEN_INT
				|| current.kind == Kind.TOKEN_BOOLEAN
				|| current.kind == Kind.TOKEN_ID) {
			ast.dec.Dec dec = parseVarDecl();
			decs.add(dec);
		}
		return decs;
	}

	// FormalList -> Type id FormalRest*
	// ->
	// FormalRest -> , Type id
	private java.util.LinkedList<ast.dec.T> parseFormalList() {
		java.util.LinkedList<ast.dec.T> formals = new java.util.LinkedList<ast.dec.T>();
		if (current.kind == Kind.TOKEN_INT
				|| current.kind == Kind.TOKEN_BOOLEAN
				|| current.kind == Kind.TOKEN_ID) {
			ast.type.T type = parseType();
			String id = eatToken(Kind.TOKEN_ID);
			ast.dec.T dec = new ast.dec.Dec(type, id);
			formals.add(dec);
			while (current.kind == Kind.TOKEN_COMMER) {
				advance();
				type = parseType();
				id = eatToken(Kind.TOKEN_ID);
				dec = new ast.dec.Dec(type, id);
				formals.add(dec);
			}
		}
		return formals;
	}

	// Method -> public Type id ( FormalList )
	// { VarDecl* Statement* return Exp ;}
	private ast.method.Method parseMethod() {
		java.util.LinkedList<ast.dec.T> locals = new java.util.LinkedList<ast.dec.T>();
		java.util.LinkedList<ast.stm.T> stms = new java.util.LinkedList<ast.stm.T>();
		ast.dec.T dec = null;
		eatToken(Kind.TOKEN_PUBLIC);
		ast.type.T retType = parseType();
		String id = eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_LPAREN);
		java.util.LinkedList<ast.dec.T> formals = parseFormalList();
		eatToken(Kind.TOKEN_RPAREN);
		eatToken(Kind.TOKEN_LBRACE);
		while (current.kind == Kind.TOKEN_INT
				|| current.kind == Kind.TOKEN_BOOLEAN
				|| current.kind == Kind.TOKEN_ID) {
			if (current.kind != Kind.TOKEN_ID) {
				dec = parseVarDecl();
				locals.add(dec);
			} else {
				String id_1 = current.lexeme;
				advance();
				if (current.kind == Kind.TOKEN_ID) {
					String t_id = eatToken(Kind.TOKEN_ID);
					ast.type.T type = new ast.type.Class(id_1);
					dec = new ast.dec.Dec((ast.type.T) type, t_id);
					locals.add(dec);
					eatToken(Kind.TOKEN_SEMI);
				} else {
					if (current.kind == Kind.TOKEN_ASSIGN) {
						advance();
						ast.exp.T exp = parseExp();
						eatToken(Kind.TOKEN_SEMI);
						stms.add(new ast.stm.Assign(id_1, exp));
						break;
					} else if (current.kind == Kind.TOKEN_LBRACK) {
						advance();
						ast.exp.T index = parseExp();
						eatToken(Kind.TOKEN_RBRACK);
						eatToken(Kind.TOKEN_ASSIGN);
						ast.exp.T exp = parseExp();
						eatToken(Kind.TOKEN_SEMI);
						stms.add(new ast.stm.AssignArray(id_1, index, exp));
						break;
					} else {
						error(current);
						return null;
					}
				}

			}
		}
		java.util.LinkedList<ast.stm.T> stms_1 = parseStatements();
		stms.addAll(stms_1);
		eatToken(Kind.TOKEN_RETURN);
		ast.exp.T retExp = parseExp();
		eatToken(Kind.TOKEN_SEMI);
		eatToken(Kind.TOKEN_RBRACE);
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a method.
		// new util.Todo();
		return new ast.method.Method(retType, id, formals, locals, stms, retExp);
	}

	// MethodDecls -> MethodDecl MethodDecls
	// ->
	private java.util.LinkedList<ast.method.T> parseMethodDecls() {
		java.util.LinkedList<ast.method.T> methods = new java.util.LinkedList<ast.method.T>();
		while (current.kind == Kind.TOKEN_PUBLIC) {
			ast.method.Method method = parseMethod();
			methods.add(method);
		}
		return methods;
	}

	// ClassDecl -> class id { VarDecl* MethodDecl* }
	// -> class id extends id { VarDecl* MethodDecl* }
	private ast.classs.T parseClassDecl() {
		eatToken(Kind.TOKEN_CLASS);
		String class_name = eatToken(Kind.TOKEN_ID);
		String extends_name = null;
		if (current.kind == Kind.TOKEN_EXTENDS) {
			eatToken(Kind.TOKEN_EXTENDS);
			extends_name = eatToken(Kind.TOKEN_ID);
		}
		eatToken(Kind.TOKEN_LBRACE);
		java.util.LinkedList<ast.dec.T> decs = parseVarDecls();
		java.util.LinkedList<ast.method.T> methods = parseMethodDecls();
		eatToken(Kind.TOKEN_RBRACE);
		return new ast.classs.Class(class_name, extends_name, decs, methods);
	}

	// ClassDecls -> ClassDecl ClassDecls
	// ->
	private java.util.LinkedList<ast.classs.T> parseClassDecls() {
		java.util.LinkedList<ast.classs.T> classes = new java.util.LinkedList<ast.classs.T>();
		while (current.kind == Kind.TOKEN_CLASS) {
			ast.classs.T cls = parseClassDecl();
			classes.add(cls);
		}
		return classes;
	}

	public ast.program.T parse() {
		return parseProgram();
	}

	// MainClass -> class id
	// {
	// public static void main ( String [] id )
	// {
	// Statement
	// }
	// }
	private ast.mainClass.MainClass parseMainClass() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a main class as described by the
		// grammar above.
		eatToken(Kind.TOKEN_CLASS);
		String mainclass_id = eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_LBRACE);
		eatToken(Kind.TOKEN_PUBLIC);
		eatToken(Kind.TOKEN_STATIC);
		eatToken(Kind.TOKEN_VOID);
		eatToken(Kind.TOKEN_MAIN);
		eatToken(Kind.TOKEN_LPAREN);
		eatToken(Kind.TOKEN_STRING);
		eatToken(Kind.TOKEN_LBRACK);
		eatToken(Kind.TOKEN_RBRACK);
		String a = eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_RPAREN);
		eatToken(Kind.TOKEN_LBRACE);
		java.util.LinkedList<ast.stm.T> stms = parseStatements();
		eatToken(Kind.TOKEN_RBRACE);
		eatToken(Kind.TOKEN_RBRACE);
		ast.mainClass.MainClass mainclass = new ast.mainClass.MainClass(
				mainclass_id, a, stms);
		return mainclass;
		// new util.Todo();
	}

	// Program -> MainClass ClassDecl*
	private ast.program.Program parseProgram() {
		ast.mainClass.MainClass mainclass = parseMainClass();
		java.util.LinkedList<ast.classs.T> classes = parseClassDecls();
		ast.program.Program prog = new ast.program.Program(mainclass, classes);
		eatToken(Kind.TOKEN_EOF);
		return prog;
	}

}
