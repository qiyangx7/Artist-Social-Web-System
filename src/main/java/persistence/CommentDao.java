package persistence;

import model.Comment;
import exception.DaoException;
import java.util.List;

public interface CommentDao {

    int add(Comment comment) throws DaoException;
    List<Comment> listAll() throws DaoException;
    boolean delete(Comment comment) throws DaoException;
    boolean update(Comment comment) throws DaoException;

}
