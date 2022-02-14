package com.university.labs.Lab3;

import com.university.labs.Lab1.LexAnalyser;
import com.university.labs.Lab1.Lexeme;
import com.university.labs.Lab1.LexemeClass;
import com.university.labs.Lab1.LexemeType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SyntaxSemanticAnalyser {
    public static List<Lexeme> lexemes = new ArrayList<>();
    public static List<PostfixEntry> entryList = new ArrayList<>();
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
            if (!isWhileStatement()){
                return;
            }
        }

        printEntryList();
    }

    public static boolean isWhileStatement(){
        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.IF){
            printError("Ключевое слово if ожидалось в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        if (!isLogExpression()) return false;

        int indJmp = writeCmdPtr(-1); //к elseif
        writeCmd(ECmd.JZ);

        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.THEN){
            printError("Ключевое слово then ожидалось в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        if (!isStatement(true)) return false;

        setCmdPtr(indJmp, entryList.size()+2); //установка индекса elseif

        int indIf = writeCmdPtr(-1); //к команде после end
        writeCmd(ECmd.JMP);

        if (lexemes.get(index).getLexemeType() != LexemeType.ELSEIF){
            printError("Ключевое слово elseif ожидалось в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        if (!isLogExpression()) return false;

        indJmp = writeCmdPtr(-1); //к else
        writeCmd(ECmd.JZ);

        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.THEN){
            printError("Ключевое слово then ожидалось в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        if (!isStatement(true )) return false;

        setCmdPtr(indJmp, entryList.size()+2);//установка индекса else

        int indElseIf = writeCmdPtr(-1); //к команде после end
        writeCmd(ECmd.JMP);

        if (lexemes.get(index).getLexemeType() != LexemeType.ELSE){
            printError("Ключевое слово else ожидалось в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        index++;
        if (!isStatement(true)) return false;

        setCmdPtr(indIf, entryList.size());
        setCmdPtr(indElseIf, entryList.size());
        return true;
    }

    public static boolean isCondition(){
        if (!isLogExpression()) return false;

        if (index >= lexemes.size())
            return true;

        while (lexemes.get(index) != null && lexemes.get(index).getLexemeType() == LexemeType.OR){
            index++;
            if (!isLogExpression()) return false;
            writeCmd(ECmd.OR);
        }
        return true;
    }

    public static boolean isLogExpression(){
        if (!isRelExpression()) return false;

        if (index >= lexemes.size())
            return true;

        while (index < lexemes.size() && lexemes.get(index).getLexemeType() == LexemeType.AND){
            index++;
            if (!isRelExpression())return false;
            writeCmd(ECmd.AND);
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
            ECmd cmd = null;
            String val = lexemes.get(index).getValue();
            if (val.equals("<")) cmd = ECmd.CMPL;
            else if (val.equals("<=")) cmd = ECmd.CMPLE;
            else if (val.equals("<>")) cmd = ECmd.CMPNE;
            else if (val.equals("==")) cmd = ECmd.CMPE;
            else if (val.equals(">")) cmd = ECmd.CMPG;
            else if (val.equals(">=")) cmd = ECmd.CMPGE;
            index++;
            if (!isOperand()) return false;
            writeCmd(cmd);
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

        if (lexemes.get(index).getLexemeClass() == LexemeClass.IDENTIFIER)
            writeVar(index);
        else
            writeConst(index);
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
            writeVar(index);
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
        writeCmd(ECmd.SET);

        isStatement(false);

        return true;
    }

    public static boolean isInputStatement(){
        index++;
        if (index >= lexemes.size() || lexemes.get(index).getLexemeClass() != LexemeClass.IDENTIFIER){
            printError("Переменная ожидалась в позиции " +lexemes.get(index).getPosition());
            return false;
        }
        writeVar(index);
        writeCmd(ECmd.INP);

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
        writeVar(index);
        writeCmd(ECmd.OUT);

        index++;
        isStatement(false);

        return true;
    }

    public static boolean isArithmExpr(){
        if (!isOperand()) return false;

        while (index < lexemes.size() && lexemes.get(index).getLexemeType() == LexemeType.ARITHMETIC){
            ECmd cmd = null;
            String val = lexemes.get(index).getValue();
            if (val.equals("+"))  cmd = ECmd.ADD;
            else if (val.equals("-")) cmd = ECmd.SUB;
            else if (val.equals("*")) cmd = ECmd.MUL;
            else if (val.equals("/")) cmd = ECmd.DIV;
            index++;
            if (!isOperand()) return false;
            writeCmd(cmd);
        }

        return true;
    }

    public static void printError(String message){
        System.out.println("Ошибка: " + message);
    }

    private static int writeCmd(ECmd cmd){
        PostfixEntry entry = new PostfixEntry();
        entry.setType(EEntryType.CMD);
        entry.setCmd(cmd);
        entryList.add(entry);
        return entryList.size()-1;
    }

    private static int writeVar(int index){
        PostfixEntry variable = new PostfixEntry();
        variable.setType(EEntryType.VAR);
        variable.setValue(lexemes.get(index).getValue());
        entryList.add(variable);
        return entryList.size()-1;
    }

    private static int writeConst(int index){
        PostfixEntry con = new PostfixEntry();
        con.setType(EEntryType.CONST);
        con.setValue(lexemes.get(index).getValue());
        entryList.add(con);
        return entryList.size()-1;
    }

    private static int writeCmdPtr(int ptr){
        PostfixEntry cmdPtr = new PostfixEntry();
        cmdPtr.setType(EEntryType.CMDPTR);
        cmdPtr.setCmdPtr(ptr);
        entryList.add(cmdPtr);
        return entryList.size()-1;
    }

    private static void setCmdPtr(int index, int ptr){
        entryList.get(index).setCmdPtr(ptr);
    }

    private static void printEntryList(){
        for (PostfixEntry entry : entryList) {
            System.out.print(entry + " ");
        }
    }
}
