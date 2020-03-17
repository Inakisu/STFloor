package com.stirling.stfloor.ui.dashboard;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class DashboardViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public DashboardViewModel() {

    }

    public LiveData<String> getText() {
        return mText;
    }
}