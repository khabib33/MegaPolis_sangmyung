package app.thecity.connection;

import app.thecity.connection.callbacks.CallbackDevice;
import app.thecity.connection.callbacks.CallbackListNewsInfo;
import app.thecity.connection.callbacks.CallbackListPlace;
import app.thecity.connection.callbacks.CallbackPlaceDetails;
import app.thecity.model.DeviceInfo;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface API {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "User-Agent: Place";

    /* Place API transaction ------------------------------- */

    @Headers({CACHE, AGENT})
    @GET("app/services/listPlaces")
    Call<CallbackListPlace> getPlacesByPage(
            @Query("page") int page,
            @Query("count") int count,
            @Query("draft") int draft
    );

    @Headers({CACHE, AGENT})
    @GET("app/services/getPlaceDetails")
    Call<CallbackPlaceDetails> getPlaceDetails(
            @Query("place_id") int place_id
    );

    /* News Info API transaction ------------------------------- */

    @Headers({CACHE, AGENT})
    @GET("app/services/listNewsInfo")
    Call<CallbackListNewsInfo> getNewsInfoByPage(
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @POST("app/services/insertGcm")
    Call<CallbackDevice> registerDevice(
            @Body DeviceInfo deviceInfo
    );

}
