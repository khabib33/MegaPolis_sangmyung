package app.thecity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import app.thecity.adapter.AdapterNewsInfo;
import app.thecity.connection.API;
import app.thecity.connection.RestAdapter;
import app.thecity.connection.callbacks.CallbackListNewsInfo;
import app.thecity.data.Constant;
import app.thecity.data.DatabaseHandler;
import app.thecity.model.NewsInfo;
import app.thecity.utils.Tools;
import app.thecity.widget.SpacingItemDecoration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityNewsInfo extends AppCompatActivity {

    public ActionBar actionBar;
    private View parent_view;
    private RecyclerView recyclerView;
    private AdapterNewsInfo mAdapter;
    private View lyt_progress;
    private Call<CallbackListNewsInfo> callbackCall = null;
    private DatabaseHandler db;

    private int post_total = 0;
    private int failed_page = 0;
    private Snackbar snackbar_retry = null;

    // can be, ONLINE or OFFLINE
    private String MODE = "ONLINE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_info);
        parent_view = findViewById(android.R.id.content);
        db = new DatabaseHandler(this);

        initToolbar();
        iniComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.title_nav_news);
        Tools.systemBarLolipop(this);
    }

    public void iniComponent() {
        lyt_progress = findViewById(R.id.lyt_progress);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpacingItemDecoration(1, Tools.dpToPx(this, 4), true));


        //set data and list adapter
        mAdapter = new AdapterNewsInfo(this, recyclerView, new ArrayList<NewsInfo>());
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterNewsInfo.OnItemClickListener() {
            @Override
            public void onItemClick(View v, NewsInfo obj, int position) {
                ActivityNewsInfoDetails.navigate(ActivityNewsInfo.this, obj, false);
            }
        });

        // detect when scroll reach bottom
        mAdapter.setOnLoadMoreListener(new AdapterNewsInfo.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (post_total > mAdapter.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    mAdapter.setLoaded();
                }
            }
        });

        // if already have data news at db, use mode OFFLINE
        if (db.getNewsInfoSize() > 0) {
            MODE = "OFFLINE";
        }
        requestAction(1);
    }

    private void displayApiResult(final List<NewsInfo> items) {
        mAdapter.insertData(items);
        firstProgress(false);
        if (items.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListNewsInfo(final int page_no) {
        if (MODE.equals("ONLINE")) {
            API api = RestAdapter.createAPI();
            callbackCall = api.getNewsInfoByPage(page_no, Constant.LIMIT_NEWS_REQUEST);
            callbackCall.enqueue(new Callback<CallbackListNewsInfo>() {
                @Override
                public void onResponse(Call<CallbackListNewsInfo> call, Response<CallbackListNewsInfo> response) {
                    CallbackListNewsInfo resp = response.body();
                    if (resp != null && resp.status.equals("success")) {
                        if (page_no == 1) {
                            mAdapter.resetListData();
                            db.refreshTableNewsInfo();
                        }
                        post_total = resp.count_total;
                        db.insertListNewsInfo(resp.news_infos);
                        displayApiResult(resp.news_infos);
                    } else {
                        onFailRequest(page_no);
                    }
                }

                @Override
                public void onFailure(Call<CallbackListNewsInfo> call, Throwable t) {
                    if (!call.isCanceled()) onFailRequest(page_no);
                }

            });
        } else {
            if (page_no == 1) mAdapter.resetListData();
            int limit = Constant.LIMIT_NEWS_REQUEST;
            int offset = (page_no * limit) - limit;
            post_total = db.getNewsInfoSize();
            List<NewsInfo> items = db.getNewsInfoByPage(limit, offset);
            displayApiResult(items);
        }
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        mAdapter.setLoaded();
        firstProgress(false);
        if (Tools.cekConnection(this)) {
            showFailedView(true, getString(R.string.refresh_failed));
        } else {
            showFailedView(true, getString(R.string.no_internet));
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            firstProgress(true);
        } else {
            mAdapter.setLoading();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestListNewsInfo(page_no);
            }
        }, MODE.equals("OFFLINE") ? 50 : 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        firstProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
    }

    private void showFailedView(boolean show, String message) {
        if(snackbar_retry == null) {
            snackbar_retry = Snackbar.make(parent_view, "", Snackbar.LENGTH_INDEFINITE);
        }
        snackbar_retry.setText(message);
        snackbar_retry.setAction(R.string.RETRY, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAction(failed_page);
            }
        });
        if (show) {
            snackbar_retry.show();
        } else {
            snackbar_retry.dismiss();
        }
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = (View) findViewById(R.id.lyt_no_item);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void firstProgress(final boolean show) {
        if (show) {
            lyt_progress.setVisibility(View.VISIBLE);
        } else {
            lyt_progress.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activiy_news_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
        } else if (id == R.id.action_refresh) {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            showFailedView(false, "");
            MODE = "ONLINE";
            post_total = 0;
            requestAction(1);
        } else if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), ActivitySetting.class);
            startActivity(i);
        } else if (id == R.id.action_rate) {
            Tools.rateAction(ActivityNewsInfo.this);
        } else if (id == R.id.action_about) {
            Tools.aboutAction(ActivityNewsInfo.this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        if (actionBar != null) {
            Tools.setActionBarColor(this, actionBar);
            // for system bar in lollipop
            Tools.systemBarLolipop(this);
        }
        super.onResume();
    }
}
