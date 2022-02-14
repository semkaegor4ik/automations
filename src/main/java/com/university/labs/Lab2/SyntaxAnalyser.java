package com.university.labs.Lab2;

import com.university.labs.Lab1.LexAnalyser;
import com.university.labs.Lab1.Lexeme;
import com.university.labs.Lab1.LexemeClass;
import com.university.labs.Lab1.LexemeType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SyntaxAnalyser {
    public static List<Lexeme> lexemes = new ArrayList<>();
    public static int index;

    public static void main(String[] args) {
        LexAnalyser.main(new String[]{});

        lexemes = LexAnalyser.lexemes
                .stream()
                .sorted(Comparator.comparingInt(Lexeme::getPosition))
                .collect(Collectors.toList());

        index = 0;
        analyse();
    }

    public static void analyse(){

        while(index < lexemes.size() && lexemes.get(index).getLexemeType() != LexemeType.END){
            if (!isIfStatement()){
                return;
            }
        }
    }

    public static boolean isIfStatement(){
        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.IF){
            printError("Ключевое слово if ожидалось в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        if (!isLogExpression()) return false;

        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.THEN){
            printError("Ключевое слово then ожидалось в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        if (!isStatement(true)) return false;

        if (lexemes.get(index).getLexemeType() != LexemeType.ELSEIF){
            printError("Ключевое слово elseif ожидалось в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        if (!isLogExpression()) return false;

        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.THEN){
            printError("Ключевое слово then ожидалось в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        if (!isStatement(true )) return false;

        if (lexemes.get(index).getLexemeType() != LexemeType.ELSE){
            printError("Ключевое слово else ожидалось в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        if (!isStatement(true)) return false;

        return true;
    }

    public static boolean isCondition(){
        if (!isLogExpression()) return false;

        if (index >= lexemes.size())
            return true;

        while (lexemes.get(index) != null && lexemes.get(index).getLexemeType() == LexemeType.OR){
            index++;
            if (!isLogExpression()) return false;
        }
        return true;
    }

    public static boolean isLogExpression(){
        if (!isRelExpression()) return false;

        if (index >= lexemes.size())
            return true;

        while (lexemes.get(index) != null && lexemes.get(index).getLexemeType() == LexemeType.AND){
            index++;
            if (!isRelExpression())return false;
        }

        return true;
    }

    public static boolean isRelExpression(){
        if (!isOperand()) return false;

        if (index >= lexemes.size()){
            printError("Оператор сравнения ожидался в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        if (lexemes.get(index).getLexemeType() == LexemeType.COMPARISON){
            index++;
            if (!isOperand()) return false;
        }
        return true;
    }

    public static boolean isOperand(){
        if (index >= lexemes.size()){
            return false;
        }
        if (lexemes.get(index).getLexemeClass() != LexemeClass.CONSTANT
                && lexemes.get(index).getLexemeClass() != LexemeClass.IDENTIFIER){
            printError("Переменная или константа ожидалась в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        return true;
    }

    public static boolean isLogicalOp(){
        if (lexemes.get(index).getLexemeType() != LexemeType.AND
                && lexemes.get(index).getLexemeType() != LexemeType.OR){
            printError("Логическая операция ожидалась в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        return true;
    }

    public static boolean isStatement(boolean isRequired){

        if (index >= lexemes.size()) return true;

        if (lexemes.get(index).getLexemeClass() == LexemeClass.IDENTIFIER){
            if (!isDefaultStatement()){
                return false;
            }
        }
        else if (lexemes.get(index).getLexemeType() == LexemeType.INPUT){
            if (!isInputStatement()){
                return false;
            }
        }
        else if (lexemes.get(index).getLexemeType() == LexemeType.OUTPUT){
            if (!isOutputStatement()){
                return false;
            }
        }
        else{
            if (isRequired){
                printError("Input, output или идентификатор ожидался в позиции " + lexemes.get(index).getPosition());
                return false;
            }
        }

        return true;
    }

    public static boolean isDefaultStatement(){ //a = b + 1
        index++;
        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.ASSIGNMENT){
            printError("Присваивание ожидалось в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        if (!isArithmExpr()) return false;

        isStatement(false);

        return true;
    }

    public static boolean isInputStatement(){
        index++;
        if (index >= lexemes.size() || lexemes.get(index).getLexemeClass() != LexemeClass.IDENTIFIER){
            printError("Переменная ожидалась в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        isStatement(false);

        return true;
    }

    public static boolean isOutputStatement(){
        index++;
        if (index >= lexemes.size() || lexemes.get(index).getLexemeClass() != LexemeClass.IDENTIFIER &&
                lexemes.get(index).getLexemeClass() != LexemeClass.CONSTANT){
            printError("Переменная или константа ожидалась в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        isStatement(false);

        return true;
    }

    public static boolean isArithmExpr(){
        if (!isOperand()) return false;

        while (index < lexemes.size() && lexemes.get(index).getLexemeType() == LexemeType.ARITHMETIC){
            index++;
            if (!isOperand()) return false;
        }

        return true;
    }

    public static void printError(String message){
        System.out.println("Ошибка: " + message);
    }

}
