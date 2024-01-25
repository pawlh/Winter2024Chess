package unitTests;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import dataAccess.MemoryDataAccess;
import dataAccess.MySqlDataAccess;

public class TestFactory {

    private enum DAOTypes {MYSQL, MEMORY}

    private static final DAOTypes currentType = DAOTypes.MYSQL;


    public static DataAccess getDatabaseFactory() {
        return switch (currentType) {
            case MYSQL -> {
                try {
                    yield new MySqlDataAccess();
                } catch (DataAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            case MEMORY -> new MemoryDataAccess();
        };
    }

}
