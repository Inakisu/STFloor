package com.stirling.stfloor.Models.HitsObjects;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stirling.stfloor.Models.HitsLists.HitsListD;

@IgnoreExtraProperties
public class HitsObjectD {

    @SerializedName("hits")
    @Expose
    private HitsListD hits;

    public HitsListD getHits(){ return hits;}

    public void setHits(HitsListD hits){ this.hits = hits;}
}
