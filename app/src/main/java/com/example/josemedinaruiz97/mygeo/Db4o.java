package com.example.josemedinaruiz97.mygeo;

import android.location.Location;
import android.util.Log;


import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.AndroidSupport;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Predicate;
import com.db4o.query.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by josemedinaruiz97 on 1/3/18.
 */

public class Db4o {

    private static final String TAG = "adios";

    private static ObjectContainer objectContainer;

    public static void alta(Location loc) {
        Localizacion l=new Localizacion(loc);
        Log.v(TAG,l.toString());
        objectContainer.store(l);
        objectContainer.commit();
    }
    public static void closeDataBase() {
        objectContainer.close();
        objectContainer=null;
    }
    public static boolean comprobarConexion(){
        if(objectContainer!=null){
            return true;
        }
        return false;
    }

    public static  ArrayList<Localizacion> consulta(final Date d) {

        ArrayList<Localizacion>l=new ArrayList<>();
        Query consulta = objectContainer.query();
        consulta.constrain(Localizacion.class);

        ObjectSet<Localizacion> locs = objectContainer.query(
                new Predicate<Localizacion>() {

                    @Override
                    public boolean match(Localizacion loc) {
                        Log.v(TAG, "2: " + loc.getFecha().getYear()+" "+loc.getFecha().getMonth()+" "+loc.getFecha().getDay());
                        if(loc.getFecha().getYear()==d.getYear() && loc.getFecha().getMonth()==d.getMonth() && loc.getFecha().getDay()==d.getDay()){
                            return true;
                        }
                        return false;
                    }
                });
        for(Localizacion localizacion:locs){
            l.add(localizacion);
        }
        return l;
    }
    public static ArrayList<Localizacion> consultaTodo(){
        boolean encontrado=false;
        ArrayList<Localizacion> localizacions=new ArrayList<>();
        Query consulta = objectContainer.query();
        consulta.constrain(Localizacion.class);
        ObjectSet<Localizacion> localizaciones = consulta.execute();
        for(Localizacion localizacion: localizaciones){
            encontrado=false;
            if (localizacions.size() == 0) {
                localizacions.add(localizacion);
            }
            else{
                for(Localizacion loc:localizacions) {
                    if (loc.getFecha().getYear() == localizacion.getFecha().getYear() && loc.getFecha().getMonth() == localizacion.getFecha().getMonth() && loc.getFecha().getDay() == localizacion.getFecha().getDay()) {
                        encontrado=true;
                    }
                }
                if(!encontrado){
                    localizacions.add(localizacion);
                }
            }
        }
        return localizacions;
    }

    public static EmbeddedConfiguration getDb4oConfig() throws IOException {
        EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
        configuration.common().add(new AndroidSupport());
        configuration.common().objectClass(Localizacion.class).
                objectField("fecha").indexed(true);
        return configuration;
    }


    public static void openDataBase(String ruta) {
        try {
            String name = ruta + "/ejemplo.db4o";
            objectContainer = Db4oEmbedded.openFile(getDb4oConfig(), name);
        } catch (IOException e) {
            Log.v(TAG, e.toString());
        }
    }



}
