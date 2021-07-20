package persistence;

import exception.DaoException;
import model.Work;
import model.WorkUpdate;
import java.util.List;
import org.sql2o.*;

public class Sql2oWorkUpdateDao implements WorkUpdateDao {

    private final Sql2o sql2o;

    public Sql2oWorkUpdateDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }


    @Override
    public int add(WorkUpdate wu) throws DaoException {
        try (Connection con = sql2o.open()) {
            String query = "INSERT INTO WorkUpdates (iteration, workId, prevItId, description, workFile)" +
                    "VALUES (:iteration, :workId, :prevItId, :description, :workFile)";
            int id = (int) con.createQuery(query, true)
                    .bind(wu)
                    .executeUpdate().getKey();
            wu.setId(id);
            return id;
        }
        catch (Sql2oException ex) {
            throw new DaoException("Add workUpdate failed!");
        }
    }

    @Override
    public List<WorkUpdate> listAll() {
        String sql = "SELECT * FROM WorkUpdates";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(WorkUpdate.class);
        }
        catch (Sql2oException ex) {
            throw new DaoException("List all workUpdates failed!");
        }
    }

    @Override
    public WorkUpdate listLast(int id) {
        String sql = "SELECT * FROM WorkUpdates WHERE workId =" + String.valueOf(id)
            + " ORDER BY ID DESC LIMIT 1";
        try (Connection con = sql2o.open()) {
            List<WorkUpdate> res = con.createQuery(sql).executeAndFetch(WorkUpdate.class);
            if (res.isEmpty()) {
                throw new DaoException("List latest workUpdate failed!");
            }
            return res.get(0);
        }
        catch (Sql2oException ex) {
            throw new DaoException("List latest workUpdate failed!");
        }
    }

    //Pedro helper function addition
    public WorkUpdate getSpecificIteration(int wId, int itId) {
        String sql = "SELECT * FROM WorkUpdates WHERE workId =" + String.valueOf(wId)
                + " AND id =" + String.valueOf(itId) + " ORDER BY ID DESC LIMIT 1";
        try (Connection con = sql2o.open()) {
            List<WorkUpdate> res = con.createQuery(sql).executeAndFetch(WorkUpdate.class);
            if (res.isEmpty()) {
                throw new DaoException("getSpecificIteration failed!");
            }
            return res.get(0);
        }
        catch (Sql2oException ex) {
            throw new DaoException("getSpecificIteration failed!");
        }
    }

    @Override
    public boolean delete(WorkUpdate wu) throws DaoException {
        try (Connection con = sql2o.open()) {
            String query = "DELETE FROM WorkUpdates WHERE id=:id";
            con.createQuery(query).bind(wu).executeUpdate();
            return true;
        }
    }

    @Override
    public boolean update(WorkUpdate wu) throws DaoException{
        try (Connection con = sql2o.open()) {

            //Check if the workUpdate exists or not
            String wuQuery = "SELECT * FROM WorkUpdates WHERE id=:id";
            List<WorkUpdate> wuRes = con.createQuery(wuQuery).bind(wu).executeAndFetch(WorkUpdate.class);
            if (wuRes.isEmpty()) {
                throw new DaoException("This workUpdate does not exist in the database!");
            }

            String query = "Update WorkUpdates SET " +
                    "iteration=:iteration, " +
                    "workId=:workId, "+
                    "prevItId=:prevItId, " +
                    "description=:description " +
                    "workFile=:workFile " +
                    "WHERE id=:id";
            con.createQuery(query).bind(wu).executeUpdate();
            return true;
        }
    }

}
