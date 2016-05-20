package com.abusalimov.mrcalc;

/**
 * @author Eldar Abusalimov
 */
public class Main {

    public static void main(String[] args) {
        runREPL();
    }

    private static void runREPL() {
        new REPL().loop();
    }
}
