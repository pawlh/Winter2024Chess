package services;

import dataAccess.DataAccess;
import dataAccess.MemoryDataAccess;

public class TestFactory {

    private enum DAOTypes {MYSQL, MEMORY}

    private static final DAOTypes currentType = DAOTypes.MEMORY;


    public static DataAccess getDatabaseFactory() {
        return switch (currentType) {
            case MYSQL -> null;
            case MEMORY -> new MemoryDataAccess();
        };
    }

}
