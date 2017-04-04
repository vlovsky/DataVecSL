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

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vlad on 3/31/2017.
 */
public class Parser {
    private Lexer lex;
    private static Map<String, Integer> prec;
    private boolean hasInto;

    static {
        prec = new HashMap();
        prec.put("*", 4);
        prec.put("/", 4);
        prec.put("%", 4);
        prec.put("+", 5);
        prec.put("-", 5);
        prec.put("<", 7);
        prec.put(">", 7);
        prec.put("<=", 7);
        prec.put(">=", 7);
        prec.put("==", 8);
        prec.put("!=", 8);
        prec.put("<>", 8);
        prec.put("in", 8);
        prec.put("&&", 12);
        prec.put("and", 12);
        prec.put("||", 13);
        prec.put("or", 12);
        prec.put("=", 15);
    }

    public Parser (java.io.InputStream inp) throws IOException {
        lex = new Lexer(new InputStream(inp));
    }

    public List<Node> parse() throws IOException, ParserException {
        List<Node> script = new ArrayList();
        while (!lex.eof()) {
            script.add(parseExpression());
            skipPunctuation(";");
        }

        return script;
    }

    private Node parseCallFunc(Node n) throws IOException, ParserException {
        return new FuncCallNode(
            (String) n.getValue(),
            delimited("(", ")", ",", () -> parseExpression()));
    }

    private Node tryCallFunc(Node expr) throws IOException, ParserException {
        return lex.last() != null
                && lex.last().type.equals(Node.VARIABLE)
                && isPunctuation("(") != null ?
                parseCallFunc(expr) :
                expr;
    }

    private List<Node> delimited(String start, String stop, String separator, TrailingParser parser) throws IOException, ParserException {
        List<Node> a = new ArrayList();
        boolean first = true;
        skipPunctuation(start);
        while (!lex.eof()) {
            if (isPunctuation(stop) != null)
                break;
            if (first)
                first = false;
            else
                skipPunctuation(separator);
            if (isPunctuation(stop) != null) break;
            a.add(parser.parse());
        }
        skipPunctuation(stop);
        return a;
    }

    private Node tryBinary(Node left, int thisPrec) throws IOException, ParserException {
        Node n = isOperator(null);
        if (n != null) {
            int otherPrec = this.prec.get(n.getValue());
            if (otherPrec < thisPrec) {
                lex.next();
                return tryBinary(new BinNode (
                        "=".equals(n.getValue()) ? Node.ASSIGNMENT : Node.BINARY, // type
                        (String) n.getValue(),                                    // operator
                        left,                                               // left
                        tryBinary(parseUnit(), otherPrec)                 // right
                ), thisPrec);
            }
        }
        return left;
    }

    private Node parseExpression() throws IOException, ParserException {
        return tryCallFunc(tryBinary(parseUnit(), 100));
    }

    public interface TrailingParser {
        Node parse() throws IOException, ParserException;
    }

    private Node parseIf() throws IOException, ParserException {
        skipKeyword("if");
        Node cond = parseExpression();
        if (isPunctuation("{") == null) skipKeyword("then");
        Node then = parseExpression();
        IfNode ret = new IfNode ("if", cond, then, null);
        if (isKeyword("else") != null) {
            lex.next();
            ret.setAlt(parseExpression());
        }
        return ret;
    }

    private Node parseIn() throws IOException, ParserException {
        skipKeyword("in");
        return parseEnum();
    }

    private Node parseBool() throws IOException, ParserException {
        return new Node ("bool", lex.next().value);
    }

    private EnumType parseEnum() throws IOException, ParserException {
        skipKeyword("enum");
        return new EnumType(delimited("(", ")", ",", () -> parseExpression()));
    }

    private Node parseField() throws IOException, ParserException {
        Pair name = lex.next();
        if (!name.type.equals("var"))
            lex.choke("Expecting variable name");
        Node fieldType = parseExpression();
        if (!(fieldType instanceof FieldType)) {
            lex.choke("Expected data type. Got: \"" + fieldType.getValue() + "\"");
        }
        Node modExp = null;
        boolean isFiller = false;
        while (!lex.peek().value.equals(",") && !lex.peek().value.equals(")")) {
            Node exp = parseExpression();
            if (exp.getValue().equals("filler") && !isFiller)
                isFiller = true;
            else if (exp instanceof IfNode || exp instanceof FuncCallNode)
                modExp = exp;
            else
                choke(modExp);
        }
        return new FieldNode(name.value, (FieldType) fieldType, isFiller, modExp);
    }

    private ParamNode parseParam(String name) throws IOException, ParserException {
        return parseParam(name, false, true);
    }

    private ParamNode parseParam(String name, boolean optional, boolean hasArgs) throws IOException, ParserException {
        Pair param = lex.next();
        if ("field terminator".equals(name) && param.value.equals("fields"))
            return new ParamNode(name, hasArgs ? parseFieldTerminator() : null);
        else {
            if (!param.type.equals(Node.KEYWORD) || !optional && !param.value.equals(name))
                lex.choke("Expecting keyword: " + name);
            return new ParamNode(name, hasArgs ? parseExpression() : null);
        }
    }

    private Node parseFieldTerminator() throws IOException, ParserException {
        skipKeyword("terminated");
        skipKeyword("by");
        return parseExpression();
    }

    private LoadUnit parseInto() throws IOException, ParserException {
        if (!hasInto) {
            skipKeyword("into");
            hasInto = true;
        } else if (isKeyword("into") != null)
            skipKeyword("into");
        else
            return null;

        return new LoadUnit(
                parseExpression(),
                parseWhen(),
                parseSchema()
        );
    }

    private WhenNode parseWhen() throws IOException, ParserException {
        if (isKeyword("when") != null)
            skipKeyword("when");
        return new WhenNode(parseExpression());
    }

    private SchemaNode parseSchema() throws IOException, ParserException {
        skipKeyword("as");
        return new SchemaNode(delimited("(", ")", ",", () -> parseField()));
    }

    private Node parseLoadData() throws IOException, ParserException {
        skipKeyword("data");
        List<ParamNode> params = new ArrayList<>();
        params.add(parseParam("from"));
        params.add(parseParam("field terminator", true, true));
        LoadDataNode ld = new LoadDataNode(params);
        ld.addLoadUnit(parseInto());
        while(isPunctuation(";") == null) {
            ld.addLoadUnit(parseInto());
        }
        return ld;
    }

    private Node parseUnit() throws IOException, ParserException {
        Node unit = null;
        if (isPunctuation("(") != null) {
            lex.next();
            Node exp = parseExpression();
            skipPunctuation(")");
            unit = exp;
        }
        else if (isKeyword("if") != null)
            unit = parseIf();
        else if (isKeyword("true") != null || isKeyword("false") != null)
            unit = parseBool();
        else if (isKeyword("load") != null) {
            lex.next();
            unit = parseLoadData();
        }
        else if (isKeyword("enum") != null)
            unit = parseEnum();
        else if (isKeyword("in") != null)
            unit = parseIn();
        else if (isKeyword("fields") != null)
            unit = parseFieldTerminator();
        else {
            Pair tok = lex.next();
            if (tok.type == Node.VARIABLE || tok.type == Node.NUMBER || tok.type == Node.STRING || tok.type == Node.KEYWORD)
                unit = new Node(tok.type, tok.value);
            else if (tok.type == Node.DTYPE)
                unit = new FieldType(tok.value);
            else choke();
        }
        return tryCallFunc(unit);
    }

    private boolean skipKeyword (String tok) throws IOException, ParserException {
        return skipKeyword(tok, false);
    }

    private boolean skipKeyword (String tok, boolean optional) throws IOException, ParserException {
        if (isKeyword(tok) != null) {
            lex.next();
            return true;
        } else if (!optional)
            lex.choke("Expected keyword \"" + tok + "\". Got: " + lex.peek().value);
        return false;
    }

    private void skipPunctuation (String tok) throws IOException, ParserException {
        if (isPunctuation(tok) != null)
            lex.next();
        else
            lex.choke("Expected punctuation: \"" + tok + "\". Got: \"" + (lex.peek() != null ? lex.peek().value : "EOF") + "\"");
    }

    private Node isPunctuation (String tok) throws IOException, ParserException {
        Pair n = lex.peek();
        if ( n != null && n.type == Node.PUNCTUATION && (tok == null || n.value.equals(tok)))
            return new Node(n.type, n.value);
        else
            return null;
    }

    private Node isOperator (String tok) throws IOException, ParserException {
        Pair n = lex.peek();
        if ( n != null && n.type == Node.OPERATOR && (tok == null || n.value.equals(tok)))
            return new Node(n.type, n.value);
        else
            return null;
    }

    private Node isKeyword (String tok) throws IOException, ParserException {
        Pair n = lex.peek();
        if ( n != null && n.type == Node.KEYWORD && (tok == null || n.value.equals(tok)))
            return new Node(n.type, n.value);
        else
            return null;
    }

    private void choke() throws IOException, ParserException {
        lex.choke("Unexpected token: \"" + lex.peek() + "\"");
    }

    private void choke(Node exp) throws IOException, ParserException {
        lex.choke("Unexpected token: \"" + exp.getValue() + "\"");
    }
}
