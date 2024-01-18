package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;

public class UserService {

    private final DataAccess dataAccess;


    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public AuthData register(UserData user) throws ChessServerException {
        try {
            if (user == null || user.username() == null || user.password() == null || user.email() == null) {
                throw new BadRequestException("Error: Username, Password, and email must not be null");
            }


            if (dataAccess.findUser(user.username()) != null) {
                throw new RequestItemTakenException("Error: username taken");
            }

            dataAccess.insertUser(user);

            AuthData auth = AuthData.getNewAuthData(user.username());
            dataAccess.insertAuth(auth);

            return auth;
        } catch (DataAccessException e) {
            throw new ChessServerException(e);
        }
    }


    public AuthData login(UserData user) throws ChessServerException {
        try {
            UserData dataUser = dataAccess.findUser(user.username());
            if (dataUser == null || !user.password().equals(dataUser.password())) {
                throw new UnauthorizedException("Error: Incorrect username or password");
            }

            AuthData auth = AuthData.getNewAuthData(user.username());
            dataAccess.insertAuth(auth);

            return auth;
        } catch (DataAccessException e) {
            throw new ChessServerException(e);
        }
    }


    public void logout(String authtoken) throws ChessServerException {
        try {
            AuthData delete = dataAccess.findAuth(authtoken);
            if (delete == null) throw new UnauthorizedException("Error: Unauthorized");
            dataAccess.deleteAuth(authtoken);
        } catch (DataAccessException e) {
            throw new ChessServerException(e);
        }
    }

}
