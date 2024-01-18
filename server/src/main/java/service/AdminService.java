package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;

public class AdminService {
    private final DataAccess dataAccess;

    public AdminService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() throws ChessServerException {
        try {
            dataAccess.clearAll();
        }catch (DataAccessException e) {
            throw new ChessServerException(e);
        }

    }

}
