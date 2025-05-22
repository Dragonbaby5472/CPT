/*
 * Copyright 2025 Neo Tsai
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.cpb_test;

public class Constraint<V> {
    private final V from;          
    private final V to;            
    private final ConstraintType type;

    public Constraint(V from, V to, ConstraintType type) {
        this.from = from;
        this.to   = to;
        this.type = type;
    }

    public V getFrom() {
        return from;
    }

    public V getTo() {
        return to;
    }

    public ConstraintType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("[%s → %s : %s]", from, to, type);
    }
}