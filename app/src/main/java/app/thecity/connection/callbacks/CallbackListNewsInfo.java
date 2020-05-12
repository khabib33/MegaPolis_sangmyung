package app.thecity.connection.callbacks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import app.thecity.model.NewsInfo;
import app.thecity.model.Place;

public class CallbackListNewsInfo implements Serializable {

    public String status = "";
    public int count = -1;
    public int count_total = -1;
    public int pages = -1;
    public List<NewsInfo> news_infos = new ArrayList<>();

}
