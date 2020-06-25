package com.stirling.stfloor.Models.POJOs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Dispositivo {

    @SerializedName("idMac")
    @Expose
    private String idMac;
    @SerializedName("nombreHab")
    @Expose
    private String nombreHab;
    @SerializedName("tConsigna")
    @Expose
    private int tConsigna;
    @SerializedName("TOU_THRESH")
    @Expose
    private int strTOU;
    @SerializedName("REL_THRESH")
    @Expose
    private int strREL;
    @SerializedName("PROX_THRESH")
    @Expose
    private int strPROX;
    @SerializedName("PREL_THRESH")
    @Expose
    private int strPREL;

    public Dispositivo(){

    }

    public String toString(){
        return "Dispositivo{" +
                "idMac='"+idMac+"'"+
                ", nombreHab='" + nombreHab + "'" +
                ", tConsigna='" + tConsigna + "'"+
                ", TOUTHRESH='" + strTOU +"'"+
                ", REL_THRESH='" + strREL +"'"+
                ", PROX_THRESH='" + strPROX +"'"+
                ", PREL_THRESH='" + strPREL +"'"+
                "}";
    }

    public String getIdMac() {
        return idMac;
    }

    public void setIdMac(String idMac) {
        this.idMac = idMac;
    }

    public String getNombreHab() {
        return nombreHab;
    }

    public void setNombreHab(String nombreHab) {
        this.nombreHab = nombreHab;
    }

    public int gettConsigna() {
        return tConsigna;
    }

    public void setTConsigna(int tConsigna) {
        this.tConsigna = tConsigna;
    }

    public int getStrTOU() {
        return strTOU;
    }

    public void setStrTOU(int strTOU) {
        this.strTOU = strTOU;
    }

    public int getStrREL() {
        return strREL;
    }

    public void setStrREL(int strREL) {
        this.strREL = strREL;
    }

    public int getStrPROX() {
        return strPROX;
    }

    public void setStrPROX(int strPROX) {
        this.strPROX = strPROX;
    }

    public int getStrPREL() {
        return strPREL;
    }

    public void setStrPREL(int strPREL) {
        this.strPREL = strPREL;
    }
}
