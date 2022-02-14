package com.university.labs.Lab3;

import lombok.Data;

@Data
public class PostfixEntry {
    private EEntryType type;
    private int index;
    private ECmd cmd;
    private String value;
    private int cmdPtr;
    private int curValue;
}
