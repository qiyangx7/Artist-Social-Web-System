package persistence;

import model.Comment;
import model.WorkUpdate;
import exception.DaoException;
import java.util.List;

public interface WorkUpdateDao {
    int add(WorkUpdate wu) throws DaoException;
    List<WorkUpdate> listAll() throws DaoException;
    WorkUpdate listLast(int id) throws DaoException;
    boolean delete(WorkUpdate wu) throws DaoException;
    boolean update(WorkUpdate wu) throws DaoException;
}
