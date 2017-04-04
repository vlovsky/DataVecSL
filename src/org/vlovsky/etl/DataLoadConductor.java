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

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.condition.ConditionOp;
import org.datavec.api.transform.condition.column.CategoricalColumnCondition;
import org.datavec.api.transform.condition.column.DoubleColumnCondition;
import org.datavec.api.transform.filter.ConditionFilter;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.writable.DoubleWritable;
import org.datavec.api.writable.Writable;
import org.datavec.spark.transform.SparkTransformExecutor;
import org.datavec.spark.transform.misc.StringToWritablesFunction;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by vlad on 4/3/2017.
 */
public class DataLoadConductor {
    private Parser parser;
    List<Node> ast;
    String[] args;
    JavaRDD<List<Writable>> rData;
    JavaRDD<String> sData;

    public DataLoadConductor (String inp, String... args) throws IOException {
        this(new ByteArrayInputStream(inp.getBytes()), args);
    }

    public DataLoadConductor (java.io.InputStream inp, String... args) throws IOException {
        parser = new Parser(inp);
        this.args = args;
    }

    public JavaRDD<List<Writable>> getLoadedData() {
        return rData;
    }

    public JavaRDD<String> getSourceData() {
        return sData;
    }

    public void execute(JavaSparkContext spCtx) throws IOException, ParserException {
        ast = parser.parse();
        bindParameters();
        Map<String, ParamNode> loadParams = ((LoadDataNode)ast.get(0)).getParams();

        TransformProcess xformProc = mapTransform(mapSchema());

        String directory = new File(loadParams.get("from").getValueAsString()).getParent();
        sData = spCtx.textFile(directory);

        RecordReader recReader;
        if (loadParams.get("field terminator") != null && loadParams.get("field terminator").getValueAsString().equals(","))
            recReader = new CSVRecordReader();
        else
            throw new IllegalArgumentException("Only CSV file format is supported.");

        JavaRDD<List<Writable>> parsedInputData = sData.map(new StringToWritablesFunction(recReader));

        rData = SparkTransformExecutor.execute(parsedInputData, xformProc);
    }

    private void bindParameters() {
        Map<String, ParamNode> loadParams = ((LoadDataNode)ast.get(0)).getParams();
        ParamNode param = loadParams.get("from");
        String paramVal = param != null ? param.getValueAsString() : null;
        if (param != null && paramVal.matches(":[1-9]+")) {
            int argIdx = Integer.parseInt(paramVal.replaceFirst(":","")) - 1;
            param.setValue(args[argIdx]);
        }
    }

    private Schema mapSchema() throws IOException, ParserException {
        SchemaNode sch = ((LoadDataNode)ast.get(0)).getLoadUnits().get(0).getSchema();
        BinNode whenCond = (BinNode) ((LoadDataNode)ast.get(0)).getLoadUnits().get(0).getWhen().getCondition();
        Schema.Builder schBuilder = new Schema.Builder();
        for (FieldNode fld : sch.getValue() ) {
            if (fld.getFieldType() instanceof EnumType) {
                List<String> enumsL = new ArrayList<>(((EnumType) fld.getFieldType()).getEnumsAsStrings());
                schBuilder.addColumnCategorical(fld.getName(), enumsL);
            } else if (fld.getFieldType().getBaseType().equals("string")) {
                Set<String> enums = ConditionEvaluator.getEnumerations(whenCond, fld.getName());
                if (enums != null) {
                    List<String> enumsL = new ArrayList<>(enums);
                    schBuilder.addColumnCategorical(fld.getName(), enumsL);
                } else {
                    schBuilder.addColumnString(fld.getName());
                }
            } else if (fld.getFieldType().getBaseType().equals("integer")) {
                schBuilder.addColumnInteger(fld.getName(),
                        ConditionEvaluator.minIntegerValue(whenCond, fld.getName()),
                        ConditionEvaluator.maxIntegerValue(whenCond, fld.getName()));
            } else if (fld.getFieldType().getBaseType().equals("double")) {
                schBuilder.addColumnDouble(fld.getName(),
                        ConditionEvaluator.minDoubleValue(whenCond, fld.getName()),
                        ConditionEvaluator.maxDoubleValue(whenCond, fld.getName()),
                        false, false);
            } else if (fld.getFieldType().getBaseType().equals("float")) {
                schBuilder.addColumnFloat(fld.getName());
            }
        }

        return schBuilder.build();
    }

    private TransformProcess mapTransform (Schema dvcSch) {
        SchemaNode sch = ((LoadDataNode)ast.get(0)).getLoadUnits().get(0).getSchema();
        BinNode whenCond = (BinNode) ((LoadDataNode)ast.get(0)).getLoadUnits().get(0).getWhen().getCondition();
        TransformProcess.Builder xformBuilder = new TransformProcess.Builder(dvcSch);

        for (FieldNode fld : sch.getValue() ) {
            // transformation of values
            Node modNode = fld.getModeExp();
            if (modNode != null) {
                if (modNode instanceof IfNode) {
                    IfNode ifNode = (IfNode) modNode;
                    BinNode cond = ifNode.getCond();
                    ConditionOp condOp;
                    String condVal;
                    boolean revCond = false;
                    Node var;
                    if (!cond.getLeft().getType().equals(Node.VARIABLE)) {
                        revCond = true;
                        condVal = (String) cond.getLeft().getValue();
                        var = cond.getRight();
                    } else {
                        condVal = (String) cond.getRight().getValue();
                        var = cond.getLeft();
                    }

                    if (cond.getOperator().equals("<") || cond.getOperator().equals(">") && revCond)
                        condOp = ConditionOp.LessThan;
                    else if (cond.getOperator().equals("<=") || cond.getOperator().equals(">=") && revCond)
                        condOp = ConditionOp.LessOrEqual;
                    else if (cond.getOperator().equals(">") || cond.getOperator().equals("<") && revCond)
                        condOp = ConditionOp.GreaterThan;
                    else if (cond.getOperator().equals(">=") || cond.getOperator().equals("<=") && revCond)
                        condOp = ConditionOp.GreaterOrEqual;
                    else if (cond.getOperator().equals("=="))
                        condOp = ConditionOp.Equal;
                    else if (cond.getOperator().equals("!="))
                        condOp = ConditionOp.NotEqual;
                    else
                        throw new IllegalArgumentException("Unexpected operator in condition: " + cond);

                    if (fld.getFieldType().getBaseType().equals("double"))
                    xformBuilder.conditionalReplaceValueTransform(
                            fld.getName(),
                            new DoubleWritable(ConditionEvaluator.evaluateDouble(ifNode.getThen())),
                            new DoubleColumnCondition((String)var.getValue(), condOp, Double.parseDouble(condVal)));
                } else {
                    throw new IllegalArgumentException("Unsupported modification expression: " + modNode);
                }
            }

            // filtering of records
            if (fld.getFieldType().getBaseType().equals("string")) {
                Set<String> enums = ConditionEvaluator.getEnumerations(whenCond, fld.getName());
                if (enums != null) {
                    xformBuilder.filter(new ConditionFilter(
                            new CategoricalColumnCondition(fld.getName(), ConditionOp.NotInSet, enums)));
                }
            } else if (fld.getFieldType() instanceof EnumType) {
                Set<String> enums = ConditionEvaluator.getEnumerations(whenCond, fld.getName(),
                        ((EnumType) fld.getFieldType()).getEnumsAsStrings());
                xformBuilder.filter(new ConditionFilter(
                        new CategoricalColumnCondition(fld.getName(), ConditionOp.NotInSet, enums)));
            }
        }

        //Finally, let's suppose we want to parse our date/time column in a format like "2016/01/01 17:50.000"
        //We use JodaTime internally, so formats can be specified as follows: http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
//        xformBuilder.stringToTimeTransform("DateTimeString","YYYY-MM-DD HH:mm:ss.SSS", DateTimeZone.UTC)

        //At this point, we have our date/time format stored internally as a long value (Unix/Epoch format): milliseconds since 00:00.000 01/01/1970
        //Suppose we only care about the hour of the day. Let's derive a new column for that, from the DateTime column
//        .transform(new DeriveColumnsFromTimeTransform.Builder("DateTimeString")
//                .addIntegerDerivedColumn("HourOfDay", DateTimeFieldType.hourOfDay())
//                .build());

        for (FieldNode fld : sch.getValue() ) {
            // removal of fillers
            if (fld.isFiller())
                xformBuilder.removeColumns(fld.getName());
        }

        return xformBuilder.build();
    }

    public void printAST() throws IOException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationConfig.Feature.INDENT_OUTPUT);
        for (Node n : ast) {
            Object json = mapper.readValue(n.toString(), Object.class);
            String indented = mapper.defaultPrettyPrintingWriter().writeValueAsString(json);
            System.out.println(indented);
        }
    }
}
