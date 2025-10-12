package at.technikum_wien.repository.interfaces;

public interface IRepoGetByID<Type> {
    public Type getById(int id);
}
