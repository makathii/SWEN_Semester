package at.technikum_wien.models.interfaces;

public interface IRepository<Type> {
    Type save(Type entity);
    Type getById(int id);
    void deleteById(Type entity);
}