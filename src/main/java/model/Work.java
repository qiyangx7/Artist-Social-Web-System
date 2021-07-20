package model;

public class Work {
    private int id;
    private int creatorId;
    private String title;
    // 0 is incomplete, 1 is complete
    private int completionStatus;
    private int currentWorkUpdateId;
    private int time;

    // for deletion purposes
    public Work (int id) {
        this.id = id;
        this.creatorId = 0;
        this.title = "title";
        this.completionStatus = 0;
        this.currentWorkUpdateId = 0;
        this.time = 0;
    }

    public Work(int creatorId, String title, int status) {
        // This id should be set by the database; 0 is a flag that indicates some kind of initiation error
        this.id = 0;
        this.creatorId = creatorId;
        this.title = title;
        this.completionStatus = status;
        // This id should be set by the database; 0 is a flag that indicates some kind of initiation error
        this.currentWorkUpdateId = 0;
        this.time = 0;
    }

    public Work(int creatorId, String title, int status, int currentWorkUpdateId) {

        // This id should be set by the database; 0 is a flag that indicates some kind of initiation error
        this.id = 0;
        this.creatorId = creatorId;
        this.title = title;
        this.completionStatus = status;
        this.currentWorkUpdateId = currentWorkUpdateId;
        this.time = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(int status) {
        this.completionStatus = status;
    }

    public int getCurrentWorkUpdateId() {
        return currentWorkUpdateId;
    }

    public void setCurrentWorkUpdateId(int currentWorkUpdateId) {
        this.currentWorkUpdateId = currentWorkUpdateId;
    }

    public int getTime() { return time; }

    public void setTime(int time) { this.time = time; }

    //update once we get the file/work stuff configured
    @Override
    public String toString() {
        return "Work{" +
                "id=" + id +
                ", creatorId=" + creatorId +
                ", title='" + title + '\'' +
                ", completionStatus=" + completionStatus +
                ", currentWorkUpdateId=" + currentWorkUpdateId +
//                ", time=" + time +
                '}';
    }
}
