/*
 * Copyright (c) 2025 Neo Tsai
 * All rights reserved.
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