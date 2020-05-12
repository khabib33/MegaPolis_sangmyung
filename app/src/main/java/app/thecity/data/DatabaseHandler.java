package app.thecity.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import app.thecity.R;
import app.thecity.model.Category;
import app.thecity.model.Images;
import app.thecity.model.NewsInfo;
import app.thecity.model.Place;
import app.thecity.utils.Tools;

public class DatabaseHandler extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    private Context context;

    // Database Version
    private static final int DATABASE_VERSION = 4;

    // Database Name
    private static final String DATABASE_NAME = "the_city";

    // Main Table Name
    private static final String TABLE_PLACE         = "place";
    private static final String TABLE_IMAGES        = "images";
    private static final String TABLE_CATEGORY      = "category";
    private static final String TABLE_NEWS_INFO     = "news_info";

    // Relational table Place to Category ( N to N )
    private static final String TABLE_PLACE_CATEGORY = "place_category";

    // table only for android client
    private static final String TABLE_FAVORITES     = "favorites_table";

    // Table Columns names TABLE_PLACE
    private static final String KEY_PLACE_ID    = "place_id";
    private static final String KEY_NAME        = "name";
    private static final String KEY_IMAGE       = "image";
    private static final String KEY_ADDRESS     = "address";
    private static final String KEY_PHONE       = "phone";
    private static final String KEY_WEBSITE     = "website";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_LNG         = "lng";
    private static final String KEY_LAT         = "lat";
    private static final String KEY_DISTANCE    = "distance";
    private static final String KEY_LAST_UPDATE = "last_update";

    // Table Columns names TABLE_IMAGES
    private static final String KEY_IMG_PLACE_ID    = "place_id";
    private static final String KEY_IMG_NAME        = "name";

    // Table Columns names TABLE_CATEGORY
    private static final String KEY_CAT_ID      = "cat_id";
    private static final String KEY_CAT_NAME    = "name";
    private static final String KEY_CAT_ICON    = "icon";

    // Table Columns names TABLE_NEWS_INFO
    private static final String KEY_NEWS_ID             = "id";
    private static final String KEY_NEWS_TITLE          = "title";
    private static final String KEY_NEWS_BRIEF_CONTENT  = "brief_content";
    private static final String KEY_NEWS_FULL_CONTENT   = "full_content";
    private static final String KEY_NEWS_IMAGE          = "image";
    private static final String KEY_NEWS_LAST_UPDATE    = "last_update";

	// Table Relational Columns names TABLE_PLACE_CATEGORY
    private static final String KEY_RELATION_PLACE_ID = KEY_PLACE_ID;
    private static final String KEY_RELATION_CAT_ID = KEY_CAT_ID;

    private int cat_id[]; // category id
    private String cat_name[]; // category name
    private TypedArray cat_icon; // category name

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.db = getWritableDatabase();

        // get data from res/values/category.xml
        cat_id = context.getResources().getIntArray(R.array.id_category);
        cat_name = context.getResources().getStringArray(R.array.category_name);
        cat_icon = context.getResources().obtainTypedArray(R.array.category_icon);

        // if length not equal refresh table category
        if(getCategorySize() != cat_id.length) {
            defineCategory(this.db);  // define table category
        }

    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase d) {
        createTablePlace(d);
        createTableImages(d);
        createTableCategory(d);
        createTableRelational(d);
        createTableFavorites(d);
        createTableNewsInfo(d);
    }

    private void createTablePlace(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_PLACE + " ("
                + KEY_PLACE_ID + " INTEGER PRIMARY KEY, "
                + KEY_NAME + " TEXT, "
                + KEY_IMAGE + " TEXT, "
                + KEY_ADDRESS + " TEXT, "
                + KEY_PHONE + " TEXT, "
                + KEY_WEBSITE + " TEXT, "
                + KEY_DESCRIPTION + " TEXT, "
                + KEY_LNG + " REAL, "
                + KEY_LAT + " REAL, "
                + KEY_DISTANCE + " REAL, "
                + KEY_LAST_UPDATE + " NUMERIC "
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    private void createTableImages(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_IMAGES + " ("
                + KEY_IMG_PLACE_ID + " INTEGER, "
                + KEY_IMG_NAME + " TEXT, "
                + " FOREIGN KEY(" + KEY_IMG_PLACE_ID + ") REFERENCES " + TABLE_PLACE + "(" + KEY_PLACE_ID + ")"
                + " )";
        db.execSQL(CREATE_TABLE);
    }

    private void createTableCategory(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_CATEGORY + "("
                + KEY_CAT_ID + " INTEGER PRIMARY KEY, "
                + KEY_CAT_NAME + " TEXT, "
                + KEY_CAT_ICON + " INTEGER"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    private void createTableFavorites(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + KEY_PLACE_ID + " INTEGER PRIMARY KEY "
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    private void defineCategory(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + TABLE_CATEGORY); // refresh table content
        db.execSQL("VACUUM");
        for (int i = 0; i < cat_id.length; i++) {
            ContentValues values = new ContentValues();
            values.put(KEY_CAT_ID, cat_id[i]);
            values.put(KEY_CAT_NAME, cat_name[i]);
            values.put(KEY_CAT_ICON, cat_icon.getResourceId(i, 0));
            db.insert(TABLE_CATEGORY, null, values); // Inserting Row
        }
    }

	// Table Relational place_category
    private void createTableRelational(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_PLACE_CATEGORY + "("
                + KEY_RELATION_PLACE_ID + " INTEGER, "      // id from table place
                + KEY_RELATION_CAT_ID + " INTEGER "        // id from table category
                + ")";
        db.execSQL(CREATE_TABLE);
    }


    private void createTableNewsInfo(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NEWS_INFO+ " ("
                + KEY_NEWS_ID+ " INTEGER PRIMARY KEY, "
                + KEY_NEWS_TITLE+ " TEXT, "
                + KEY_NEWS_BRIEF_CONTENT+ " TEXT, "
                + KEY_NEWS_FULL_CONTENT+ " TEXT, "
                + KEY_NEWS_IMAGE+ " TEXT, "
                + KEY_NEWS_LAST_UPDATE+ " NUMERIC "
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DB ", "onUpgrade "+oldVersion+" to "+newVersion);
        if(oldVersion < newVersion) {
            // Drop older table if existed
            truncateDB(db);
        }
    }

    public void truncateDB(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS_INFO);

        // Create tables again
        onCreate(db);
    }

    // refresh table place and place_category
    public void refreshTablePlace(){
        db.execSQL("DELETE FROM " + TABLE_PLACE_CATEGORY);
        db.execSQL("VACUUM");
        db.execSQL("DELETE FROM " + TABLE_IMAGES);
        db.execSQL("VACUUM");
        db.execSQL("DELETE FROM " + TABLE_PLACE);
        db.execSQL("VACUUM");
    }

    // refresh table place and place_category
    public void refreshTableNewsInfo(){
        db.execSQL("DELETE FROM " + TABLE_NEWS_INFO);
        db.execSQL("VACUUM");
    }


    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Insert List place
    public void insertListPlace(List<Place> modelList) {
        modelList = Tools.itemsWithDistance(context, modelList);
        for (Place p : modelList) {
            ContentValues values = getPlaceValue(p);
            // Inserting or Update Row
            db.insertWithOnConflict(TABLE_PLACE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            // Insert relational place with category
            insertListPlaceCategory(p.place_id, p.categories);
            // Insert Images places
            insertListImages(p.images);
        }
    }

    // Insert List place with asynchronous scheme
    public void insertListPlaceAsync(List<Place> modelList) {
        String sql = "INSERT OR REPLACE INTO "+TABLE_PLACE + " ";
        sql = sql + "("+KEY_PLACE_ID+", "+KEY_NAME+","+KEY_IMAGE+", "+KEY_ADDRESS+", "+KEY_PHONE +", "
                +KEY_WEBSITE+", "+KEY_DESCRIPTION+", "+KEY_LNG+", "+KEY_LAT+", "+KEY_DISTANCE+", "+KEY_LAST_UPDATE+") ";
        sql = sql + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        db.beginTransaction();

        SQLiteStatement stmt = db.compileStatement(sql);
        modelList = Tools.itemsWithDistance(context, modelList);
        for (Place p : modelList) {
            stmt.bindLong(1, p.place_id);
            stmt.bindString(2, p.name);
            stmt.bindString(3, p.image);
            stmt.bindString(4, p.address);
            stmt.bindString(5, p.phone);
            stmt.bindString(6, p.website);
            stmt.bindString(7, p.description);
            stmt.bindDouble(8, p.lng);
            stmt.bindDouble(9, p.lat);
            stmt.bindDouble(10, p.distance);
            stmt.bindDouble(11, p.last_update);
            stmt.execute();
            stmt.clearBindings();

            // Insert relational place with category
            insertListPlaceCategoryAsync(p.place_id, p.categories);
            // Insert Images places
            insertListImagesAsync(p.images);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    // Insert List place
    public void insertListNewsInfo(List<NewsInfo> modelList) {
        for (NewsInfo n : modelList) {
            ContentValues values = getNewsInfoValue(n);
            // Inserting or Update Row
            db.insertWithOnConflict(TABLE_NEWS_INFO, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    // Update one place
    public Place updatePlace(Place place) {
        List<Place> objcs = new ArrayList<>();
        objcs.add(place);
        insertListPlace(objcs);
        if(isPlaceExist(place.place_id)){
            return getPlace(place.place_id);
        }
        return null;
    }

    private ContentValues getPlaceValue(Place model){
        ContentValues values = new ContentValues();
        values.put(KEY_PLACE_ID, model.place_id);
        values.put(KEY_NAME, model.name);
        values.put(KEY_IMAGE, model.image);
        values.put(KEY_ADDRESS, model.address);
        values.put(KEY_PHONE, model.phone);
        values.put(KEY_WEBSITE, model.website);
        values.put(KEY_DESCRIPTION, model.description);
        values.put(KEY_LNG, model.lng);
        values.put(KEY_LAT, model.lat);
        values.put(KEY_DISTANCE, model.distance);
        values.put(KEY_LAST_UPDATE, model.last_update);
        return values;
    }

    private ContentValues getNewsInfoValue(NewsInfo model){
        ContentValues values = new ContentValues();
        values.put(KEY_NEWS_ID, model.id);
        values.put(KEY_NEWS_TITLE, model.title);
        values.put(KEY_NEWS_BRIEF_CONTENT, model.brief_content);
        values.put(KEY_NEWS_FULL_CONTENT, model.full_content);
        values.put(KEY_NEWS_IMAGE, model.image);
        values.put(KEY_LAST_UPDATE, model.last_update);
        return values;
    }

    // Adding new location by Category
    public List<Place> searchAllPlace(String keyword) {
        List<Place> locList = new ArrayList<>();
        Cursor cur;
        if (keyword.equals("")) {
            cur = db.rawQuery("SELECT p.* FROM "+TABLE_PLACE+" p ORDER BY " + KEY_LAST_UPDATE + " DESC", null);
        } else {
            keyword = keyword.toLowerCase();
            cur = db.rawQuery("SELECT * FROM " + TABLE_PLACE + " WHERE LOWER(" + KEY_NAME + ") LIKE ? OR LOWER("+ KEY_ADDRESS + ") LIKE ? OR LOWER("+ KEY_DESCRIPTION + ") LIKE ? ",
                  new String[]{"%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%"});
        }
        locList = getListPlaceByCursor(cur);
        return locList;
    }

    public List<Place> getAllPlace() {
        return getAllPlaceByCategory(-1);
    }

    public List<Place> getPlacesByPage(int c_id, int limit, int offset) {
        List<Place> locList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT DISTINCT p.* FROM "+TABLE_PLACE+" p ");
        if(c_id == -2) {
            sb.append(", "+TABLE_FAVORITES+" f ");
            sb.append(" WHERE p." +KEY_PLACE_ID+ " = f." +KEY_PLACE_ID+" ");
        } else if(c_id != -1){
            sb.append(", "+TABLE_PLACE_CATEGORY+" pc ");
            sb.append(" WHERE pc." +KEY_RELATION_PLACE_ID+ " = p." +KEY_PLACE_ID+ " AND pc." +KEY_RELATION_CAT_ID+ "=" +c_id+ " ");
        }
        sb.append(" ORDER BY p."+KEY_DISTANCE+" ASC, p."+KEY_LAST_UPDATE+" DESC ");
        sb.append(" LIMIT "+limit+" OFFSET "+ offset+" ");
        Cursor cursor = db.rawQuery(sb.toString(), null);
        if (cursor.moveToFirst()) {
            locList = getListPlaceByCursor(cursor);
        }
        return locList;
    }

    public List<Place> getAllPlaceByCategory(int c_id) {
        List<Place> locList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT DISTINCT p.* FROM "+TABLE_PLACE+" p ");
        if(c_id == -2) {
            sb.append(", "+TABLE_FAVORITES+" f ");
            sb.append(" WHERE p." +KEY_PLACE_ID+ " = f." +KEY_PLACE_ID+" ");
        } else if(c_id != -1){
            sb.append(", "+TABLE_PLACE_CATEGORY+" pc ");
            sb.append(" WHERE pc." +KEY_RELATION_PLACE_ID+ " = p." +KEY_PLACE_ID+ " AND pc." +KEY_RELATION_CAT_ID+ "=" +c_id+ " ");
        }
        sb.append(" ORDER BY p."+KEY_LAST_UPDATE+" DESC ");
        Cursor cursor = db.rawQuery(sb.toString(), null);
        if (cursor.moveToFirst()) {
            locList = getListPlaceByCursor(cursor);
        }
        return locList;
    }

    public Place getPlace(int place_id) {
        Place p = new Place();
        String query = "SELECT * FROM " + TABLE_PLACE + " p WHERE p." + KEY_PLACE_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{place_id+""});
        p.place_id = place_id;
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            p = getPlaceByCursor(cursor);
        }
        return p;
    }

    private List<Place> getListPlaceByCursor(Cursor cur) {
        List<Place> locList = new ArrayList<>();
        // looping through all rows and adding to list
        if (cur.moveToFirst()) {
            do {
                // Adding place to list
                locList.add(getPlaceByCursor(cur));
            } while (cur.moveToNext());
        }
        return locList;
    }

    private List<NewsInfo> getListNewsInfoByCursor(Cursor cur) {
        List<NewsInfo> list = new ArrayList<>();
        // looping through all rows and adding to list
        if (cur.moveToFirst()) {
            do {
                // Adding place to list
                list.add(getNewsInfoByCursor(cur));
            } while (cur.moveToNext());
        }
        return list;
    }

    private Place getPlaceByCursor(Cursor cur){
        Place p       = new Place();
        p.place_id    = cur.getInt(cur.getColumnIndex(KEY_PLACE_ID));
        p.name        = cur.getString(cur.getColumnIndex(KEY_NAME));
        p.image       = cur.getString(cur.getColumnIndex(KEY_IMAGE));
        p.address     = cur.getString(cur.getColumnIndex(KEY_ADDRESS));
        p.phone       = cur.getString(cur.getColumnIndex(KEY_PHONE));
        p.website     = cur.getString(cur.getColumnIndex(KEY_WEBSITE));
        p.description = cur.getString(cur.getColumnIndex(KEY_DESCRIPTION));
        p.lng         = cur.getDouble(cur.getColumnIndex(KEY_LNG));
        p.lat         = cur.getDouble(cur.getColumnIndex(KEY_LAT));
        p.distance    = cur.getFloat(cur.getColumnIndex(KEY_DISTANCE));
        p.last_update = cur.getLong(cur.getColumnIndex(KEY_LAST_UPDATE));
        return p;
    }

    private NewsInfo getNewsInfoByCursor(Cursor cur){
        NewsInfo n      = new NewsInfo();
        n.id            = cur.getInt(cur.getColumnIndex(KEY_NEWS_ID));
        n.title         = cur.getString(cur.getColumnIndex(KEY_NEWS_TITLE));
        n.brief_content = cur.getString(cur.getColumnIndex(KEY_NEWS_BRIEF_CONTENT));
        n.full_content  = cur.getString(cur.getColumnIndex(KEY_NEWS_FULL_CONTENT));
        n.image         = cur.getString(cur.getColumnIndex(KEY_NEWS_IMAGE));
        n.last_update   = cur.getLong(cur.getColumnIndex(KEY_NEWS_LAST_UPDATE));
        return n;
    }

    // Get LIst Images By Place Id
    public List<Images> getListImageByPlaceId(int place_id) {
        List<Images> imageList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_IMAGES + " WHERE " + KEY_IMG_PLACE_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{place_id + ""});
        if (cursor.moveToFirst()) {
            do {
                Images img = new Images();
                img.place_id = cursor.getInt(0);
                img.name = cursor.getString(1);
                imageList.add(img);
            } while (cursor.moveToNext());
        }
        return imageList;
    }

    public Category getCategory(int c_id){
        Category category = new Category();
        try {
            Cursor cur = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY + " WHERE " + KEY_CAT_ID + " = ?", new String[]{c_id + ""});
            cur.moveToFirst();
            category.cat_id = cur.getInt(0);
            category.name = cur.getString(1);
            category.icon = cur.getInt(2);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Db Error", e.toString());
            return null;
        }
        return category;
    }


    // get list News Info
    public List<NewsInfo> getNewsInfoByPage(int limit, int offset) {

        Log.d("DB", "Size : " + getNewsInfoSize());
        Log.d("DB", "Limit : " + limit + " Offset : " + offset);
        List<NewsInfo> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT DISTINCT n.* FROM "+TABLE_NEWS_INFO+" n ");
        sb.append(" ORDER BY n."+KEY_NEWS_ID+" DESC ");
        sb.append(" LIMIT "+limit+" OFFSET "+ offset+" ");
        Cursor cursor = db.rawQuery(sb.toString(), null);
        if (cursor.moveToFirst()) {
            list = getListNewsInfoByCursor(cursor);
        }
        return list;
    }

    // Insert new imagesList
    public void insertListImages(List<Images> images) {
        for (int i = 0; i < images.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(KEY_IMG_PLACE_ID, images.get(i).place_id);
            values.put(KEY_IMG_NAME, images.get(i).name);
            // Inserting or Update Row
            db.insertWithOnConflict(TABLE_IMAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    // Insert new imagesList with asynchronous scheme
    public void insertListImagesAsync(List<Images> images) {
        String sql = "INSERT OR REPLACE INTO "+TABLE_IMAGES+ " ";
        sql = sql + "("+KEY_IMG_PLACE_ID+", "+KEY_IMG_NAME +") VALUES (?, ?)";
        SQLiteStatement stmt = db.compileStatement(sql);
        for (Images i : images) {
            stmt.bindLong(1, i.place_id);
            stmt.bindString(2, i.name);
            stmt.execute();
            stmt.clearBindings();
        }
    }

    // Inserting new Table PLACE_CATEGORY relational
    public void insertListPlaceCategory(int place_id, List<Category> categories) {
        for (Category c : categories) {
            ContentValues values = new ContentValues();
            values.put(KEY_RELATION_PLACE_ID, place_id);
            values.put(KEY_RELATION_CAT_ID, c.cat_id);
            // Inserting or Update Row
            db.insertWithOnConflict(TABLE_PLACE_CATEGORY, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    // Inserting new Table PLACE_CATEGORY relational with asynchronous scheme
    public void insertListPlaceCategoryAsync(int place_id, List<Category> categories) {
        String sql = "INSERT OR REPLACE INTO "+TABLE_PLACE_CATEGORY+ " ";
        sql = sql + "("+KEY_RELATION_PLACE_ID+", "+KEY_RELATION_CAT_ID +") VALUES (?, ?)";
        SQLiteStatement stmt = db.compileStatement(sql);
        for (Category c : categories) {
            stmt.bindLong(1, place_id);
            stmt.bindLong(2, c.cat_id);
            stmt.execute();
            stmt.clearBindings();
        }
    }

    // Adding new Connector
    public void addFavorites(int id) {
        ContentValues values = new ContentValues();
        values.put(KEY_PLACE_ID, id);
        // Inserting Row
        db.insert(TABLE_FAVORITES, null, values);
    }

    // all Favorites
    public List<Place> getAllFavorites() {
        List<Place> locList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT p.* FROM " + TABLE_PLACE + " p, " + TABLE_FAVORITES + " f" +" WHERE p." + KEY_PLACE_ID + " = f." + KEY_PLACE_ID, null);
        locList = getListPlaceByCursor(cursor);
        return locList;
    }

    public void deleteFavorites(int id) {
        if (isFavoritesExist(id)) {
            db.delete(TABLE_FAVORITES, KEY_PLACE_ID + " = ?", new String[]{id+""});
        }
    }

    public boolean isFavoritesExist(int id) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FAVORITES + " WHERE " + KEY_PLACE_ID + " = ?", new String[]{id+""});
        int count = cursor.getCount();
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPlaceExist(int id) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PLACE + " WHERE " + KEY_PLACE_ID + " = ?", new String[]{id + ""});
        int count = cursor.getCount();
        cursor.close();
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    public int getPlacesSize() {
        int count = (int)DatabaseUtils.queryNumEntries(db, TABLE_PLACE);
        return count;
    }

    public int getNewsInfoSize() {
        int count = (int)DatabaseUtils.queryNumEntries(db, TABLE_NEWS_INFO);
        return count;
    }

    public int getPlacesSize(int c_id) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(DISTINCT p."+KEY_PLACE_ID+") FROM "+TABLE_PLACE+" p ");
        if(c_id == -2) {
            sb.append(", "+TABLE_FAVORITES+" f ");
            sb.append(" WHERE p." +KEY_PLACE_ID+ " = f." +KEY_PLACE_ID+" ");
        } else if(c_id != -1){
            sb.append(", "+TABLE_PLACE_CATEGORY+" pc ");
            sb.append(" WHERE pc." +KEY_RELATION_PLACE_ID+ " = p." +KEY_PLACE_ID+ " AND pc." +KEY_RELATION_CAT_ID+ "=" +c_id+ " ");
        }
        Cursor cursor = db.rawQuery(sb.toString(), null);
        cursor.moveToFirst();
        int size = cursor.getInt(0);
        cursor.close();
        return size;
    }

    public int getCategorySize() {
        int count = (int)DatabaseUtils.queryNumEntries(db, TABLE_CATEGORY);
        return count;
    }

    public int getFavoritesSize() {
        int count = (int)DatabaseUtils.queryNumEntries(db, TABLE_FAVORITES);
        return count;
    }

    public int getImagesSize() {
        int count = (int)DatabaseUtils.queryNumEntries(db, TABLE_IMAGES);
        return count;
    }

    public int getPlaceCategorySize() {
        int count = (int)DatabaseUtils.queryNumEntries(db, TABLE_PLACE_CATEGORY);
        return count;
    }

    // to export database file
    // for debugging only
    private void exportDatabase(){
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "/data/data/" + context.getPackageName() + "/databases/"+DATABASE_NAME;
                String backupDBPath = "backup_"+DATABASE_NAME+".db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
