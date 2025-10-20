package at.technikum_wien.domain.interfaces;

public interface IRepository<Type> {
    Type save(Type entity);
    Type getById(int id);
    void deleteById(Type entity);
}