package ui;

public interface UserInterface {
    String eval(String cmd, String[] args);
    String help();
    String getPromptText();
}
