package com.university.labs.Lab1;

import lombok.Data;

@Data
public class Lexeme {
    private final LexemeClass lexemeClass;
    private final LexemeType lexemeType;
    private final String value;
    private final int position;
}
