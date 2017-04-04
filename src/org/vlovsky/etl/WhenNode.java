package org.vlovsky.etl;

/**
 * Created by vlad on 4/3/2017.
 */
public class WhenNode extends Node {
    private Node cond;

    WhenNode (Node condition) {
        super(null, null);
        cond = condition;
    }

    protected Node getCondition () {
        return cond;
    }
}
