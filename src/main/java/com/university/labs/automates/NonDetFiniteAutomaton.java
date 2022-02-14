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

public class NonDetFiniteAutomaton {
    public static int numOfStates;
    public static List<String> alphabet;
    public static List<Integer> [][] transFunc;
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
        List<Integer> startStateList = Arrays.asList(startState);
        startAutomate(startStateList, input.split(""), 0, new ArrayList<>());
    }

    public static void startAutomate(List<Integer> currentStateList, String [] input, int inputIndex, List<Integer> endStateList){
        if (currentStateList.size() == 0){
            return;
        }

        if (inputIndex == input.length){
            endStateList.addAll(currentStateList);
            return;
        }

        for (int state : currentStateList){
            int symbolIndex = alphabet.indexOf(input[inputIndex]);
            startAutomate(transFunc[state][symbolIndex], input, inputIndex + 1, endStateList);
        }

        if (inputIndex != 0)
            return;

        boolean result = false;
        for (int i = 0; i < endStates.length; i++){
            int ind = endStateList.indexOf(endStates[i]);
            if (ind != -1){
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

        Path path = Paths.get("src/main/resources/NonDetAuto_transactionFunctions.txt");
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
        transFunc = new List[numOfStates][alphabetLength]; //размерность массива функции переходов
        for (int i=0; i < numOfStates; i++){
            String[] stateLists = reader.readLine().split(" ");
            for (int j=0; j < alphabetLength; j++){
                if (stateLists[j].equals("_")) {
                    transFunc[i][j] = new ArrayList<>();
                    continue;
                }
                String [] stateArr = stateLists[j].split(",");
                List<Integer> stateList = new ArrayList<>();
                for (String s : stateArr)
                    stateList.add(Integer.valueOf(s));
                transFunc[i][j] = stateList;
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
