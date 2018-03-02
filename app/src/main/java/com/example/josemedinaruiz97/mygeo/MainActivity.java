package com.example.josemedinaruiz97.mygeo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Spinner spnFecha;
    private ArrayList<Localizacion> localizaciones;
    private void ejecucionDeHebra(){
        if(!Db4o.comprobarConexion()){
            Db4o.openDataBase(getExternalFilesDir(null)+"");
        }

        Thread t=new Thread(){
            @Override
            public void run() {
                super.run();
                localizaciones = Db4o.consultaTodo();
            }
        };
        t.start();
        try{
            t.join();
        }catch (Exception e){

        }
    }
    private void init(){
        ejecucionDeHebra();
        spnFecha=findViewById(R.id.sfecha);
        if(localizaciones.size()==0){
            //Db4o.closeDataBase();
            Intent i=new Intent(MainActivity.this,MapsActivity.class);
            startActivity(i);
        }
        ArrayList<String> dates=new ArrayList<>();
        for(Localizacion lo: localizaciones){
            dates.add(lo.getFecha()+"");
        }
        spnFecha.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, dates));
        Button btnFecha =findViewById(R.id.btnFecha);
        btnFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,MapsActivity.class);
                i.putExtra("date",spnFecha.getSelectedItem()+"");
                startActivity(i);
            }
        });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();


    }
}
