/*
 * Copyright (c) 2025 Neo Tsai
 * All rights reserved.
 */

package com.example.cpb_test;

public enum ConstraintType {
    POSITIVE,   // 必須至少出現在某條測試路徑中
    ONCE,       // 僅能出現一次
    NEGATIVE,   // 不可出現
    MAX_ONCE    // 最多出現一次
}