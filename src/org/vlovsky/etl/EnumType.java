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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vlad on 4/1/2017.
 */
class EnumType extends FieldType {
    private Set<Node> enums;

    protected EnumType(List<Node> enums) {
        if (enums.size() > 0 && Node.STRING.equals(enums.get(0).getType())) {
            setBaseType(Node.STRING);
        } else
            throw new IllegalArgumentException("Enumeration must contain at least one literal.");
        this.enums = new HashSet();
        this.enums.addAll(enums);
    }

    protected Set<Node> getEnums () {
        return enums;
    }

    protected Set<String> getEnumsAsStrings () {
        Set<String> res = new HashSet();
        for (Node n : enums) {
            res.add(n.getValue().toString());
        }
        return res;
    }
}
