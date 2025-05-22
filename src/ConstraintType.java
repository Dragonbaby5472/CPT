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

/**
 * Defines the kinds of constraints that can be applied to vertices when generating test paths.
 *
 * <p>Each enum constant specifies how often—or whether—a given vertex must appear in a
 * test path:
 * <ul>
 *   <li>{@link #POSITIVE}   – vertex must appear in at least one test path.</li>
 *   <li>{@link #ONCE}       – vertex must appear exactly once in a single test path.</li>
 *   <li>{@link #NEGATIVE}   – vertex must not appear in any test path.</li>
 *   <li>{@link #MAX_ONCE}   – vertex may appear at most once in a test path.</li>
 * </ul>
 * </p>
 */
public enum ConstraintType {
    POSITIVE, 
    ONCE, 
    NEGATIVE, 
    MAX_ONCE 
}