package model;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Collectors;

public class User {
    private int id;
    private String username;
    private String followers;
    private String following;

    public User(String username) {
        // This id should be set by the database; 0 is a flag that indicates some kind of initiation error
        this.id = 0;
        this.username = username;
        this.followers = "";
        this.following = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFollowers() {
        return followers;
    }

    public void setFollowers(String followers) {
        this.followers = followers;
    }

    public String getFollowing() {
        return following;
    }

    public void setFollowing(String following) {
        this.following = following;
    }

    // Check whether the user passed in as the argument is a follower of -this-
    public boolean isFollower(User user) {
        int checkId = user.getId();
//        System.out.println("isFollower info: \n checkId = " + checkId);
//        System.out.println("Here are your follower: " + this.getFollowers().equals(""));
        if (!(this.getFollowers().equals("") || this.getFollowers() == null)) {
            String[] followers = this.getFollowers().split(",");
//            System.out.println(Arrays.toString(followers));
            int[] numbers = Arrays.stream(followers).mapToInt(Integer::parseInt).toArray();
//            System.out.println(Arrays.toString(numbers));
            boolean result = Arrays.stream(numbers).anyMatch(n -> (n == checkId));
//            System.out.println(result);
//            System.out.println("\n\n");

            return result;
        }

        return false;
    }

    // Check whether -this- is following the user passed in as the argument
    public boolean isFollowing(User user) {
        int checkId = user.getId();

        if (!(this.getFollowing().equals("") || this.getFollowing() == null)) {
            String[] following = this.getFollowing().split(",");
            int[] numbers = Arrays.stream(following).mapToInt(Integer::parseInt).toArray();
            boolean result = Arrays.stream(numbers).anyMatch(n -> (n == checkId));

            return result;
        }

        return false;
    }

    // Add the user passed in as the argument to -this-'s following string
    public void addFollowing(User following){
        // checking whether we are already following this user
        if (!this.isFollowing(following)) {
            // System.out.println("Adding new following");
            int followingId = following.getId();
            String newFollowings = this.getFollowing();
            if (newFollowings.equals("")) {
                newFollowings = Integer.toString(followingId);
            }
            else {
                newFollowings = this.getFollowing() + "," + followingId;
            }

            this.setFollowing(newFollowings);
            // System.out.println(this.getFollowing());
            following.addFollower(this);
        }

//        System.out.println("Removing non-existent following");
    }

    // Add the user passed in as the argument to -this-'s follower string
    public void addFollower(User follower) {

        // checking whether this user is already our follower
        if (!this.isFollower(follower)) {
            int followerId = follower.getId();
            String newFollowers = this.getFollowers();
            if (newFollowers.equals("")) {
                newFollowers = Integer.toString(followerId);
            }
            else {
                newFollowers = this.getFollowers() + "," + followerId;
            }
            this.setFollowers(newFollowers);
            follower.addFollowing(this);
        }

    }

    public void removeFollowing(User following) {
        if (this.isFollowing(following)) {
            int followingId = following.getId();
            String[] followings = this.getFollowing().split(",");
            int[] numbers = Arrays.stream(followings).mapToInt(Integer::parseInt).filter(n -> n != followingId).toArray();
            String newFollowings = Arrays.stream(numbers).mapToObj(Integer::toString).collect(Collectors.joining(","));
//            System.out.println(newFollowings);

            this.setFollowing(newFollowings);
            following.removeFollower(this);
        }

//        System.out.println("Removing non-existent following");
    }

    public void removeFollower(User follower) {
        if (this.isFollower(follower)) {
            int followerId = follower.getId();
            String[] followers = this.getFollowers().split(",");
            int[] numbers = Arrays.stream(followers).mapToInt(Integer::parseInt).filter(n -> n != followerId).toArray();
            String newFollowers = Arrays.stream(numbers).mapToObj(Integer::toString).collect(Collectors.joining(","));
//            System.out.println(newFollowers);

            this.setFollowers(newFollowers);
            follower.removeFollowing(this);
        }

//        System.out.println("Removing non-existent follower");
    }
}
