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

/**
 * Created by vlad on 4/1/2017.
 */
public class Node {
    public static String PUNCTUATION = "pnc";
    public static String NUMBER = "num";
    public static String KEYWORD = "kw";
    public static String VARIABLE = "var";
    public static String STRING = "str";
    public static String OPERATOR = "op";
    public static String ASSIGNMENT = "assig";
    public static String BINARY = "bin";
    public static String FUNC = "func";
    public static String DTYPE = "dtp";
    public static String BOOL = "bool";

    private String type;
    private Object value = new String("NOVAL");

    Node (String type, Object value) {
        this.type = type;
        if (value != null)
            this.value = value;
    }

    protected String getType() {
        return type;
    }

    protected Object getValue() {
        return value;
    }

    protected void setValue(Object newVal) {
        this.value = newVal;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
