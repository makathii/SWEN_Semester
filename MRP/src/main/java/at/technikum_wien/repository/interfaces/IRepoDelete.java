package at.technikum_wien.repository.interfaces;

public interface IRepoDelete<Type> {
    public void deleteById(Type type);
}
