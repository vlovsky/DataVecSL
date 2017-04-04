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

import java.util.Set;

/**
 * Created by vlad on 4/3/2017.
 */
public class ConditionEvaluator {

    protected static Set<String> getEnumerations(BinNode cond, String varName) {
        return getEnumerations(cond, varName, null);
    }

    protected static Set<String> getEnumerations(BinNode cond, String varName, Set<String> curVal) {
        if (varName == null || cond == null)
            throw new IllegalArgumentException("Condition and name cannot be null.");

        Set<String> res = curVal;
        Node ln = cond.getLeft();
        Node rn = cond.getRight();

        if (ln instanceof BinNode)
            res = getEnumerations((BinNode) ln, varName, res);

        if (rn instanceof BinNode)
            res = getEnumerations((BinNode) rn, varName, res);

        String op = cond.getOperator();
        Set<String> testVal;
        if ("in".equals(op) && varName.equals(ln.getValue()) && Node.VARIABLE.equals(ln.getType()))
            testVal = ((EnumType) rn).getEnumsAsStrings();
        else
            return res;

        if (res != null)
            testVal.retainAll(res);
        return testVal;
    }

    protected static Integer maxIntegerValue (BinNode cond, String varName) {
        return minMaxIntegerValue (cond, varName, null, false);
    }

    protected static Integer minIntegerValue (BinNode cond, String varName) {
        return minMaxIntegerValue (cond, varName, null, true);
    }

    protected static Integer minMaxIntegerValue (BinNode cond, String varName, Integer curVal, boolean min) {
        if (varName == null || cond == null)
            throw new IllegalArgumentException("Condition and name cannot be null.");

        Integer res = curVal;
        Node ln = cond.getLeft();
        Node rn = cond.getRight();

        if (ln instanceof BinNode)
            res = minMaxIntegerValue((BinNode) ln, varName, res, min);

        if (rn instanceof BinNode)
            res = minMaxIntegerValue((BinNode) rn, varName, res, min);

        String op = cond.getOperator();
        String evalOperator1;
        String evalOperator2;
        String evalOperator3;
        String evalOperator4;
        if (min) {
            evalOperator1 = ">="; evalOperator2 = ">"; evalOperator3 = "<="; evalOperator4 = "<";
        } else {
            evalOperator1 = "<="; evalOperator2 = "<"; evalOperator3 = ">="; evalOperator4 = ">";
        }

        Integer testVal;
        if (evalOperator1.equals(op) && varName.equals(ln.getValue()) && Node.VARIABLE.equals(ln.getType()))
                testVal = Integer.parseInt((String)rn.getValue());
        else if (evalOperator3.equals(op) && varName.equals(rn.getValue()) && Node.VARIABLE.equals(rn.getType()))
                testVal = Integer.parseInt((String)ln.getValue());
        else if (evalOperator2.equals(op) && varName.equals(ln.getValue()) && Node.VARIABLE.equals(ln.getType()))
                testVal = Integer.parseInt((String)rn.getValue()) + (min ? 1 : -1);
        else if (evalOperator4.equals(op) && varName.equals(rn.getValue()) && Node.VARIABLE.equals(rn.getType()))
                testVal = Integer.parseInt((String)ln.getValue()) + (min ? 1 : -1);
        else
            return res;

        if (res == null)
            return testVal;
        if (min)
            return Math.max(res, testVal);
        else
            return Math.min(res, testVal);
    }

    protected static Double maxDoubleValue (BinNode cond, String varName) {
        return minMaxDoubleValue (cond, varName, null, false);
    }

    protected static Double minDoubleValue (BinNode cond, String varName) {
        return minMaxDoubleValue (cond, varName, null, true);
    }

    protected static Double minMaxDoubleValue (BinNode cond, String varName, Double curVal, boolean min) {
        if (varName == null || cond == null)
            throw new IllegalArgumentException("Condition and name cannot be null.");

        Double res = curVal;
        Node ln = cond.getLeft();
        Node rn = cond.getRight();

        if (ln instanceof BinNode)
            res = minMaxDoubleValue((BinNode) ln, varName, res, min);

        if (rn instanceof BinNode)
            res = minMaxDoubleValue((BinNode) rn, varName, res, min);

        String op = cond.getOperator();
        String evalOperator1;
        String evalOperator2;
        String evalOperator3;
        String evalOperator4;
        if (min) {
            evalOperator1 = ">="; evalOperator2 = ">"; evalOperator3 = "<="; evalOperator4 = "<";
        } else {
            evalOperator1 = "<="; evalOperator2 = "<"; evalOperator3 = ">="; evalOperator4 = ">";
        }

        Double testVal;
        if (evalOperator1.equals(op) && varName.equals(ln.getValue()) && Node.VARIABLE.equals(ln.getType()))
                testVal = Double.parseDouble((String)rn.getValue());
        else if (evalOperator3.equals(op) && varName.equals(rn.getValue()) && Node.VARIABLE.equals(rn.getType()))
                testVal = Double.parseDouble((String)ln.getValue());
        else if (evalOperator2.equals(op) && varName.equals(ln.getValue()) && Node.VARIABLE.equals(ln.getType()))
                testVal = Double.parseDouble((String)rn.getValue()) + (min ? Double.MIN_NORMAL : -Double.MIN_NORMAL);
        else if (evalOperator4.equals(op) && varName.equals(rn.getValue()) && Node.VARIABLE.equals(rn.getType()))
                testVal = Double.parseDouble((String)ln.getValue()) + (min ? Double.MIN_NORMAL : -Double.MIN_NORMAL);
        else
            return res;

        if (res == null)
            return testVal;
        if (min)
            return Math.max(res, testVal);
        else
            return Math.min(res, testVal);
    }

    protected static Float maxFloatValue (BinNode cond, String varName) {
        return minMaxFloatValue (cond, varName, null, false);
    }

    protected static Float minFloatValue (BinNode cond, String varName) {
        return minMaxFloatValue (cond, varName, null, true);
    }

    protected static Float minMaxFloatValue (BinNode cond, String varName, Float curVal, boolean min) {
        if (varName == null || cond == null)
            throw new IllegalArgumentException("Condition and name cannot be null.");

        Float res = curVal;
        Node ln = cond.getLeft();
        Node rn = cond.getRight();

        if (ln instanceof BinNode)
            res = minMaxFloatValue((BinNode) ln, varName, res, min);

        if (rn instanceof BinNode)
            res = minMaxFloatValue((BinNode) rn, varName, res, min);

        String op = cond.getOperator();
        String evalOperator1;
        String evalOperator2;
        String evalOperator3;
        String evalOperator4;
        if (min) {
            evalOperator1 = ">="; evalOperator2 = ">"; evalOperator3 = "<="; evalOperator4 = "<";
        } else {
            evalOperator1 = "<="; evalOperator2 = "<"; evalOperator3 = ">="; evalOperator4 = ">";
        }

        Float testVal;
        if (evalOperator1.equals(op) && varName.equals(ln.getValue()) && Node.VARIABLE.equals(ln.getType()))
            testVal = Float.parseFloat((String)rn.getValue());
        else if (evalOperator3.equals(op) && varName.equals(rn.getValue()) && Node.VARIABLE.equals(rn.getType()))
            testVal = Float.parseFloat((String)ln.getValue());
        else if (evalOperator2.equals(op) && varName.equals(ln.getValue()) && Node.VARIABLE.equals(ln.getType()))
            testVal = Float.parseFloat((String)rn.getValue()) + (min ? Float.MIN_NORMAL : -Float.MIN_NORMAL);
        else if (evalOperator4.equals(op) && varName.equals(rn.getValue()) && Node.VARIABLE.equals(rn.getType()))
            testVal = Float.parseFloat((String)ln.getValue()) + (min ? Float.MIN_NORMAL : -Float.MIN_NORMAL);
        else
            return res;

        if (res == null)
            return testVal;
        if (min)
            return Math.max(res, testVal);
        else
            return Math.min(res, testVal);
    }

    protected static Double evaluateDouble (Node exp) {
        return Double.parseDouble((String) exp.getValue());
    }
}
