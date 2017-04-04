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
class IfNode extends Node {
    private BinNode cond;
    private Node then;
    private Node alt;

    IfNode(String type, Node cond, Node then, Node alt) {
        super(type, "if " + cond.getValue() + " then " + then.getValue() + (alt != null ? " else " + alt.getValue() : ""));
        this.cond = (BinNode) cond;
        this.then = then;
        this.alt = alt;
    }

    protected void setAlt(Node altNode) {
        alt = altNode;
        setValue(getValue() + " else " + alt.getValue());
    }

    protected BinNode getCond () {
        return cond;
    }

    protected Node getThen () {
        return then;
    }

    protected Node getAlt () {
        return alt;
    }
}
