package com.example.josemedinaruiz97.mygeo;

import android.location.Location;

import java.util.Date;
import java.util.GregorianCalendar;

public class Localizacion {

    private Location localizacion;
    private Date fecha;

    public Localizacion() {
        this(new Location("provider"));
    }

    public Localizacion(Location localizacion) {
        this(localizacion, new GregorianCalendar().getTime());
    }

    public Localizacion(Location localizacion, Date fecha) {
        this.localizacion = localizacion;
        this.fecha = fecha;
    }

    public Location getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(Location localizacion) {
        this.localizacion = localizacion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "Localizacion{" +
                "localizacion=" + localizacion.toString() +
                ", fecha=" + fecha.toString() +
                '}';
    }
}