import data.DataCache;
import ui.Repl;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;

public class ClientMain {
    public static void main(String[] args) throws DeploymentException, URISyntaxException, IOException {
        Repl repl = new Repl();
        String host = "localhost";
        int port = 8080;
        if (args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        DataCache.getInstance().setRunOptions(host, port, repl);
        repl.run();
    }
}
