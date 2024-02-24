package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public interface DataAccess {
    AuthDAO getAuthDAO();
    GameDAO getGameDAO();
    UserDAO getUserDAO();
}
