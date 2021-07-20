package persistence;

import exception.DaoException;
import model.Comment;
import java.util.List;

import model.WorkUpdate;
import org.sql2o.*;

public class Sql2oCommentDao implements CommentDao {

    private final Sql2o sql2o;

    public Sql2oCommentDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }


    @Override
    public int add(Comment comment) throws DaoException {
        try (Connection con = sql2o.open()) {
            String query = "INSERT INTO Comments (parentId, comment, userId, workUpdateId)" +
                    "VALUES (:parentId, :comment, :userId, :workUpdateId)";
            int id = (int) con.createQuery(query, true)
                    .bind(comment)
                    .executeUpdate().getKey();
            comment.setId(id);
            return id;
        }
        catch (Sql2oException ex) {
            throw new DaoException("Add comment failed!");
        }
    }

    @Override
    public List<Comment> listAll() {
        String sql = "SELECT * FROM Comments";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(Comment.class);
        }
        catch (Sql2oException ex) {
            throw new DaoException("List all comments failed!");
        }
    }

    @Override
    public boolean delete(Comment comment) throws DaoException {
        try (Connection con = sql2o.open()) {
            String query = "DELETE FROM Comments WHERE id=:id";
            con.createQuery(query).bind(comment).executeUpdate();
            return true;
        }
    }

    @Override
    public boolean update(Comment comment) throws DaoException{
        try (Connection con = sql2o.open()) {

            //Check if the comment exists or not
            String commentQuery = "SELECT * FROM Comments WHERE id=:id";
            List<Comment> commentRes = con.createQuery(commentQuery).bind(comment).executeAndFetch(Comment.class);
            if (commentRes.isEmpty()) {
                throw new DaoException("This comment does not exist in the database!");
            }

            String query = "Update Comments SET " +
                    "userId=:userId, " +
                    "comment:=comment, " +
                    "parentId:=parentId " +
                    "workUpdateId=:workUpdateId, " +
                    "WHERE username=:username";
            con.createQuery(query).bind(comment).executeUpdate();
            return true;
        }
    }

    public List<Comment> listCommentByWorkUpdate(int wuId) {
        String sql = "SELECT * FROM Comments WHERE workUpdateId=:workUpdateId"
                + " ORDER BY ID DESC";
        try (Connection con = sql2o.open()) {
            List<Comment> res = con.createQuery(sql)
                                      .addParameter("workUpdateId", wuId)
                                      .executeAndFetch(Comment.class);
            return res;
        }
        catch (Sql2oException ex) {
            throw new DaoException("List comments by work update failed!");
        }
    }

}
