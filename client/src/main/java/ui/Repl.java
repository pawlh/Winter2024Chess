package ui;

import data.DataCache;

import java.util.Arrays;
import java.util.Scanner;

public class Repl {

    public void run() {
        System.out.println("\uD83D\uDC36 Welcome to Chess. Sign in to start.");
        System.out.print(DataCache.getInstance().getUi().help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            String[] tokens = line.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

            try {
                result = DataCache.getInstance().getUi().eval(cmd, params);
                System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                System.out.print(e.getMessage());
            }
        }
        System.out.println();
    }


    private void printPrompt() {
        System.out.print("\n" + EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.SET_TEXT_ITALIC +
                DataCache.getInstance().getUi().getPromptText() + " >>> " + EscapeSequences.SET_TEXT_COLOR_GREEN +
                EscapeSequences.RESET_TEXT_ITALIC);
    }

}
