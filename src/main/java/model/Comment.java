package model;

public class Comment {
    private int id;
    private int userId;
    private int workUpdateId;
    private int parentId;
    private String comment;

    public Comment(int uid, int wid, int pid, String comment) {
        // This id should be set by the database; 0 is a flag that indicates some kind of initiation error
        this.id = 0;
        this.userId = uid;
        this.workUpdateId = wid;
        this.comment = comment;
        // A comment that doesn't have a reply yet by default has parentId set to 0 as a flag
        this.parentId = pid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getWorkUpdateId() {
        return workUpdateId;
    }

    public void setWorkUpdateId(int workUpdateId) {
        this.workUpdateId = workUpdateId;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
