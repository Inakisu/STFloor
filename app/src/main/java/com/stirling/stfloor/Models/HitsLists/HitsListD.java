package com.stirling.stfloor.Models.HitsLists;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stirling.stfloor.Models.Sources.DispositivoSource;

import java.util.List;

@IgnoreExtraProperties
public class HitsListD {

    @SerializedName("hits")
    @Expose
    private List<DispositivoSource> dispositivoIndex;

    public List<DispositivoSource> getDispositivoIndex(){ return dispositivoIndex;}

    public void setDispositivoIndex (List<DispositivoSource> dispositivoIndex) {
        this.dispositivoIndex = dispositivoIndex;
    }
}
