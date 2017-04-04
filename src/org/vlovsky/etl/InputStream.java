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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by vlad on 3/31/2017.
 */
public class InputStream {
    private int ln = -1, pos = 0, markPos;
    private String line;
    private BufferedReader inp;

    public InputStream(java.io.InputStream inp) throws IOException {
        this.inp = new BufferedReader(new InputStreamReader(inp));
        nextLine();
    }

    protected void nextLine() throws IOException {
        line = inp.readLine() + "\n";
        pos = 0;
        markPos = 0;
        ln++;
    }

    protected char peek() throws IOException {
        if (pos >= line.length())
            nextLine();

        if (line == null)
            throw new IOException("EOF");

        return line.charAt(pos);
    }

    protected char next() throws IOException {
        if (pos >= line.length())
            nextLine();

        return line.charAt(pos++);
    }

    protected String next(int numChars) throws IOException {
        String res = line.substring(pos, pos + numChars > line.length() ? line.length() : pos + numChars);
        pos += numChars;
        return res;
    }

    protected boolean eof() throws IOException {
        if ((line == null || pos >= line.length()) && inp.ready())
            nextLine();
        else if (line != null && pos >= line.length())
            line = null;

        return line == null;
    }

    protected void mark() {
        markPos = pos;
    }

    protected void reset() {
        pos = markPos;
    }

    protected void choke(String msg) throws ParserException {
        String scr = "\n" + line + "\n";

        for (int i = 0; i < pos; i++) {
            scr += "-";
        }
        scr += "^";

        throw new ParserException(msg + " at line: " + ln + ", position: " + pos + scr);
    }
}
