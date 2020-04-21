package com.stirling.stfloor.Models.POJOs;

public class Dispositivo {

    private String idMac;
    private String nombreHab;
    private int tConsigna;
    private int sensibilidad;

    public Dispositivo(){

    }

    public String toString(){
        return "Dispositivo{" +
                "idMac='"+idMac+'\''+
                ", nombreHab='" + nombreHab + '\'' +
                ", tConsigna='" + tConsigna + '\''+
                ", sensibilidad='" + sensibilidad + '\''+
                '}';
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

    public int getSensibilidad() {
        return sensibilidad;
    }

    public void setSensibilidad(int sensibilidad) {
        this.sensibilidad = sensibilidad;
    }
}
