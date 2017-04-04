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
 * Created by vlad on 4/3/2017.
 */
public class LoadUnit {
    private Node target;
    private WhenNode when;
    private SchemaNode schema;

    LoadUnit (Node target, WhenNode when, SchemaNode schema) {
        this.target = target;
        this.when = when;
        this.schema = schema;
    }

    protected Node getTarget() {
        return target;
    }

    protected WhenNode getWhen () {
        return when;
    }

    protected SchemaNode getSchema () {
        return schema;
    }
}
