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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLOutput;


public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    FrameLayout frameLayout;
    ListView listView;
    String[] opciones;
    ActionBarDrawerToggle drawerToggle;
    Utilies util = Utilies.getInstance();
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageTemsa);
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
                if (conectado(view.getContext())) {

                    if (position == 0) {
                        util.setSwMenu(1);
                        Intent i = new Intent(view.getContext(), Seleccion_Ini_Fin.class);
                        startActivity(i);
                    }

                    if (position == 1) {
                        util.setSwMenu(2);
                        Intent i = new Intent(view.getContext(), Seleccion_Ini_Fin.class);
                        startActivity(i);
                    }

                    if (position == 2) {
                        util.setSwMenu(3);
                        Intent i = new Intent(view.getContext(), Seleccion_Ini_Fin.class);
                        startActivity(i);
                    }

                    if (position == 3) {
                        Intent i = new Intent(view.getContext(), Paletizado.class);
                        startActivity(i);
                    }

                    if (position == 4) {
                        Intent i = new Intent(view.getContext(), Print.class);
                        startActivity(i);
                    }

                    //cierra el menu al pulsar una opcion
                    drawerLayout.closeDrawer(listView);
                    // otra alternativa es drawerLayout.closeDrawer();

                    //cambiar el titulo del action bar
                    //getSupportActionBar().setTitle(opciones[position]);

                } else {
                    Toast toast = Toast.makeText(MainActivity.this, "NO HAY CONEXION WIFI\nASEGURESE DE ESTAR CONECTADO A LA RED", Toast.LENGTH_LONG);
                    //centrar texto de toast
                    LinearLayout layout = (LinearLayout) toast.getView();
                    if (layout.getChildCount() > 0) {
                        TextView tv = (TextView) layout.getChildAt(0);
                        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    }
                    toast.show();
                }
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
        for (NetworkInfo rede : redes) {
            // Si alguna red tiene conexión, se devuelve true
            //System.out.println(redes[i].getTypeName());
            //System.out.println(redes[i].getSubtypeName());
            //System.out.println(redes[i].getExtraInfo());
            if (rede.getTypeName().equals("WIFI") && rede.getState() == NetworkInfo.State.CONNECTED) {
                connected = true;
            }
        }
        return connected;
    }
}
