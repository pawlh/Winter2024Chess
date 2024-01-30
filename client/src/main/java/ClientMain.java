import data.DataCache;
import ui.Repl;

public class ClientMain {
    public static void main(String[] args) {
        if (args.length == 1) {
            String serverUrl = args[0];
            DataCache.getInstance().setServerUrl(serverUrl);
        }

        new Repl().run();
    }
}
