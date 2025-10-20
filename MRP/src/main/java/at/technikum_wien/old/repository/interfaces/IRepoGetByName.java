package at.technikum_wien.old.repository.interfaces;

import java.sql.SQLException;

public interface IRepoGetByName<Type> {
    public Type getByName(String name) throws SQLException;
}
