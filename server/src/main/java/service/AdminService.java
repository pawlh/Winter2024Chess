package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import websocket.WebSocketHandler;

public class AdminService {
    private final DataAccess dataAccess;

    public AdminService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() throws ChessServerException {
        try {
            dataAccess.clearAll();
            WebSocketHandler.getInstance().clear();
        }catch (DataAccessException e) {
            throw new ChessServerException(e);
        }

    }

}
