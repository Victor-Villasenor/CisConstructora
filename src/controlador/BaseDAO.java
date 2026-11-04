package controlador;

import java.util.List;

public interface BaseDAO<T> {
    boolean registrar(T objeto);
    List<T> listar();
    boolean modificar(T objeto);
    boolean eliminar(int id);
    List<T> buscar(String texto);
}
