package com.devalutix.ringtoneapp.presenters;

import com.devalutix.ringtoneapp.contracts.RingtonesContract;
import com.devalutix.ringtoneapp.pojo.Ringtone;
import com.devalutix.ringtoneapp.ui.activities.RingtonesActivity;
import com.devalutix.ringtoneapp.utils.ApiEndpointInterface;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;

public class RingtonesPresenter implements RingtonesContract.Presenter {

    /***************************************** Declarations ***************************************/
    private RingtonesActivity mView;
    private String mode;
    private ApiEndpointInterface apiService;
    private int position;

    /***************************************** Constructor ****************************************/
    public RingtonesPresenter(ApiEndpointInterface apiService) {
        this.apiService = apiService;
    }

    /***************************************** Essential Methods **********************************/
    @Override
    public void attach(RingtonesContract.View view) {
        mView = (RingtonesActivity) view;
    }

    @Override
    public void dettach() {
        mView = null;
    }

    @Override
    public boolean isAttached() {
        return !(mView == null);
    }

    /***************************************** Methods ********************************************/
    @Override
    public void initRecyclerView(String name) {

        Call<ArrayList<Ringtone>> call = null;
        ArrayList<Ringtone> ringtones = getRingtones();

        //        switch (mode) {
//            case "category": {
//                call = apiService.getCategoryRingtones(Config.TOKEN, name);
//            }
//            break;
//            case "ringtones": {
//                call = apiService.getRingtones(Config.TOKEN, name);
//            }
//            break;
//            case "search": {
//                call = apiService.searchRingtones(Config.TOKEN, name);
//            }
//            break;
//        }
//
//        call.enqueue(new Callback<ArrayList<Ringtone>>() {
//            @Override
//            public void onResponse(@NonNull Call<ArrayList<Ringtone>> call,
//                                   @NonNull Response<ArrayList<Ringtone>> response) {
//                if (response.isSuccessful()) {
//                    mView.initRecyclerView(response.body());
//                    mView.showRingtones();
//
//                    assert response.body() != null;
//                    if (response.body().size() > 0) mView.showRingtones();
//                    else {
//                        if (mode.equals("search"))
//                            mView.showEmptyResults(mView.getResources().getString(R.string.empty_search));
//                        else mView.showEmptyResults(mView.getResources().getString(R.string.empty_results));
//                    }
//                } else {
//                    mView.initRecyclerView(null);
//                    mView.showNoRingtone();
//                    (mView).showRetryCard(mView.getResources().getString(R.string.server_prblm_retry));
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ArrayList<Ringtone>> call, @NonNull Throwable t) {
//                mView.initRecyclerView(null);
//                mView.showNoRingtone();
//                mView.showRetryCard(mView.getResources().getString(R.string.net_prblm_retry));
//            }
//        });

        mView.initRecyclerView(ringtones);
    }

    @Override
    public void updateRecyclerView(String name) {

        Call<ArrayList<Ringtone>> call = null;
        ArrayList<Ringtone> ringtones = getRingtones();

        //        switch (mode) {
//            case "category": {
//                call = apiService.getCategoryRingtones(Config.TOKEN, name);
//            }
//            break;
//            case "ringtones": {
//                call = apiService.getRingtones(Config.TOKEN, name);
//            }
//            break;
//            case "search": {
//                call = apiService.searchRingtones(Config.TOKEN, name);
//            }
//            break;
//        }
//
//        call.enqueue(new Callback<ArrayList<Ringtone>>() {
//            @Override
//            public void onResponse(@NonNull Call<ArrayList<Ringtone>> call,
//                                   @NonNull Response<ArrayList<Ringtone>> response) {
//                if (response.isSuccessful()) {
//                    mView.updateRecyclerView(response.body());
//                    mView.showRingtones();
//
//                    assert response.body() != null;
//                    if (response.body().size() > 0) mView.showRingtones();
//                    else {
//                        if (mode.equals("search"))
//                            mView.showEmptyResults(mView.getResources().getString(R.string.empty_search));
//                        else mView.showEmptyResults(mView.getResources().getString(R.string.empty_results));
//                    }
//                } else {
//                    mView.updateRecyclerView(null);
//                    mView.showNoRingtone();
//                    (mView).showRetryCard(mView.getResources().getString(R.string.server_prblm_retry));
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ArrayList<Ringtone>> call, @NonNull Throwable t) {
//                mView.updateRecyclerView(null);
//                mView.showNoRingtone();
//                mView.showRetryCard(mView.getResources().getString(R.string.net_prblm_retry));
//            }
//        });

        mView.updateRecyclerView(ringtones);
    }

    @Override
    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public void setCurrent(int position) {
        this.position = position;
    }

    @Override
    public String getMode() {
        return mode;
    }

    @Override
    public int getPosition() {
        return position;
    }

    private ArrayList<Ringtone> getRingtones() {
        String json = null;
        try {
            InputStream is = mView.getAssets().open("ringtones.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        Ringtone[] collectionItem = new Gson().fromJson(json, Ringtone[].class);
        return new ArrayList<Ringtone>(Arrays.asList(collectionItem));
    }
}
