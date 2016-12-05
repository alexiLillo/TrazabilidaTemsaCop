package cl.fosforos.trazabilidadtemc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.SQLOutput;


public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    FrameLayout frameLayout;
    ListView listView;
    String[] opciones;
    ActionBarDrawerToggle drawerToggle;
    Utilies util = Utilies.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        opciones = new String[]{"SELECCIONADORAS", "MARCADORAS", "FAJADORAS", "PALETIZADO", "IMPRIMIR ETIQUETAS"};
        drawerLayout = (DrawerLayout) findViewById(R.id.contenedorPrincipal);
        frameLayout = (FrameLayout) findViewById(R.id.contenedorFragmento);
        listView = (ListView) findViewById(R.id.menu);

        //creando un adaptador al listview con opciones

        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, opciones));

        //evento de los items del listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                //Toast.makeText(view.getContext(),"pos:"+position,Toast.LENGTH_SHORT).show();

                //Fragment ff= new Fragment();

                if (position == 0) {
                    //AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    //builder.setMessage("Seleccione Proceso")
                    //        .setTitle("")
                    //        .setCancelable(false)
                    //        .setNegativeButton("Inicio",
                    //                new DialogInterface.OnClickListener() {
                    //                    public void onClick(DialogInterface dialog, int id) {
                    //                    }
                    //                })
                    //        .setPositiveButton("Termino",
                    //                new DialogInterface.OnClickListener() {
                    //                    public void onClick(DialogInterface dialog, int id) {
                    //                        if(conectado(view.getContext())) {
                    //                            Intent i = new Intent(view.getContext(), Seleccion_Final.class);
                    //                            startActivity(i);
                    //                        }else{
                    //                            Toast.makeText(view.getContext(),"Sin Conexión WiFi",Toast.LENGTH_SHORT).show();
                    //                        }
                    //                   }
                    //                });
                    //AlertDialog alert = builder.create();
                    //alert.show();
                    util.setSwMenu(1);
                    if (conectado(view.getContext())) {
                        Intent i = new Intent(view.getContext(), Seleccion_Ini_Fin.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(view.getContext(), "Sin Conexión WiFi", Toast.LENGTH_SHORT).show();
                    }
                }
                if (position == 1) {
                    util.setSwMenu(2);
                    if (conectado(view.getContext())) {
                        Intent i = new Intent(view.getContext(), Seleccion_Ini_Fin.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(view.getContext(), "Sin Conexión WiFi", Toast.LENGTH_SHORT).show();
                    }
                }
                if (position == 2) {
                    util.setSwMenu(3);
                    if (conectado(view.getContext())) {
                        Intent i = new Intent(view.getContext(), Seleccion_Ini_Fin.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(view.getContext(), "Sin Conexión WiFi", Toast.LENGTH_SHORT).show();
                    }
                }
                if (position == 3) {
                    if (conectado(view.getContext())) {
                        Intent i = new Intent(view.getContext(), Paletizado.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(view.getContext(), "Sin Conexión WiFi", Toast.LENGTH_SHORT).show();
                    }
                }

                if (position == 4) {
                    if (conectado(view.getContext())) {
                        Intent i = new Intent(view.getContext(), Print.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(view.getContext(), "Sin Conexión WiFi", Toast.LENGTH_SHORT).show();
                    }
                }

                //FragmentManager FM = getSupportFragmentManager();
                //FragmentTransaction FT = FM.beginTransaction();
                //FT.replace(R.id.contenedorFragmento,ff);
                //FT.commit();

                //getSupportFragmentManager().beginTransaction().replace(R.id.contenedorFragmento,ff).commit();


                //cierra el menu al pulsar una opcion
                drawerLayout.closeDrawer(listView);
                // otra alternativa es drawerLayout.closeDrawer();

                //cambiar el titulo del action bar
                //getSupportActionBar().setTitle(opciones[position]);

            }
        });


        //CONFIGURAR EL DRAWER TOGGLE
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, android.R.drawable.ic_media_play, R.string.abrir, R.string.cerrar);
        drawerLayout.setDrawerListener(drawerToggle);


        //una flecha volver
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //para cambiar icono
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_action_reorder);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static boolean conectado(Context context) {
        boolean connected = false;
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Recupera todas las redes (tanto móviles como wifi)
        NetworkInfo[] redes = connec.getAllNetworkInfo();
        for (int i = 0; i < redes.length; i++) {
            // Si alguna red tiene conexión, se devuelve true
            //System.out.println(redes[i].getTypeName());
            //System.out.println(redes[i].getSubtypeName());
            //System.out.println(redes[i].getExtraInfo());
            if (redes[i].getTypeName().equals("WIFI") && redes[i].getState() == NetworkInfo.State.CONNECTED) {
                connected = true;
            }
        }
        return connected;
    }
}
