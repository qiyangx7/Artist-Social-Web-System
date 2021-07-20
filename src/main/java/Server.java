import com.google.gson.Gson;
import exception.DaoException;
import model.*;
import org.sql2o.Sql2o;
import persistence.*;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.velocity.VelocityTemplateEngine;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static spark.Spark.*;


public class Server {

    private static String url = "jdbc:postgresql://localhost:5432/wip";
    private static String user = "postgres";
    private static String password = "wipapp";
    private static final int PORT = 7000;

    //gets Sql2o for wipapp
    private static Sql2o getSql2o() {
        try {
            getConnection();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return new Sql2o(url, user, password);
    }

    private static int getHerokuAssignedPort() {
        String herokuPort = System.getenv("PORT");
        if (herokuPort != null) {
            return Integer.parseInt(herokuPort);
        }
        return PORT;
    }

    private static String getJksPath() {
        String herokuJksPath = System.getenv("JKS_PATH");
        if (herokuJksPath != null) {
            return herokuJksPath;
        }
        return "src/main/resources/deploy/wip.jks";
    }

    private static void getConnection() throws URISyntaxException {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null) {
            // Not on Heroku, keep default settings
            return;
        }

        URI dbUri = new URI(databaseUrl);

        user = dbUri.getUserInfo().split(":")[0];
        password = dbUri.getUserInfo().split(":")[1];
        url = "jdbc:postgresql://" + dbUri.getHost() + ':'
                + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";

        return;
    }

    public static void main(String[] args)  {

        // if we're running locally, we need to set up a secure connection via jks
        String herokuJksPath = System.getenv("JKS_PATH");
        if (herokuJksPath == null) {
            secure(getJksPath(), "wipapp", null, null);
        }

        // set port number
        port(getHerokuAssignedPort());

        // specify resource files location
        staticFiles.location("/public");

        // request for index page
        post("/", (req, res) -> {
            String username = req.queryParams("username");
            res.cookie("username", username);
            res.redirect("/");
            return null;
        });

        // handle index page request
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            if (checkUser(req, res, model)) {

                Sql2oUserDao userDao = new Sql2oUserDao(getSql2o());
                String currentUsername = req.cookie("username");
                User currentUser = userDao.getUserByUsername(currentUsername);

                List<Work> feeds = null;
                String following = currentUser.getFollowing();
                if (following.equals("")) {
                    following += currentUser.getId();
                } else {
                    following += "," + currentUser.getId();
                }
//            System.out.println(following);
                String[] followingArray = following.split(",");
                feeds = new Sql2oWorkDao(getSql2o()).feeds(followingArray);
                //List<Work> feeds = new Sql2oWorkDao(getSql2o()).feeds();
                model.put("feeds", feeds);
            }

            res.status(200);
            res.type("text/html");
            return new ModelAndView(model, "public/templates/index.vm");
        }, new VelocityTemplateEngine());

        //logout function
        post("/logout", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // remove username from cookie
            res.removeCookie("username");
//            res.status(200);
//            res.type("text/html");
//            return new ModelAndView(model, "public/templates/index.vm");
            res.redirect("/");
            return null;
        });

        // getting all the works from the database and render them as list of links
        get("/works", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            // user check
            if (!checkUser(req, res, model)) {
                res.redirect("/");
            }
            List<Work> works = new Sql2oWorkDao(getSql2o()).listAll();
            model.put("works", works);
            res.status(200);
            res.type("text/html");
            return new ModelAndView(model, "public/templates/works.vm");
        }, new VelocityTemplateEngine());

        // list all works by the logged in user
        get("/myworks", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            //User check
            if (!checkUser(req, res, model)) {
                res.redirect("/");
            }
            int userId = new Sql2oUserDao(getSql2o()).getId(new User(req.cookie("username")));
            // uses the userID to get all works with that Id
            List<Work> authorWorks = new Sql2oWorkDao(getSql2o()).listByAuthor(userId);
            model.put("works", authorWorks);
            res.status(200);
            res.type("text/html");
            return new ModelAndView(model, "public/templates/works.vm");
        }, new VelocityTemplateEngine());

        // list all users in the system (should we still have this page?)
        // could be helpful in future if you want to search works by user?
        get("/users", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            if (!checkUser(req, res, model)) {
                res.redirect("/");
            }
            model.put("users", new Sql2oUserDao(getSql2o()).listAll());
            res.status(200);
            res.type("text/html");
            return new ModelAndView(model, "public/templates/users.vm");
        }, new VelocityTemplateEngine());

        // viewing a user
        get("/users/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            Map<String, Object> model = new HashMap<>();
            if (!checkUser(req, res, model)) {
                res.redirect("/");
            }
            // This line relies on the invariance that each registered user will have a corresponding
            // userProfile; we need to actively maintain this invariance
            Sql2oUserProfileDao updao = new Sql2oUserProfileDao(getSql2o());
            UserProfile userProfile = updao.getUserProfileById(id);
            List<Work> works = new Sql2oWorkDao(getSql2o()).listAll();
            Sql2oUserDao udao = new Sql2oUserDao(getSql2o());
            User profileUser = udao.getUserById(id);
            User currentUser = udao.getUserByUsername(req.cookie("username"));

            //gets favorite list from the list of works if it exists
            //should be displayed in the profile
            String favorites = userProfile.getFavoriteWorks();
            if (!favorites.isEmpty()){
                String[] favArray = favorites.split(",");
                StringBuilder favString = new StringBuilder();
                for (String favId : favArray) {
                    for (Work work : works) {
                        if (work.getId() == Integer.parseInt(favId)) {
                            favString.append(work.getTitle());
                            favString.append(";");
                            favString.append(favId);
                            favString.append(",");
                        }
                    }
                }
                favorites = favString.toString();
            }
            boolean isFollower = profileUser.isFollower(currentUser);

            //everything needed for display
            model.put("profileUser", profileUser);
            model.put("currentUser", currentUser);
            model.put("userProfile", userProfile);
            model.put("isFollower", isFollower);
            model.put("favorites", favorites);
            res.status(200);
            res.type("text/html");
            return new ModelAndView(model, "public/templates/userprofile.vm");
        }, new VelocityTemplateEngine());

        // adding new user interface
        get("/adduser", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            if (!checkUser(req, res, model)) {
                res.redirect("/");
            }
            res.status(200);
            res.type("text/html");
            return new ModelAndView(model, "public/templates/adduser.vm");
        }, new VelocityTemplateEngine());

        // handler for adding new user request
        post("/adduser", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String username = req.queryParams("username");
            User user = new User(username);
            try {
                int id = new Sql2oUserDao(getSql2o()).add(user);
                if (id > 0) {
                    model.put("added", "true");
                    // if the add succeeds, we create an empty profile for them automatically
                    UserProfile newProfile = new UserProfile(user);
                    new Sql2oUserProfileDao(getSql2o()).add(newProfile);
                }
                else {
                    model.put("failedAdd", "true");
                }
            }
            catch (DaoException ex) {
                model.put("failedAdd", "true");
            }
            res.status(201);
            res.type("text/html");
            ModelAndView mdl = new ModelAndView(model, "public/templates/adduser.vm");
            return new VelocityTemplateEngine().render(mdl);
        });

        // adding a follower
        post("/addfollower", (req, res) -> {

            // getting the user and follower in question here
            int userId = Integer.parseInt(req.queryParams("userId"));
//            System.out.println("userId: " + userId);
            int followerId = Integer.parseInt(req.queryParams("followerId"));
//            System.out.println("followerId: " + followerId);
            Sql2oUserDao sql2oUser = new Sql2oUserDao(getSql2o());
            User user = sql2oUser.getUserById(userId);
            User follower = sql2oUser.getUserById(followerId);
            user.addFollower(follower);

//            System.out.println("--------");
//            System.out.println(user.getFollowers());
//            System.out.println(follower.getFollowing());
//            System.out.println("--------");

            sql2oUser.update(user);
            sql2oUser.update(follower);

            res.status(201);
            //need to delete work once user has List<Works>
            String s = "Follower " + follower.getUsername() + " has been added!";
            res.type("application/json");

            return new Gson().toJson(s);
        });

        // removing a follower
        post("/removefollower", (req, res) -> {

            // getting the user and follower in question here
            int userId = Integer.parseInt(req.queryParams("userId"));
//            System.out.println("userId: " + userId);
            int followerId = Integer.parseInt(req.queryParams("followerId"));
//            System.out.println("followerId: " + followerId);
            Sql2oUserDao sql2oUser = new Sql2oUserDao(getSql2o());
            User user = sql2oUser.getUserById(userId);
            User follower = sql2oUser.getUserById(followerId);
            user.removeFollower(follower);

//            System.out.println("--------");
//            System.out.println(user.getFollowers());
//            System.out.println(follower.getFollowing());
//            System.out.println("--------");

            sql2oUser.update(user);
            sql2oUser.update(follower);

            res.status(201);
            //need to delete work once user has List<Works>
            String s = "Follower " + follower.getUsername() + " has been removed!";
            res.type("application/json");

            return new Gson().toJson(s);
        });

        // adding new work interface
        get("/addwork", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            if (!checkUser(req, res, model)) {
                res.redirect("/");
            }            res.status(200);
            res.type("text/html");
            return new ModelAndView(model, "public/templates/addwork.vm");
        }, new VelocityTemplateEngine());

        // handler for adding new user request
        post("/addwork", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String curUser = req.cookie("username");

            // getting information about the new work
            String title = req.queryParams("title");
            int status = Integer.parseInt(req.queryParams("status"));
            int creatorId = new Sql2oUserDao(getSql2o()).getId(new User(curUser));
            int currentWorkUpdateId = 0;
            // creating new work
            Work w = new Work(creatorId, title, status, currentWorkUpdateId);

            //makes sure that failing to add work doesn't crash the app
            try {
                Sql2oWorkDao workDao = new Sql2oWorkDao(getSql2o());
                int workID = workDao.add(w);
                if (workID > 0) {
                    try {
                        // getting the information to make the workUpdate
                        String description = req.queryParams("description");
                        String content = req.queryParams("content");
                        byte[] byteFile = content.getBytes();

                        // adding the corresponding workUpdate
                        Sql2oWorkUpdateDao workUpdateDao = new Sql2oWorkUpdateDao(getSql2o());
                        WorkUpdate wu = new WorkUpdate(1, workID,
                                0,
                                description, byteFile);
                        int wuId = workUpdateDao.add(wu);

                        // updating the currentWorkUpdateId field in the work
                        w.setCurrentWorkUpdateId(wuId);
                        workDao.update(w);
                        model.put("added", "true");
                    }
                    catch (DaoException ex) {
                        model.put("failedAdd", "true");
                    }
                }
                else {
                    model.put("failedAdd", "true");
                }
            }
            catch (DaoException ex) {
                model.put("failedAdd", "true");
            }
            res.status(201);
            res.type("text/html");
            ModelAndView mdl = new ModelAndView(model, "public/templates/index.vm");
            return new VelocityTemplateEngine().render(mdl);
        });

        // viewing a userProfile
        get("/works/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            Map<String, Object> model = new HashMap<>();
            if (!checkUser(req, res, model)) {
                res.redirect("/");
            }
            // setting up displaying current iteration
            int userId = new Sql2oUserDao(getSql2o()).getId(new User(req.cookie("username")));
            WorkUpdate latestWu = new Sql2oWorkUpdateDao(getSql2o()).listLast(id);
            Work currentW = new Sql2oWorkDao(getSql2o()).getWorkById(latestWu.getWorkId());

            // checking whether the user is the author of this work
            if (currentW.getCreatorId() == userId) {
                model.put("isAuthor", true);
            }

            // getting the author user and the work string
            User author = new Sql2oUserDao(getSql2o()).getUserById(currentW.getCreatorId());
            String latestWorkContent = new String(latestWu.getWorkFile());

            model.put("author", author);
            model.put("currentWork", currentW);
            model.put("latestUpdate", latestWu);
            model.put("workContent", latestWorkContent);

            // checks whether this work has previous iterations
            if (latestWu.getPrevItId() < 1) {
                model.put("prevItExists", false);
            } else {
                model.put("prevItExists", true);
            }

            // Setting up showing all comments for current iterations
            List<Comment> comments = new Sql2oCommentDao(getSql2o()).listCommentByWorkUpdate(latestWu.getId());
            model.put("comments", comments);

            // Pedro addition - set up allowing to see who authored a comment
            List<User> allUsers = new Sql2oUserDao(getSql2o()).listAll();
            model.put("users", allUsers);

            res.status(200);
            res.type("text/html");
            return new ModelAndView(model, "public/templates/displaywork.vm");
        }, new VelocityTemplateEngine());

        // viewing specific iteration of a work
        get("/works/:id/:itId", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            int itId = Integer.parseInt(req.params("itId"));
            Map<String, Object> model = new HashMap<>();
            if (!checkUser(req, res, model)) {
                res.redirect("/");
            }
            // Setting up displaying specific iteration
            int userId = new Sql2oUserDao(getSql2o()).getId(new User(req.cookie("username")));
            WorkUpdate wu = new Sql2oWorkUpdateDao(getSql2o()).getSpecificIteration(id,itId);
            Work currentW = new Sql2oWorkDao(getSql2o()).getWorkById(wu.getWorkId());
            if (currentW.getCreatorId() == userId) {
                model.put("isAuthor", true);
            }
            //all info needed when displaying a work
            String itWorkContent = new String(wu.getWorkFile());
            model.put("currentWorkUpdate", wu);
            model.put("workContent", itWorkContent);
            model.put("workId", id);

            if (wu.getPrevItId() < 1) {
                model.put("prevItExists", false);
            } else {
                model.put("prevItExists", true);
            }

            //Setting up showing comments for current iteration
            List<Comment> comments = new Sql2oCommentDao(getSql2o()).listCommentByWorkUpdate(wu.getId());
            model.put("comments", comments);

            model.put("users", new Sql2oUserDao(getSql2o()).listAll());

            res.status(200);
            res.type("text/html");
            return new ModelAndView(model, "public/templates/displayiteration.vm");
        }, new VelocityTemplateEngine());

        //updatework route; update a work
        get("/updatework", (req, res) -> {
            int workId = Integer.parseInt(req.queryParams("id"));
            Map<String, Object> model = new HashMap<>();
            if (!checkUser(req, res, model)) {
                res.redirect("/");
            }
            model.put("workId", workId);

            res.status(201);
            res.type("text/html");
            return new ModelAndView(model, "public/templates/updatework.vm");
        }, new VelocityTemplateEngine());

        //updatework route; update a work
        post("/updatework", (req, res) -> {
            int workId = Integer.parseInt(req.queryParams("id"));
            String title = req.queryParams("title");
            int status = Integer.parseInt(req.queryParams("status"));
            String description = req.queryParams("description");
            String content = req.queryParams("content");

            byte[] byteFile = content.getBytes();
            //update iteration/previtid in future updates
            Sql2oWorkUpdateDao workUpdateDao = new Sql2oWorkUpdateDao(getSql2o());
            WorkUpdate lastUpdate = workUpdateDao.listLast(workId);
            WorkUpdate wu = new WorkUpdate(lastUpdate.getIteration()+1, workId,
                    lastUpdate.getId(),
                    description, byteFile);
            int wuId = workUpdateDao.add(wu);

            // Updating the currentWorkUpdateId field in the work
            Sql2oWorkDao workDao = new Sql2oWorkDao(getSql2o());
            Work w = workDao.getWorkById(workId);
            w.setTitle(title);
            w.setCurrentWorkUpdateId(wuId);
            w.setCompletionStatus(status);
            workDao.update(w);

            res.status(201);
            res.type("application/json");
            return new Gson().toJson(wu.toString());
        });

        //deluser route; delete a user
        post("/deluser", (req, res) -> {
            String name = req.queryParams("name");
            Sql2oUserDao sql2oUser = new Sql2oUserDao(getSql2o());

            sql2oUser.delete(new User(name));
            res.status(201);
            //need to delete work once user has List<Works>
            String s = "User " + name + " has been deleted!";
            res.type("application/json");

            return new Gson().toJson(s);
        });

        //delwork route; delete a work
        post("/delwork", (req, res) -> {
            String idString = req.queryParams("id");
            int id = Integer.parseInt(idString);

            Sql2oWorkDao sql2oWork = new Sql2oWorkDao(getSql2o());
            sql2oWork.delete(new Work(id));

            res.status(201);
            String s = "Work " + id + " deleted";
            res.type("application/json");
            return new Gson().toJson(s);

        });

        //add comment post
        post("/addcomment", (req, res) -> {
            //need id for commenter, the writer of the work and the work Id
            int userId = new Sql2oUserDao(getSql2o()).getId(new User(req.cookie("username")));
            int parentId = Integer.parseInt(req.queryParams("parentId"));
            int workUpdateId = Integer.parseInt(req.queryParams("workUpdateId"));
            String content = req.queryParams("comment");

            Comment comment = new Comment(userId, workUpdateId, parentId, content);

            // System.out.println(comment.getComment());

            int id = new Sql2oCommentDao(getSql2o()).add(comment);

            res.status(201);
            String s = "Comment " + id + " added";
            res.type("application/json");
            return new Gson().toJson(s);
        });

        //seeing a userProfile
        get("/userProfile", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            if (!checkUser(req, res, model)) {
                res.redirect("/");
            }
            List<UserProfile> profList = new Sql2oUserProfileDao(getSql2o()).listAll();
            UserProfile prof = profList.get(0);
            model.put("userProfile", prof);
            res.status(200);
            res.type("text/html");
            return new ModelAndView(model, "public/templates/userprofile.vm");
        }, new VelocityTemplateEngine());

        post("/updateuserprofile", (req, res) -> {
            int userId = Integer.parseInt(req.queryParams("userId"));
            int status = Integer.parseInt(req.queryParams("isPublic"));
            boolean isPublic;
            if (status == 0) {
                isPublic = false;
            }
            else {
                isPublic = true;
            }
            String favoriteWorks = req.queryParams("favoriteWorks");
            System.out.println(favoriteWorks);
            String bio = req.queryParams("bio");

            Sql2oUserProfileDao updao = new Sql2oUserProfileDao(getSql2o());
            UserProfile currentProfile = updao.getUserProfileById(userId);
//            currentProfile.setPublic(isPublic);
            currentProfile.setFavoriteWorks(favoriteWorks);
            currentProfile.setBio(bio);
            updao.update(currentProfile);

            System.out.println("You are here!");

            res.status(201);
            res.type("application/json");
            return new Gson().toJson(currentProfile.toString());
        });

        post("/addFav", (req, res) -> {
            //need user Id for favorite list
            int userId = new Sql2oUserDao(getSql2o()).getId(new User(req.cookie("username")));
            String workId = req.queryParams("workId");
            Sql2oUserProfileDao userProfDao = new Sql2oUserProfileDao(getSql2o());
            // System.out.println(userId);

            UserProfile targetUser = userProfDao.getUserProfileById(userId);
            // System.out.println(targetUser.getId());

            //append a String to search and add to favoriteWork
            String favWorkString = targetUser.getFavoriteWorks();

            String[] favWorkArr = favWorkString.split(",");
            boolean alreadyFaved = Arrays.stream(favWorkArr).anyMatch(n -> (n.equals(workId)));
            if (!alreadyFaved) {
                favWorkString = favWorkString + workId + ",";
            }
            targetUser.setFavoriteWorks(favWorkString);
            userProfDao.update(targetUser);
            res.status(201);
            res.type("application/json");
            return new Gson().toJson(favWorkString);
        });

        //removing from favorite list
        post("/removeFav", (req, res) -> {
            int userId = new Sql2oUserDao(getSql2o()).getId(new User(req.cookie("username")));
            String workId = req.queryParams("workId");
            Sql2oUserProfileDao userProfDao = new Sql2oUserProfileDao(getSql2o());
            // System.out.println(userId);
            UserProfile targetUser = userProfDao.getUserProfileById(userId);

            //remove Favorite and update userProfDao
            targetUser.removeFavorite(workId);
            userProfDao.update(targetUser);
            res.status(201);
            res.type("application/json");
            return new Gson().toJson(targetUser.getFavoriteWorks());
        });
    }

    private static boolean checkUser(Request req, Response res, Map<String, Object> model) {
        String currentUsername = req.cookie("username");

        // if we do have a username registered in the cookie
        if (currentUsername != null) {
            Sql2oUserDao udao = new Sql2oUserDao(getSql2o());

            // the corner case where username cookie isn't empty, but somehow isn't
            // registered in our database
            if (!udao.exists(new User(currentUsername))){
                // removing their cookie so that they have to go through the
                // add user process to enter the database
                res.removeCookie("username");
                return false;
            }
            // paranoia check passed; user is legal and in our database
            else {
                User currentUser = udao.getUserByUsername(currentUsername);
                model.put("currentUser", currentUser);
                return true;
            }
        }

        return false;
    }

}
