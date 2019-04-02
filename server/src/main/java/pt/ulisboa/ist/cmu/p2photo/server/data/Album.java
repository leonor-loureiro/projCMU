package pt.ulisboa.ist.cmu.p2photo.server.data;

/**
 * This class represents a P2Photo album
 */
public class Album {

    //Album name
    private String name;
    //Album catalog url
    private String url;


    public Album(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
