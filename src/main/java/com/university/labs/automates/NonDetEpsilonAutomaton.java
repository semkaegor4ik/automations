package com.university.labs.automates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class NonDetEpsilonAutomaton {
    public static int numOfStates;
    public static List<String> alphabet = new ArrayList<>();
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

        while(true) {
            System.out.print("Алфавит: ");
            for (String s : alphabet) {
                if (s.equals("eps"))
                    System.out.print("| " + s);
                else
                    System.out.print(s + " ");
            }


            System.out.println("\nВведите строку на вход автомата: ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            List<Integer> startStateList = Arrays.asList(startState);
            startAutomate(startStateList, input.split(""), 0, new HashSet<>());
        }
    }

    public static void startAutomate(List<Integer> currentStateList, String [] inputSymbols, int inputIndex, Set<Integer> endStateList) {
        if (currentStateList.size() == 0){
            return;
        }

        for (int state : currentStateList){
            if (transFunc[state][alphabet.indexOf("eps")].size() != 0){
                startAutomate(transFunc[state][alphabet.indexOf("eps")], inputSymbols, inputIndex, endStateList);
            }
        }

        for (int state : currentStateList){
            if (inputIndex == inputSymbols.length){
                endStateList.addAll(currentStateList);
                return;
            }

            int symbolIndex = alphabet.indexOf(inputSymbols[inputIndex]);
            startAutomate(transFunc[state][symbolIndex], inputSymbols, inputIndex + 1, endStateList);
        }

        if (inputIndex != 0)
            return;

        boolean result = false;
        for (int i = 0; i < endStates.length; i++){
            if (endStateList.contains(endStates[i])){
                result = true;
                break;
            }
        }

        System.out.print("Заключительные состояния: ");
        for (int state : endStateList)
            System.out.print(state + " ");
        System.out.println();

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/output.txt"));
            for (String s : inputSymbols){
                writer.write(s);
            }

            if (result){
                System.out.println("Строка принята.\n");
                writer.write(": Строка принята.\n");
            }
            else{
                System.out.println("Строка отвергнута.\n");
                writer.write(": Строка отвергнута.\n");
            }


            writer.close();
        }
        catch (IOException e){

        }
    }

    public static void prepareData() throws IOException {
        String automateNum = "s1";

        Path path = Paths.get("src/main/resources/EpsAuto_transactionFunctions.txt");
        BufferedReader reader = Files.newBufferedReader(path);
        String line = "";
        while (!line.equals(automateNum)){
            line = reader.readLine();
        }

        alphabet.addAll(Arrays.asList(reader.readLine().split(" ")));
        alphabet.add("eps");
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
