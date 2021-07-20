package persistence;

import model.Work;
import exception.DaoException;
import java.util.List;

public interface WorkDao {
    int add(Work work) throws DaoException;
    List<Work> listAll() throws DaoException;
    boolean delete(Work work) throws DaoException;
    boolean update(Work work) throws DaoException;
}
