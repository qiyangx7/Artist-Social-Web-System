package persistence;

import model.User;
import exception.DaoException;
import java.util.List;

public interface UserDao {
    int add(User user) throws DaoException;
    List<User> listAll() throws DaoException;
    User getUserById(int id) throws DaoException;
    int getId(User user) throws DaoException;
    boolean delete(User user) throws DaoException;
    boolean update(User user) throws DaoException;
}
