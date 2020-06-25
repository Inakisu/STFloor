package com.stirling.stfloor.Models.POJOs;

public class Dispositivo {

    private String idMac;
    private String nombreHab;
    private int tConsigna;
    private int strTOU, strREL, strPROX, strPREL;

    public Dispositivo(){

    }

    public String toString(){
        return "Dispositivo{" +
                "idMac='"+idMac+"'"+
                ", nombreHab='" + nombreHab + "'" +
                ", tConsigna='" + tConsigna + "'"+
                ", TOU_THRESH='" + strTOU +"'"+
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
