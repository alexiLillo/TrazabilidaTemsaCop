package cl.fosforos.trazabilidadtemc;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Seleccion_Ini_Fin extends AppCompatActivity {

    Button botInicio;
    Button botFin;
    Utilies util = Utilies.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion__ini__fin);
        if (util.getSwMenu() == 1) {
            getSupportActionBar().setTitle("SELECCIONADORAS");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else if (util.getSwMenu() == 2) {
            getSupportActionBar().setTitle("MARCADORAS");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else if (util.getSwMenu() == 3) {
            getSupportActionBar().setTitle("FAJADORAS");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        botInicio = (Button) findViewById(R.id.botInicio);
        botFin = (Button) findViewById(R.id.botFin);

        botInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conectado(v.getContext()) && util.getSwMenu() == 1) {
                    Intent i = new Intent(v.getContext(), Seleccion_Inicio.class);
                    startActivity(i);
                } else if (conectado(v.getContext()) && util.getSwMenu() == 2) {
                    Intent i = new Intent(v.getContext(), Marcado_Inicio.class);
                    startActivity(i);
                } else if (conectado(v.getContext()) && util.getSwMenu() == 3) {
                    Intent i = new Intent(v.getContext(), Fajado_Inicio.class);
                    startActivity(i);
                } else {
                    Toast toast = Toast.makeText(Seleccion_Ini_Fin.this, "NO HAY CONEXION WIFI\nASEGURESE DE ESTAR CONECTADO A LA RED", Toast.LENGTH_LONG);
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

        botFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conectado(v.getContext()) && util.getSwMenu() == 1) {
                    Intent i = new Intent(v.getContext(), Seleccion_Final.class);
                    startActivity(i);
                } else if (conectado(v.getContext()) && util.getSwMenu() == 2) {
                    Intent i = new Intent(v.getContext(), Marcado_Final.class);
                    startActivity(i);
                } else if (conectado(v.getContext()) && util.getSwMenu() == 3) {
                    Intent i = new Intent(v.getContext(), Fajado_Final.class);
                    startActivity(i);
                } else {
                    Toast toast = Toast.makeText(Seleccion_Ini_Fin.this, "NO HAY CONEXION WIFI\nASEGURESE DE ESTAR CONECTADO A LA RED", Toast.LENGTH_LONG);
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
    }

    public static boolean conectado(Context context) {
        boolean connected = false;
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Recupera todas las redes (tanto móviles como wifi)
        NetworkInfo[] redes = connec.getAllNetworkInfo();
        for (NetworkInfo rede : redes) {
            // Si alguna red tiene conexión, se devuelve true
            //System.out.println(redes[i].getTypeName());
            if (rede.getTypeName().equals("WIFI") && rede.getState() == NetworkInfo.State.CONNECTED) {
                connected = true;
            }
        }
        return connected;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
