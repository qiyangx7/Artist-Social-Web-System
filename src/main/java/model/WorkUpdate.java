package model;

import java.util.List;
import java.io.File;

public class WorkUpdate {
    private int id;
    private int workId;
    private int iteration;
    private int prevItId;
    private String description;
    private byte[] workFile;

    public WorkUpdate(int iteration, int workId, int prevItId, String description, byte[] workFile) {
        this.id = 0;
        this.workId = workId;
        this.prevItId = prevItId;
        this.iteration = iteration;
        this.description = description;
        this.workFile = workFile;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWorkId() {
        return workId;
    }

    public void setWorkId(int workId) {
        this.workId = workId;
    }

    public int getPrevItId() {
        return prevItId;
    }

    public void setPrevItId(int prevItId) {
        this.prevItId = prevItId;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getWorkFile() {
        return workFile;
    }

    public void setWorkFile(byte[] workFile) {
        this.workFile = workFile;
    }

    @Override
    public String toString() {
        return "WorkUpdate{" +
                "id=" + id +
                ", workId=" + workId +
                ", iteration='" + iteration + '\'' +
                ", description=" + description +
                '}';
    }

}
