package com.university.labs.Lab3;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostfixEntry {
    private EEntryType type;
    private int index;
    private ECmd cmd;
    private String value;
    private int cmdPtr;
    private int curValue;

    @Override
    public String toString() {
        if (cmd != null)
            return cmd.toString();
        else if (value != null)
            return value;
        else return String.valueOf(cmdPtr);
    }
}
