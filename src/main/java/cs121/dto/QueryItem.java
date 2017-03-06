package cs121.dto;

public class QueryItem {

    private String title;
    private String url;
    private String description;
    private double score;

    public QueryItem(String title, String url, String description, double score) {
        super();
        this.title = title;
        this.url = url;
        this.description = description;
        this.score = score;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

}
