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
public class BinNode extends Node {
    private String operator;
    private Node left;
    private Node right;

    protected BinNode(String type, String operator, Node left, Node right) {
        super(type, left.getValue() + " " + operator + " " + right.getValue());
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    protected Node getLeft () {
        return left;
    }

    protected Node getRight () {
        return right;
    }

    protected String getOperator () {
        return operator;
    }
}
