/*
 * Copyright 2025 Neo Tsai
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.example.cpb_test;

public enum ConstraintType {
    POSITIVE,   // 必須至少出現在某條測試路徑中
    ONCE,       // 僅能出現一次
    NEGATIVE,   // 不可出現
    MAX_ONCE    // 最多出現一次
}