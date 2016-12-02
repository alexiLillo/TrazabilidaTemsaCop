package cl.fosforos.trazabilidadtemc;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import BaseDatos.ConexionHelperSQLServer;

public class Paletizado extends AppCompatActivity {

    TextView txtturno;
    TextView txtturnoIni;
    TextView txtturnoFin;
    TextView txtturnoCod;
    Spinner spinerPTerminado;
    TextView txtOperador;
    TextView txtCCalidad;
    String scanContent;
    String scanFormat;
    Button botLeerQR;
    Button botGrabar;
    String fechahora;
    private ConexionHelperSQLServer helperSQLServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paletizado);
        getSupportActionBar().setTitle("PALETIZADO");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        helperSQLServer = new ConexionHelperSQLServer();
        txtturno = (TextView) findViewById(R.id.txtTurno);
        txtturnoIni = (TextView) findViewById(R.id.txtTurnoInicio);
        txtturnoFin = (TextView) findViewById(R.id.txtTurnoFinal);
        txtturnoCod = (TextView) findViewById(R.id.txtTurnoCodigo);
        spinerPTerminado = (Spinner) findViewById(R.id.spinerPTerminado);
        txtOperador = (TextView) findViewById(R.id.txtOperador);
        txtCCalidad = (TextView) findViewById(R.id.txtCCalidad);
        botLeerQR = (Button) findViewById(R.id.botLeerQR);
        botGrabar = (Button) findViewById(R.id.botGrabar);

        botLeerQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan("");
            }
        });

        ArrayAdapter llenaspiner = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, llenalistaPTerminadoDes());
        spinerPTerminado.setAdapter(llenaspiner);

        Connection con = helperSQLServer.CONN();
        String query = "select * from  PLAN_TURNOS order by pt_codigo desc";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                txtturno.setText(rs.getString("PT_Turno"));
                txtturnoIni.setText(new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("PT_FecIni")) + " " + new SimpleDateFormat("HH:mm").format(rs.getTime("PT_FecIni")));
                txtturnoFin.setText(new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("PT_FecTer")) + " " + new SimpleDateFormat("HH:mm").format(rs.getTime("PT_FecTer")));
                txtturnoCod.setText(rs.getString("PT_Codigo"));
                break;
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
    }

    public ArrayList<String> llenalistaPTerminadoDes() {
        Connection con = helperSQLServer.CONN();
        ArrayList<String> lista = new ArrayList<>();
        String query = "Select ProL_Descrip from PRODTERM_LOCAL order by ProL_Descrip";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            lista.add("Seleccione Producto Terminado");
            while (rs.next()) {
                lista.add(rs.getString("ProL_Descrip"));
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
        return lista;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null) {
            if (scanningResult.getContents() != null) {
                scanContent = scanningResult.getContents().toString();
                scanFormat = scanningResult.getFormatName().toString();

                //escanear cajas en pallets e imprimir etiquetas
            }
        } else {
            Toast.makeText(this, "NO SE ESCANEO NINGUN CODIGO", Toast.LENGTH_SHORT).show();
        }
    }

    public void scan(String titulo) {
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.setPrompt(titulo);
        scanIntegrator.setBeepEnabled(true);
        scanIntegrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.setBarcodeImageEnabled(true);
        scanIntegrator.initiateScan();
    }
}
