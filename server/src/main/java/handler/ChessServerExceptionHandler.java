package handler;

import com.google.gson.Gson;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;


/**
 * Handles Exceptions thrown from the server
 *
 * @param <T> The type of exception
 */
public class ChessServerExceptionHandler<T extends Exception> implements ExceptionHandler<T> {

    private final int responseCode;


    /**
     * @param responseCode HTTP response code to use for this type of Exception
     */
    public ChessServerExceptionHandler(int responseCode) {
        this.responseCode = responseCode;
    }


    @Override
    public void handle(T t, Request request, Response response) {
        t.printStackTrace();
        response.status(responseCode);

        Result result = new Result(t.getMessage());

        response.body(new Gson().toJson(result));
    }

    private static record Result(String message) {}


}
