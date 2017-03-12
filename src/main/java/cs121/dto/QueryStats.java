package cs121.dto;

public class QueryStats {
    private long totalTime;
    private int totalResponses;

    public QueryStats(long time, int count) {
        totalTime = time;
        totalResponses = count;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public int getTotalResponses() {
        return totalResponses;
    }

    public void setTotalResponses(int totalResponses) {
        this.totalResponses = totalResponses;
    }
}
