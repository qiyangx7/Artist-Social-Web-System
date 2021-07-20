package persistence;

import exception.DaoException;
import model.UserProfile;

import java.util.List;

public interface UserProfileDao {
    int add(UserProfile userProf) throws DaoException;
    List<UserProfile> listAll() throws DaoException;
    boolean delete(UserProfile userProf) throws DaoException;
    boolean update(UserProfile userProf) throws DaoException;
}
