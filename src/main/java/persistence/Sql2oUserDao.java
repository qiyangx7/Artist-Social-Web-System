package persistence;

import exception.DaoException;
import model.User;
import java.util.List;

import org.sql2o.*;

public class Sql2oUserDao implements UserDao {

    private final Sql2o sql2o;

    public Sql2oUserDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }


    @Override
    public int add(User user) throws DaoException {
        try (Connection con = sql2o.beginTransaction()) {
            String query = "INSERT INTO Users (username, followers, following)" +
                    "VALUES (:username, :followers, :following)";
            int id = (int) con.createQuery(query, true)
                    .addParameter("username", user.getUsername())
                    .addParameter("followers", user.getFollowers())
                    .addParameter("following", user.getFollowing())
                    .executeUpdate().getKey();
            user.setId(id);
            con.commit();
            return id;
        }
        catch (Sql2oException ex) {
            throw new DaoException("Add user failed!");
        }
    }

    @Override
    public List<User> listAll() {
        String sql = "SELECT * FROM Users";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(User.class);
        }
        catch (Sql2oException ex) {
            throw new DaoException("List all users failed!");
        }
    }

    @Override
    public User getUserById(int id) throws DaoException {
        String sql = "SELECT * FROM Users WHERE id=:id";
        try (Connection con = sql2o.open()) {
            List<User> res = con.createQuery(sql).addParameter("id", id).executeAndFetch(User.class);
            if (res.isEmpty()) {
                throw new DaoException("This user does not exist in the database!");
            }
            return res.get(0);
        }
        catch (Sql2oException ex) {
            throw new DaoException("Get User by Id failed!");
        }
    }

    public User getUserByUsername(String username) throws DaoException {
        String sql = "SELECT * FROM Users WHERE username=:username";
        try (Connection con = sql2o.open()) {
            List<User> res = con.createQuery(sql).addParameter("username", username).executeAndFetch(User.class);
            if (res.isEmpty()) {
                throw new DaoException("This user does not exist in the database!");
            }
            return res.get(0);
        }
        catch (Sql2oException ex) {
            throw new DaoException("Get User by username failed!");
        }
    }

    @Override
    public int getId(User user) throws DaoException {
        try (Connection con = sql2o.open()) {
            String query = "SELECT * FROM Users WHERE username =:username";
            List<User> res = con.createQuery(query).bind(user).executeAndFetch(User.class);
            System.out.println(res);
            return res.get(0).getId();
        }
        catch (Sql2oException ex) {
            throw new DaoException("Get userId failed!");
        }
    }

    @Override
    public boolean delete(User user) throws DaoException {
        try (Connection con = sql2o.open()) {
            String query = "DELETE FROM Users WHERE username=:username";
            con.createQuery(query).bind(user).executeUpdate();
            return true;
        }
    }

    @Override
    public boolean update(User user) throws DaoException{
        try (Connection con = sql2o.open()) {

            //Check if the user exists or not
            String userQuery = "SELECT * FROM Users WHERE username=:username";
            List<User> userRes = con.createQuery(userQuery).bind(user).executeAndFetch(User.class);
            if (userRes.isEmpty()) {
                throw new DaoException("This user does not exist in the database!");
            }

            String query = "Update Users SET " +
                    "username=:username, " +
                    "followers=:followers, " +
                    "following=:following " +
                    "WHERE username=:username";
            con.createQuery(query).bind(user).executeUpdate();
            return true;
        }
    }

    public boolean exists(User u) throws DaoException{
        try (Connection con = sql2o.open()) {
            String query = "SELECT FROM Users WHERE username=:username";
            List<User> res = con.createQuery(query).addParameter("username", u.getUsername()).executeAndFetch(User.class);
            return (!res.isEmpty());
        }
    }

}
