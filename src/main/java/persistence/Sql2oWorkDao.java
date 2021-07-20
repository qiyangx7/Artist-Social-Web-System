package persistence;

import exception.DaoException;
import model.User;
import model.Work;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.LinkedList;
import java.util.List;

public class Sql2oWorkDao implements WorkDao {

    private final Sql2o sql2o;

    public Sql2oWorkDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }


    @Override
    public int add(Work work) throws DaoException {
        try (Connection con = sql2o.open()) {
            String query = "INSERT INTO Works (creatorId, title, completionStatus, currentWorkUpdateId)" +
                    "VALUES (:creatorId, :title, :completionStatus, :currentWorkUpdateId)";
            int id = (int) con.createQuery(query, true)
                    .bind(work)
                    .executeUpdate().getKey();
            work.setId(id);
            return id;
        }
        catch (Sql2oException ex) {
            throw new DaoException("Add work failed!");
        }
    }

    @Override
    public List<Work> listAll() {
        String sql = "SELECT * FROM Works";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(Work.class);
        }
        catch (Sql2oException ex) {
            throw new DaoException("List all works failed!");
        }
    }

    public List<Work> feeds(String[] followingArray) {
        String sql = "SELECT * FROM Works LIMIT 20";
        try (Connection con = sql2o.open()) {
            List<Work> fullList = con.createQuery(sql).executeAndFetch(Work.class);
            List<Work> outList = new LinkedList<>();
            for (Work work : fullList) {
                for (String id: followingArray) {
                    if (work.getCreatorId() == Integer.parseInt(id)) {
                        outList.add(work);
                    }
                }
            }
            return outList;
        }
        catch (Sql2oException ex) {
            throw new DaoException("Feed failed!");
        }
    }

    public List<Work> listByAuthor(int id) {
        String sql = "SELECT * FROM Works WHERE creatorID =" + String.valueOf(id);
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(Work.class);
        }
        catch (Sql2oException ex) {
            throw new DaoException("List work by author failed!");
        }
    }

    public Work getWorkById(int id) {
        String sql = "SELECT * FROM Works WHERE id=:id";
        try (Connection con = sql2o.open()) {
            List<Work> res = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Work.class);
            if (res.isEmpty()) {
                throw new DaoException("This work does not exist in the database!");
            }
            return res.get(0);
        }
        catch (Sql2oException ex) {
            throw new DaoException("List work by author failed!");
        }
    }

    @Override
    public boolean delete(Work work) throws DaoException {
        try (Connection con = sql2o.open()) {
            String query = "DELETE FROM Works WHERE id=:id";
            con.createQuery(query).bind(work).executeUpdate();
            return true;
        }
    }

    @Override
    public boolean update(Work work) throws DaoException{
        try (Connection con = sql2o.open()) {

            //Check if the work exists or not
            String workQuery = "SELECT * FROM Works WHERE id=:id";
            List<Work> workRes = con.createQuery(workQuery).bind(work).executeAndFetch(Work.class);
            if (workRes.isEmpty()) {
                throw new DaoException("This work does not exist in the database!");
            }

            String query = "Update Works SET " +
                    "title=:title, " +
                    "completionStatus=:completionStatus, " +
                    "currentWorkUpdateId=:currentWorkUpdateId " +
                    "WHERE id=:id";
            con.createQuery(query).bind(work).executeUpdate();
            return true;
        }
    }

}
