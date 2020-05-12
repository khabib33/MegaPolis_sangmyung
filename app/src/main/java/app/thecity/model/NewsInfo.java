package app.thecity.model;

import java.io.Serializable;

public class NewsInfo implements Serializable{

    public int id;
    public String title;
    public String brief_content;
    public String full_content;
    public String image;
    public long last_update;

}
