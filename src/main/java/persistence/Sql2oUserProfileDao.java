package persistence;

import exception.DaoException;
import model.User;
import java.util.List;

import model.UserProfile;
import model.WorkUpdate;
import model.Work;
import org.sql2o.*;

public class Sql2oUserProfileDao implements UserProfileDao {

    private final Sql2o sql2o;

    public Sql2oUserProfileDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public int add(UserProfile userProf) throws DaoException {
        try (Connection con = sql2o.open()) {
            String query = "INSERT INTO UserProfiles (userID, isPublic, bio, favoriteWorks)" +
                    "VALUES (:userID, :isPublic, :bio, :favoriteWorks)";
            int id = (int) con.createQuery(query, true)
                    .addParameter("userID", userProf.getUserID())
                    .addParameter("isPublic", userProf.isPublic())
                    .addParameter("bio", userProf.getBio())
                    .addParameter("favoriteWorks",userProf.getFavoriteWorks())
                    .executeUpdate().getKey();
            userProf.setId(id);
            return id;
        }
        catch (Sql2oException ex) {
            throw new DaoException("Add userProfile failed!");
        }
    }

    @Override
    public List<UserProfile> listAll() {
        String sql = "SELECT * FROM UserProfiles";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(UserProfile.class);
        }
        catch (Sql2oException ex) {
            throw new DaoException("List all user profiles failed!");
        }
    }

    public UserProfile getUserProfileById (int userID) {
        String sql = "SELECT * FROM UserProfiles WHERE userID=:userID";
        try (Connection con = sql2o.open()) {
            List<UserProfile> res = con.createQuery(sql)
                    .addParameter("userID", userID)
                    .executeAndFetch(UserProfile.class);
            if (res.isEmpty()) {
                throw new DaoException("Get user profile by username failed!");
            }
            return res.get(0);
        }
        catch (Sql2oException ex) {
            throw new DaoException("Get user profile by username failed!");
        }
    }

    @Override
    public boolean delete(UserProfile userProf) throws DaoException {
        try (Connection con = sql2o.open()) {
            String query = "DELETE FROM UserProfiles WHERE username=:userID";
            con.createQuery(query).bind(userProf).executeUpdate();
            return true;
        }
    }

    @Override
    public boolean update(UserProfile userProf) throws DaoException{
        try (Connection con = sql2o.open()) {
            //Check if the user exists or not
            String userQuery = "SELECT * FROM UserProfiles WHERE userID=:userID";
            List<UserProfile> userRes = con.createQuery(userQuery).bind(userProf).executeAndFetch(UserProfile.class);
            if (userRes.isEmpty()) {
                throw new DaoException("This userProfile does not exist in the database!");
            }

            String query = "Update UserProfiles SET " +
//                    "isPublic=:isPublic, " +
                    "favoriteWorks=:favoriteWorks, " +
                    "bio=:bio " +
                    "WHERE userID=:userID";
            con.createQuery(query).bind(userProf).executeUpdate();
            return true;
        }
    }

}