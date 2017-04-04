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

import java.util.List;

/**
 * Created by vlad on 3/31/2017.
 */
public class FuncCallNode extends Node {
    private List<Node> args;
    private String name;

    protected FuncCallNode(String name, List<Node> args) {
        super(Node.FUNC, null);
        this.args = args;
        this.name = name;
    }

    protected List<Node> getArgs() {
        return args;
    }

    protected String getFuncName() {
        return name;
    }

    @Override
    public String toString() {
        String st = (name != null ? name : "") + "(";
        boolean first = true;
        for (Node n : args) {
            if (first) {
                first = false;
                st += n.toString();
            } else
                st += ", " + n.toString();
        }

        return st + ")";
    }
}
