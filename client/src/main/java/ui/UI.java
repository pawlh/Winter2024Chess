package ui;

import web.ServerFacade;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public interface UI {
    String eval(String cmd, String[] args);
    String help();
    String getPromptText();
}
