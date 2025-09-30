package at.technikum_wien.repository;

import java.sql.SQLException;

public interface Repository<Type> {
        public Type save(Type type);
        public void deleteById(Type type);
        public Type getById(int id);
        public Type getByName(String name) throws SQLException;
}
