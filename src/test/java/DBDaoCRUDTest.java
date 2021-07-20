import model.User;
import model.UserProfile;
import model.Work;
import model.WorkUpdate;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sql2o.Sql2o;
import persistence.Sql2oUserDao;
import persistence.Sql2oUserProfileDao;
import persistence.Sql2oWorkDao;
import persistence.Sql2oWorkUpdateDao;

import javax.swing.plaf.nimbus.State;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DBDaoCRUDTest {

//    private static String URI;
    private static Connection conn;
    private static Statement st;

    private static final String url = "jdbc:postgresql://localhost:5432/wip";
    private static final String user = "postgres";
    private static final String password = "wipapp";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    @BeforeClass
    public static void beforeClassTests() throws SQLException {
        conn = connect();
        st = conn.createStatement();

        String sql = "DROP TABLE IF EXISTS Users CASCADE";
        st.execute(sql);

        sql = "DROP TABLE IF EXISTS Works CASCADE";
        st.execute(sql);

        sql = "DROP TABLE IF EXISTS WorkUpdates CASCADE";
        st.execute(sql);

        sql = "DROP TABLE IF EXISTS Comments CASCADE";
        st.execute(sql);

        sql = "DROP TABLE IF EXISTS UserProfiles CASCADE";
        st.execute(sql);
    }

    @Before
    public void beforeEachTest() throws SQLException {

        String sql = "DROP TABLE IF EXISTS Users CASCADE";
        st.execute(sql);

        sql = "DROP TABLE IF EXISTS Works CASCADE";
        st.execute(sql);

        sql = "DROP TABLE IF EXISTS WorkUpdates CASCADE";
        st.execute(sql);

        sql = "DROP TABLE IF EXISTS Comments CASCADE";
        st.execute(sql);

        sql = "DROP TABLE IF EXISTS UserProfiles CASCADE";
        st.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS Users (id SERIAL PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL UNIQUE, " +
                "followers VARCHAR(100), " +
                "following VARCHAR(100));";
        st.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS Works (id SERIAL PRIMARY KEY," +
                " title VARCHAR NOT NULL, completionStatus INTEGER NOT NULL, currentWorkUpdateId INTEGER NOT NULL," +
                " creatorId INTEGER NOT NULL REFERENCES Users(id)" +
                " ON UPDATE CASCADE ON DELETE CASCADE);";
        st.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS WorkUpdates (id SERIAL PRIMARY KEY," +
                " iteration INTEGER NOT NULL, prevItId INTEGER NOT NULL, description VARCHAR," +
                " workFile bytea NOT NULL," +
                " workId INTEGER NOT NULL REFERENCES Works(id)" +
                " ON UPDATE CASCADE ON DELETE CASCADE);";
        st.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS Comments (id SERIAL PRIMARY KEY," +
                " parentId INTEGER NOT NULL, comment VARCHAR NOT NULL, " +
                " UserId INTEGER NOT NULL REFERENCES Users(id)" +
                " ON UPDATE CASCADE ON DELETE CASCADE," +
                " workUpdateId INTEGER NOT NULL REFERENCES workUpdates(id)" +
                " ON UPDATE CASCADE ON DELETE CASCADE);";
        st.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS UserProfiles (id SERIAL PRIMARY KEY," +
                " userID INTEGER NOT NULL REFERENCES Users(id)" +
                " ON UPDATE CASCADE ON DELETE CASCADE," +
                " isPublic BOOLEAN NOT NULL," +
                " bio VARCHAR (150)," +
                " favoriteWorks VARCHAR (3000));";

        st.execute(sql);
    }

    @Test
    public void testInsertUser1() throws SQLException {
        Sql2o sql2o = new Sql2o(url, user, password);
        Sql2oUserDao adao = new Sql2oUserDao(sql2o);
        Sql2oUserProfileDao updao = new Sql2oUserProfileDao(sql2o);
        int initial = adao.listAll().size();
        List<User> users = adao.listAll();
        assertEquals(initial, users.size());

        //Add two users
        User u1 = new User("test1");
        User u2 = new User("test2");
        adao.add(u1);
        adao.add(u2);

        UserProfile up1 = new UserProfile(u1);
        UserProfile up2 = new UserProfile(u2);
        updao.add(up1);
        updao.add(up2);

        //Get list of authors and verify size
        users = adao.listAll();
        assertEquals(initial + 2, users.size());
    }

    @Test
    public void testInsertWork1() throws SQLException {
        Sql2o sql2o = new Sql2o(url, user, password);

        //Create users to upload works
        Sql2oUserDao udao = new Sql2oUserDao(sql2o);
        Sql2oUserProfileDao updao = new Sql2oUserProfileDao(sql2o);
        //Add the users
        User u1 = new User("insertWorkUser1");
        int userId = udao.add(u1);
        UserProfile up1 = new UserProfile(u1);
        updao.add(up1);

        //Create works uploaded by user(s)
        Sql2oWorkDao wdao = new Sql2oWorkDao(sql2o);
        Work w1 = new Work(userId, "Test1", 0);
        int workId = wdao.add(w1);

        //Create workUpdates
        String testFile = "test!";
        byte[] byteFile = testFile.getBytes();

        Sql2oWorkUpdateDao wudao = new Sql2oWorkUpdateDao(sql2o);
        WorkUpdate wu1 = new WorkUpdate(1, workId, 0, "ah", byteFile);
        int wuId = wudao.add(wu1);
        w1.setCurrentWorkUpdateId(wuId);
        wdao.update(w1);

        //Get list of authors and verify size
        List<Work> works = wdao.listAll();
        // System.out.println(works);
    }

    @Test
    public void basicDeletePostWorks() throws SQLException{
        Sql2o sql2o = new Sql2o(url, user, password);

        //Create users to upload works
        Sql2oUserDao udao = new Sql2oUserDao(sql2o);
        Sql2oUserProfileDao updao = new Sql2oUserProfileDao(sql2o);
        //Add the users
        User u1 = new User("insertWorkUser2");
        int userId = udao.add(u1);

        UserProfile up1 = new UserProfile(u1);
        updao.add(up1);

        //Create works uploaded by user(s)
        Sql2oWorkDao wdao = new Sql2oWorkDao(sql2o);
        Work w1 = new Work(userId, "Test2", 0);
        int workId = wdao.add(w1);

        //delete work
        wdao.delete(w1);

        //make sure list is 0
        List<Work> works = wdao.listAll();
        assertEquals(0, works.size());
    }

    @Test
    public void basicDelUserTest() {
        Sql2o sql2o = new Sql2o(url, user, password);
        Sql2oUserDao adao = new Sql2oUserDao(sql2o);
        Sql2oUserProfileDao updao = new Sql2oUserProfileDao(sql2o);

        //Add two users
        User u1 = new User("testDel1");
        User u2 = new User("testDel2");
        adao.add(u1);
        adao.add(u2);

        UserProfile up1 = new UserProfile(u1);
        UserProfile up2 = new UserProfile(u2);
        updao.add(up1);
        updao.add(up2);

        List<User> users = adao.listAll();
        int ogSize = users.size();

        //Get list of authors and verify size
        adao.delete(u1);
        users = adao.listAll();
        assertEquals( ogSize - 1, users.size());

        adao.delete(u2);
        users = adao.listAll();
        assertEquals(ogSize - 2, users.size());

    }

    @Test
    public void testWorksByUser() {
        Sql2o sql2o = new Sql2o(url, user, password);
        Sql2oUserDao adao = new Sql2oUserDao(sql2o);
        Sql2oUserProfileDao updao = new Sql2oUserProfileDao(sql2o);
        int initial = adao.listAll().size();
        List<User> users = adao.listAll();
        assertEquals(initial, users.size());

        //Add two users
        User u1 = new User("test1");
        User u2 = new User("test2");
        adao.add(u1);
        adao.add(u2);

        UserProfile up1 = new UserProfile(u1);
        UserProfile up2 = new UserProfile(u2);
        updao.add(up1);
        updao.add(up2);

       //add work to the users
        Work w1 = new Work(u1.getId(), "Work1", 0);
        Work w2 = new Work(u2.getId(), "Work2", 0);
        Work w3 = new Work(u1.getId(), "Work3", 0);

        //add works to WorkDao
        Sql2oWorkDao works = new Sql2oWorkDao(sql2o);
        works.add(w1);
        works.add(w2);
        works.add(w3);

        List<Work> authorOneWorks = works.listByAuthor(u1.getId());
        assertEquals(2, authorOneWorks.size());
    }

    @Test
    public void userProfileParameters() {
        Sql2o sql2o = new Sql2o(url, user, password);

        Sql2oUserDao adao = new Sql2oUserDao(sql2o);
        Sql2oUserProfileDao apdao = new Sql2oUserProfileDao(sql2o);

        List<UserProfile> userProfiles = apdao.listAll();
        int initial = userProfiles.size();
        assertEquals(0, initial);
        assertEquals(initial, userProfiles.size());

        User u1 = new User("user1");
        int userID = adao.add(u1);

        UserProfile up1 = new UserProfile(u1);
        assertEquals(0, up1.getId());

        assertEquals(userID, up1.getUserID());
        assert(up1.isPublic());
        assertEquals("This user doesn't have anything to say about themselves!", up1.getBio());

        String bio = "Welcome to my profile!!!";
        up1.setBio(bio);
        assertEquals(bio, up1.getBio());

        int profileID = apdao.add(up1);
        assertEquals(profileID, up1.getId());

        assertEquals(1, apdao.listAll().size());
    }

}
