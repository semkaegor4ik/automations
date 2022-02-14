package com.university.labs.automates;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class DetFiniteAutomaton {
    public static int numOfStates;
    public static List<String> alphabet = new ArrayList<>();
    public static int [][] transFunc;
    public static int startState;
    public static int [] endStates;

    public static void main(String[] args) {
        try {
            prepareData();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }

        System.out.print("Алфавит: ");
        for (String s : alphabet) {
            System.out.print(s + " ");
        }
        System.out.println("\nВведите строку на вход автомата: ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        startAutomate(input);
    }

    public static void startAutomate(String input){
        int currentState = startState;
        String [] inputArray = input.split("");
        for (String symbol : inputArray){
            int index = alphabet.indexOf(symbol);
            currentState = transFunc[currentState][index];
        }

        boolean result = false;
        for (int i = 0; i < endStates.length; i++){
            if (currentState == endStates[i]){
                result = true;
                break;
            }
        }

        if (result){
            System.out.println("Строка принята.");
        }
        else
            System.out.println("Строка отвергнута.");
    }

    public static void prepareData() throws IOException {
        String automateNum = "s1";

        Path path = Paths.get("src/main/resources/DetAuto_transactionFunctions.txt");
        BufferedReader reader = Files.newBufferedReader(path);
        String line = "";
        while (!line.equals(automateNum)){
            line = reader.readLine();
        }

        alphabet = Arrays.asList(reader.readLine().split(" "));
        String[] params = reader.readLine().split(" ");

        //число состояний (множество)
        numOfStates = Integer.parseInt(params[0]);

        //алфавит
        int alphabetLength = alphabet.size();

        //матрица переходов
        transFunc = new int[numOfStates][alphabetLength]; //размерность массива функции переходов
        for (int i=0; i < numOfStates; i++){
            String[] states = reader.readLine().split(" ");
            for (int j=0; j<alphabetLength; j++){
                transFunc[i][j] = Integer.parseInt(states[j]);
            }
        }

        //начальное состояние
        startState = Integer.parseInt(params[2]);

        //множество конечных состояний
        int len = params.length - 3;
        endStates = new int[len];
        for (int i=0; i< len; i++){
            endStates[i] = Integer.parseInt(params[i+3]);
        }
    }
}
