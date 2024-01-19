package server;

import dataAccess.DataAccess;
import dataAccess.MemoryDataAccess;
import handler.*;
import service.*;
import spark.*;

import java.net.HttpURLConnection;

public class Server {

    public static void main(String[] args) {
        System.out.println("http://localhost:" +new Server().run(0));
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        var webDir = Server.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        Spark.staticFiles.location( "web");

        DataAccess dataAccess = new MemoryDataAccess();

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", new RegisterHandler(dataAccess));

        Spark.path("/session", () -> {
            Spark.post("", new LoginHandler(dataAccess));
            Spark.delete("", new LogoutHandler(dataAccess));
        });

        Spark.path("/game", () -> {
            Spark.get("", new ListGamesHandler(dataAccess));
            Spark.post("", new CreateGameHandler(dataAccess));
            Spark.put("", new JoinGameHandler(dataAccess));
        });

        Spark.delete("/db", new ClearHandler(dataAccess));

        Spark.exception(BadRequestException.class, new ChessServerExceptionHandler<>(HttpURLConnection.HTTP_BAD_REQUEST));
        Spark.exception(UnauthorizedException.class, new ChessServerExceptionHandler<>(HttpURLConnection.HTTP_UNAUTHORIZED));
        Spark.exception(RequestItemTakenException.class, new ChessServerExceptionHandler<>(HttpURLConnection.HTTP_FORBIDDEN));
        Spark.exception(ChessServerException.class, new ChessServerExceptionHandler<>(HttpURLConnection.HTTP_INTERNAL_ERROR));

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
    }
}
