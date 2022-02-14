package com.university.labs.Lab1;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class LexAnalyser {
    public static String inputString;
    public static int numOfStates;
    public static int [][] transFunc;
    public static int startState;
    public static int [] endStates;
    public static List<String> keywords = new ArrayList<>();
    public static Map<Integer, String> stateMap = new HashMap<>();
    public static List<Lexeme> lexemes = new ArrayList<>();
    public static boolean checkCase;

    private static void loadKeywords() throws IOException {
        Path path = Paths.get("src/main/resources/lab1_resources/keywords.txt");
        BufferedReader reader = Files.newBufferedReader(path);

        while (reader.ready()){
            keywords.add(reader.readLine());
        }
    }

    private static void loadStateMap(){
        stateMap.put(0, "Start");
        stateMap.put(1, "Identifier");
        stateMap.put(2, "Constant");
        stateMap.put(3, "Arithmetic");
        stateMap.put(4, "LessComparison");
        stateMap.put(5, "Assignment");
        stateMap.put(6, "MoreOrEqualComparison");
        stateMap.put(7, "Equality");
        stateMap.put(8, "Final");
    }

    public static void loadAutomaton(){
        try {
            loadKeywords();
            loadStateMap();
            prepareData();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        loadAutomaton();
        System.out.println("\nВведите строку на вход автомата: ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        inputString = input;
        startAutomate(input + " ");
    }

    public static void startAutomate(String input){
        int previousState = startState;
        String [] inputArray = input.split("");
        StringBuilder lexeme = new StringBuilder();
        int lexemeIndex = 0;
        int movingIndex = 0;

        for (String symbol : inputArray){
            movingIndex++;
            int index = getTableIndex(symbol);

            int nextState = transFunc[previousState][index];
            if (nextState == 0 && (previousState == 0 || previousState == 8)) {
                lexemeIndex++;
                continue;
            }

            if (nextState == 8){
                getLexemeInfo(previousState, lexeme.toString(), lexemeIndex);
                lexemeIndex = movingIndex;
                lexeme.setLength(0);
                if (index != 9)
                    lexeme.append(symbol);

                if (transFunc[nextState][index] != 0){
                    previousState = transFunc[nextState][index];
                } else{
                    previousState = nextState;
                }

            } else{
                previousState = nextState;
                lexeme.append(symbol);
            }
        }

        System.out.println();
        //printLexemes();
    }

    public static int getTableIndex(String symbol){
        if (Character.isDigit(symbol.charAt(0)))
            return 1;

        if (symbol.charAt(0) >= 65 && symbol.charAt(0) <= 122)
            return 0;

        return switch (symbol) {
            case "+" -> 2;
            case "-" -> 3;
            case "<" -> 4;
            case ">" -> 5;
            case "=" -> 6;
            case "*" -> 7;
            case "/" -> 8;
            default -> 9;
        };
    }

    public static void getLexemeInfo(int previousState, String lexeme, int position){
        String state = stateMap.get(previousState);
        LexemeType lexemeType = null;
        LexemeClass lexemeClass = null;
        switch (state) {
            case "Identifier":
                String lexCheckCase = checkCase ? lexeme : lexeme.toLowerCase(Locale.ROOT);
                if (keywords.contains(lexCheckCase)) {
                    lexemeClass = LexemeClass.KEYWORD;
                    for (LexemeType lex : LexemeType.values()) {
                        if (lex.toString().toLowerCase(Locale.ROOT).equals(lexCheckCase)) {
                            lexemeType = lex;
                            break;
                        }
                    }
                } else {
                    lexemeClass = LexemeClass.IDENTIFIER;
                    lexemeType = LexemeType.UNDEFINED;
                }
                break;
            case "Constant":
                lexemeClass = LexemeClass.CONSTANT;
                lexemeType = LexemeType.UNDEFINED;
                break;
            case "Arithmetic":
                lexemeClass = LexemeClass.OPERATOR;
                lexemeType = LexemeType.ARITHMETIC;
                break;
            case "LessComparison":
            case "MoreOrEqualComparison":
            case "Equality":
                lexemeClass = LexemeClass.OPERATOR;
                lexemeType = LexemeType.COMPARISON;
                break;
            case "Assignment":
                lexemeClass = LexemeClass.OPERATOR;
                lexemeType = LexemeType.ASSIGNMENT;
                break;
            default:
                return;
        }

        Lexeme lex = new Lexeme(lexemeClass, lexemeType, lexeme, position);
        lexemes.add(lex);
    }

    private static void printLexemes(){
        System.out.println("\nКлючевые слова: ");
        for (Lexeme lex : lexemes.stream()
                .filter(x -> x.getLexemeClass() == LexemeClass.KEYWORD)
                .collect(Collectors.toList())){
            System.out.println(lex);
        }

        System.out.println("\nИдентификаторы: ");
        for (Lexeme lex : lexemes.stream()
                .filter(x -> x.getLexemeClass() == LexemeClass.IDENTIFIER)
                .collect(Collectors.toList())){

            System.out.println(lex);
        }

        System.out.println("\nКонстанты: ");
        for (Lexeme lex : lexemes.stream()
                .filter(x -> x.getLexemeClass() == LexemeClass.CONSTANT)
                .collect(Collectors.toList())){

            System.out.println(lex);
        }

        System.out.println("\nСпециальные символы: ");
        for (Lexeme lex : lexemes.stream()
                .filter(x -> x.getLexemeClass() == LexemeClass.OPERATOR)
                .collect(Collectors.toList())){

            System.out.println(lex);
        }
    }

    public static void prepareData() throws IOException {
        checkCase = true;
        numOfStates = 9;
        int alphabetLength = 10;
        //начальное состояние
        startState = 0;
        //конечное состояние
        endStates = new int [] {8};

        Path path = Paths.get("src/main/resources/lab1_resources/trans_table.txt");
        BufferedReader reader = Files.newBufferedReader(path);

        //матрица переходов
        transFunc = new int[numOfStates][alphabetLength]; //размерность массива функции переходов
        for (int i=0; i < numOfStates; i++){
            String[] states = reader.readLine().split(" ");
            for (int j=0; j<alphabetLength; j++){
                transFunc[i][j] = Integer.parseInt(states[j]);
            }
        }
    }
}
