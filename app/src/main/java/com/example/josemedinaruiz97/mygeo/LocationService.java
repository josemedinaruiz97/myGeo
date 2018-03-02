package com.example.josemedinaruiz97.mygeo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LocationService extends Service {

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent i=new Intent(this, LocationService.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Notification.Builder constructorNotificacion = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("notificación servicio")
                .setContentText("texto servicio")
                .setContentIntent(PendingIntent.getActivity(this, 0, i, 0));
        startForeground(1, constructorNotificacion.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //do the job ...
        return START_STICKY;
    }

}