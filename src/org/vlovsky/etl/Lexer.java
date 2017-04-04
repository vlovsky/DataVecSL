/*-
 * Copyright 2017 Vlad Sadilovki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.vlovsky.etl;

import java.io.IOException;
import java.util.*;

/**
 * Created by vlad on 3/31/2017.
 */
public class Lexer {
    private static Set<String> kw = new HashSet(Arrays.asList(new String[]
            {"if", "then", "else", "load", "data", "from", "into", "when", "enum", "as", "filler", "fields", "terminated", "by"}));
    private static Set<String> tp = new HashSet(Arrays.asList(new String[] {"string", "integer", "double"}));
    private static String punc = ",;{}[]()'";
    private static String ops = "-+=/*&|<>";
    private static Set<String> mcOp = new HashSet(Arrays.asList(new String[] {"and", "or", "!=", "<>", ">=", "<=", "in"}));
    private InputStream inp;
    private Pair next;
    private Pair last;

    protected Lexer(InputStream inp) {
        this.inp = inp;
        next = null;
    }

    protected Pair readNext() throws IOException, ParserException {
        readWhiteSpaces();
        if (inp.eof())
            return null;
        char ch = inp.peek();
        if (isRemStart(ch) && skipComment()) {
            return readNext();
        }
        if (ch == '"' || ch == '\'')
            return readString();
        if (isNumberStart(ch))
            return readNumber();
        if (ch == ':')
            return readBindIndex();
        if (isPunctuation(ch))
            return new Pair(Node.PUNCTUATION, new String(new char[]{inp.next()}));
        Pair op = readOp();
        if (op != null)
            return op;
        if (isIdentStart(ch))
            return readIdent();
        inp.choke("Don't know how to parse: " + ch);

        return null;
    }

    private Pair readBindIndex () throws IOException, ParserException {
        String res = inp.next() + readWhile((char ch) -> new String(new char[]{ch}).matches("[0-9]"));

        return new Pair(Node.NUMBER, res);
    }

    private Pair readNumber () throws IOException, ParserException {
        String res = readWhile(
                new Predicate() {
                    boolean hasDot = false;
                    boolean hasExp = false;
                    boolean hasExpSign = false;

                    public boolean test(char ch) {
                        if (ch == '.') {
                            if (hasDot)
                                return false;
                            hasDot = true;
                        } else if (ch == 'E') {
                            if (hasExp)
                                return false;
                            hasExp = true;
                            return true;
                        } else if ((ch == '-' || ch == '+') && hasExp) {
                            if (hasExpSign)
                                return false;
                            hasExpSign = true;
                            return true;
                        }
                        return isNumber(ch);
                    }
                }
        );
        try {
            Integer.parseInt(res);
        } catch (NumberFormatException e) {
            try {
                Double.parseDouble(res);
            } catch (NumberFormatException e1) {
                try {
                    Float.parseFloat(res);
                } catch (NumberFormatException e2) {
                    try {
                        Long.parseLong(res);
                    } catch (NumberFormatException e3) {
                        choke("Expected number. Got: " + res);
                    }
                }
            }
        }
        return new Pair(Node.NUMBER, res);
    }

    private Pair readString () throws IOException {
        boolean esc = false;
        String res = "";
        inp.next();
        while (!inp.eof()) {
            char ch = inp.next();
            if (esc) {
                res += ch;
                esc = false;
            } else if (ch == '\\')
                esc = true;
            else if (ch == '\"' || ch == '\'')
                break;
            else
                res += ch;
        }

        return new Pair(Node.STRING, res);
    }

    private Pair readIdent() throws IOException {
        String ident = readWhile((char ch) -> isIdent(ch));
        return new Pair(
                isKeyword(ident) ? Node.KEYWORD : (
                        isType(ident) ? Node.DTYPE : Node.VARIABLE
                ), ident
        );
    }

    private Pair readOp() throws IOException {
        readWhiteSpaces();
        inp.mark();
        String op = readWhile(
            new Predicate(){
                private boolean hasSymbols = false;
                private boolean first = false;

                public boolean test(char ch) {
                    if (!first) {
                        first = true;
                        hasSymbols = ops.indexOf(ch) >= 0;
                    }
                    return !isWhiteSpace(ch) && (!hasSymbols || ops.indexOf(ch) >= 0);
                }
            }
        );

        if (op.length() == 1 && ops.indexOf(op.charAt(0)) < 0 || op.length() > 1 && !mcOp.contains(op)) {
            inp.reset();
            return null;
        }
        return new Pair(Node.OPERATOR, op);
    }

    private boolean skipComment() throws IOException {
        inp.mark();
        String res = inp.next(4);
        if (res.matches("rem ")) {
            inp.nextLine();
            return true;
        } else
            inp.reset();
        return false;
    }

    private void readWhiteSpaces() throws IOException {
        readWhile((char ch) -> isWhiteSpace(ch));
    }

    private boolean isRemStart(char ch) {
        return new String(new char[]{ch}).matches("[rR]");
    }

    private boolean isNumberStart(char ch) {
        return new String(new char[]{ch}).matches("[0-9.]");
    }

    private boolean isNumber(char ch) {
        return isNumberStart(ch) || !new String(new char[]{ch}).matches("[" + ops + " \n\r\t]");
    }

    private boolean isWhiteSpace(char ch) {
        return new String(new char[]{ch}).matches("[ \n\r\t]");
    }

    private boolean isKeyword(String str) {
        return kw.contains(str.toLowerCase());
    }

    private boolean isType(String str) {
        return tp.contains(str.toLowerCase());
    }

    private boolean isPunctuation(char ch) {
        return punc.indexOf(ch) >= 0;
    }

    private boolean isIdentStart(char ch) {
        return new String(new char[]{ch}).matches("[a-zA-Z_]");
    }

    private boolean isIdent(char ch) {
        return isIdentStart(ch) || new String(new char[]{ch}).matches("[a-zA-Z_0-9]");
    }

    private String readWhile(Predicate cond) throws IOException {
        String res = new String();
        while (!inp.eof() && cond.test(inp.peek()))
            res += inp.next();

        return res;
    }

    protected Pair next() throws IOException, ParserException {
        Pair n;
        if (next != null)
            n = next;
        else
            n = readNext();
        next = null;
        last = n;
        return n;
    }

    protected Pair last() throws IOException, ParserException {
        return last;
    }

    protected Pair peek() throws IOException, ParserException {
        if (next == null) {
            next = readNext();
        }
        return next;
    }

    protected boolean eof () throws IOException {
        readWhiteSpaces();
        return inp.eof();
    }

    protected void choke (String msg) throws IOException, ParserException {
        readWhiteSpaces();
        inp.choke(msg);
    }

    interface Predicate {
        boolean test(char t);
    }
}
