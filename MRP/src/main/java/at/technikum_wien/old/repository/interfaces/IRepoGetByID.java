package at.technikum_wien.old.repository.interfaces;

public interface IRepoGetByID<Type> {
    public Type getById(int id);
}
