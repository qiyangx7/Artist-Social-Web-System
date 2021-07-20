package model;

import org.apache.commons.collections.buffer.BlockingBuffer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserProfile {
    private int id;
    private int userID;
    private boolean isPublic;
    private String favoriteWorks;
    private String bio;
    private Byte[] profilePic;

    public UserProfile(User user) {
        this.id = 0;
        this.userID = user.getId();
        this.isPublic = true; //automatically set to public
        this.bio = "This user doesn't have anything to say about themselves!";
        this.favoriteWorks = "";
        this.profilePic = null;
        //profilePic = defuaultIcon; TODO: Update when we figure out image storage and frontend display better
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getFavoriteWorks() {
        return favoriteWorks;
    }

    public void setFavoriteWorks(String favoriteWorks) {
        this.favoriteWorks = favoriteWorks;
    }

    public void removeFavorite(String workId) {
        int workIdInt = Integer.parseInt(workId);
        String[] favArray = this.getFavoriteWorks().split(",");
        int[] favIds = Arrays.stream(favArray)
                .mapToInt(Integer::parseInt)
                .filter(n -> n != workIdInt)
                .toArray();
        String newFavList = Arrays.stream(favIds)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(","));
        this.setFavoriteWorks(newFavList);
    };

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Byte[] getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(Byte[] profilePic) {
        this.profilePic = profilePic;
    }
}

