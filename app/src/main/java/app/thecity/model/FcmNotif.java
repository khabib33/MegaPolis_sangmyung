package app.thecity.model;

import java.io.Serializable;

public class FcmNotif implements Serializable {

    public String title, content, type;
    public Place place;
    public NewsInfo news;

}
