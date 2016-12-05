
package cl.fosforos.trazabilidadtemc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import net.sourceforge.jtds.jdbc.DateTime;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import BaseDatos.ConexionHelperSQLServer;

import static android.R.attr.format24Hour;
import static android.R.attr.viewportHeight;
import static android.R.attr.x;

public class Seleccion_Inicio extends AppCompatActivity {

    TextView txtcaja;
    TextView txtturno;
    TextView txtturnoIni;
    TextView txtturnoFin;
    TextView txtturnoCod;
    TextView txtmaquina;
    Spinner spinerLinea;
    Spinner spinerProducto;
    String scanContent;
    String scanFormat;
    Button botLeerQR;
    Button botGrabar;
    String fechahora;
    private ConexionHelperSQLServer helperSQLServer;
    public int escaneos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion__inicio);
        getSupportActionBar().setTitle("INICIO SELECCIONADORAS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        helperSQLServer = new ConexionHelperSQLServer();
        txtturno = (TextView) findViewById(R.id.txtTurno);
        txtturnoIni = (TextView) findViewById(R.id.txtTurnoInicio);
        txtturnoFin = (TextView) findViewById(R.id.txtTurnoFinal);
        txtturnoCod = (TextView) findViewById(R.id.txtTurnoCodigo);
        txtmaquina = (TextView) findViewById(R.id.txtmaquina);
        txtcaja = (TextView) findViewById(R.id.txtcaja);
        spinerLinea = (Spinner) findViewById(R.id.spinerlinea);
        spinerProducto = (Spinner) findViewById(R.id.spinerproducto);
        botLeerQR = (Button) findViewById(R.id.botLeerQR);
        botGrabar = (Button) findViewById(R.id.botGrabar);

        botLeerQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (escaneos == 0)
                    scan("ESCANEAR CODIGO DE MAQUINA");
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("¿Desea re-escanear los códigos QR?")
                            .setTitle("Atención!")
                            .setCancelable(false)
                            .setNegativeButton("Cancelar",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                        }
                                    })
                            .setPositiveButton("Re-escanear",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            txtcaja.setText("");
                                            txtmaquina.setText("");
                                            escaneos = 0;
                                            scan("ESCANEAR CODIGO DE MAQUINA");
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        botGrabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinerLinea.getSelectedItemPosition() == 0 || spinerProducto.getSelectedItemPosition() == 0 || !(txtcaja.getText().toString()).startsWith("EN") || !(txtmaquina.getText().toString()).startsWith("MS")) {
                    Toast.makeText(v.getContext(), "FALTA INFORMACION, COMPLETE TODOS LOS CAMPOS", Toast.LENGTH_SHORT).show();
                } else {
                    Connection conF = helperSQLServer.CONN();
                    String queryFecha = "SELECT GETDATE() AS FECHA";
                    try {
                        Statement stmt = conF.createStatement();
                        ResultSet rs = stmt.executeQuery(queryFecha);
                        if (rs.next()) {
                            fechahora = new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("FECHA")) + " " + new SimpleDateFormat("HH:mm").format(rs.getTime("FECHA"));
                            //Toast.makeText(v.getContext(), "Fecha "+fechahora, Toast.LENGTH_SHORT).show();
                        }
                        conF.close();
                    } catch (Exception ex) {
                        Toast.makeText(v.getContext(), "ERROR AL SELECCIONAR FECHA DEL SERVIDOR", Toast.LENGTH_SHORT).show();
                    }

                    Connection con = helperSQLServer.CONN();
                    String query = "insert into TMP_SELECCIONADORA values ('" + txtcaja.getText().toString() + "','" + txtmaquina.getText().toString() +
                            "','" + fechahora + "'" +
                            "," + Integer.parseInt(txtturnoCod.getText().toString()) +
                            "," + Integer.parseInt(llenalistalineacod().get(spinerLinea.getSelectedItemPosition())) +
                            "," + Integer.parseInt(llenalistaproduccod().get(spinerProducto.getSelectedItemPosition())) + ")";
                    try {
                        Statement stmt = con.createStatement();
                        stmt.executeUpdate(query);
                        con.close();
                        Toast.makeText(v.getContext(), "INICIO DE SELECCIONADO REGISTRADO", Toast.LENGTH_SHORT).show();
                        //Seleccion_Inicio.super.onBackPressed();
                        finish();
                        //txtcaja.setText("");
                        //txtmaquina.setText("");
                        //spinerLinea.setSelection(0);
                        //spinerProducto.setSelection(0);
                    } catch (Exception ex) {
                        Toast.makeText(v.getContext(), "ERROR AL GUARDAR INICIO SELECCIONADO! INTENTE NUEVAMENTE", Toast.LENGTH_SHORT).show();
                        //System.out.println(ex.toString());
                        //error
                    }
                }
            }
        });


        ArrayAdapter llenaspiner1 = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, llenalistalineades());
        spinerLinea.setAdapter(llenaspiner1);

        ArrayAdapter llenaspiner2 = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, llenalistaproducdes());
        spinerProducto.setAdapter(llenaspiner2);

        Connection con = helperSQLServer.CONN();
        String query = "select * from  PLAN_TURNOS order by pt_codigo desc";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                //String[] horai = rs.getString("PT_FecIni").split(" ");
                //String[] horaf = rs.getString("PT_FecTer").split(" ");
                txtturno.setText(rs.getString("PT_Turno"));
                //txtturnoIni.setText(new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("PT_FecIni")) + " " + horai[1].substring(0, 5));
                //txtturnoFin.setText(new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("PT_FecTer")) + " " + horaf[1].substring(0, 5));
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

    public ArrayList<String> llenalistalineacod() {
        Connection con = helperSQLServer.CONN();
        ArrayList<String> lista = new ArrayList<>();
        String query = "select lin_codigo from tipos_lineas order by lin_codigo";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            lista.add("Seleccione Linea");
            while (rs.next()) {
                lista.add(rs.getString("lin_codigo"));
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
        return lista;
    }

    public ArrayList<String> llenalistalineades() {
        Connection con = helperSQLServer.CONN();
        ArrayList<String> lista = new ArrayList<>();
        String query = "select lin_descrip from tipos_lineas order by lin_codigo";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            lista.add("Seleccione Linea");
            while (rs.next()) {
                lista.add(rs.getString("lin_descrip"));
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
        return lista;
    }

    public ArrayList<String> llenalistaproduccod() {
        Connection con = helperSQLServer.CONN();
        ArrayList<String> lista = new ArrayList<>();
        String query = "select PB_Codigo from producto_base order by PB_Codigo";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            lista.add("Seleccione Producto Base");
            while (rs.next()) {
                lista.add(rs.getString("PB_Codigo"));
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
        return lista;
    }

    public ArrayList<String> llenalistaproducdes() {
        Connection con = helperSQLServer.CONN();
        ArrayList<String> lista = new ArrayList<>();
        String query = "select PB_descrip from producto_base order by PB_Codigo";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            lista.add("Seleccione Producto Base");
            while (rs.next()) {
                lista.add(rs.getString("PB_descrip"));
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

                if (escaneos == 1) {
                    if (scanContent.startsWith("EN") && !txtmaquina.getText().equals("")) {
                        txtcaja.setText(scanContent);
                        escaneos = 2;
                        //comprobar si la caja ya esta en proceso
                        Connection con = helperSQLServer.CONN();
                        String query = "SELECT Tmp_Fecha,MaqST_Codigo,MaqST_Descrip,Tmp_Cod_Producto,PB_Descrip,Tmp_Cod_Linea,Lin_Descrip FROM TMP_SELECCIONADORA " +
                                "INNER JOIN TIPOS_LINEAS ON TMP_SELECCIONADORA.Tmp_Cod_Linea=TIPOS_LINEAS.Lin_Codigo " +
                                "INNER JOIN PRODUCTO_BASE ON TMP_SELECCIONADORA.Tmp_Cod_Producto=PRODUCTO_BASE.PB_Codigo " +
                                "INNER JOIN MAQ_STI_STO ON TMP_SELECCIONADORA.Tmp_COD_Maquina=MAQ_STI_STO.MaqST_QRCode " +
                                "Where Tmp_Cod_Caja='" + scanContent.toString() + "'";
                        try {
                            Statement stmt = con.createStatement();
                            ResultSet rs = stmt.executeQuery(query);
                            if (rs.next()) {
                                error();
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setMessage("Caja escaneada ya se encuentra en proceso \nMáquina " + rs.getString("MaqST_Descrip") + "\nFecha " + new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("Tmp_Fecha")) + " " + new SimpleDateFormat("HH:mm").format(rs.getTime("Tmp_Fecha")))
                                        .setTitle("Atención!")
                                        .setCancelable(false)
                                        .setNegativeButton("Reasignar",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        Connection conD = helperSQLServer.CONN();
                                                        String queryD = "Delete TMP_SELECCIONADORA Where Tmp_Cod_Caja='" + scanContent.toString() + "'";
                                                        try {
                                                            Statement stmtD = conD.createStatement();
                                                            stmtD.executeUpdate(queryD);
                                                            conD.close();
                                                        } catch (Exception ex) {
                                                            System.out.println("-------->" + ex.toString());
                                                        }
                                                    }
                                                })
                                        .setPositiveButton("Escanear otra caja",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        txtcaja.setText("");
                                                        escaneos = 1;
                                                        scan("ESCANEAR CODIGO DE CAJA");
                                                    }
                                                });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }else {
                                ok();
                            }
                            con.close();
                        } catch (Exception ex) {
                            System.out.println("-------->" + ex.toString());
                        }
                    } else {
                        error();
                        Toast.makeText(this, "CODIGO NO CORRESPONDE A CAJA", Toast.LENGTH_SHORT).show();
                        scan("ESCANEAR CODIGO DE CAJA");
                    }
                }

                if (escaneos == 0) {
                    if (scanContent.startsWith("MS") && txtmaquina.getText().equals("")) {
                        txtmaquina.setText(scanContent);
                        escaneos = 1;
                        ok();
                        scan("ESCANEAR CODIGO DE CAJA");
                    } else {
                        error();
                        Toast.makeText(this, "CODIGO NO CORRESPONDE A MAQUINA", Toast.LENGTH_SHORT).show();
                        scan("ESCANEAR CODIGO DE MAQUINA");
                    }
                }
            }
        } else {
            error();
            Toast.makeText(this, "NO SE ESCANEO NINGUN CODIGO", Toast.LENGTH_SHORT).show();
        }
    }

    public void scan(String titulo) {
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.setPrompt(titulo);
        scanIntegrator.setBeepEnabled(false);
        scanIntegrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.setBarcodeImageEnabled(true);
        scanIntegrator.initiateScan();
    }

    private void ok() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.ok);
        mp.setVolume(50, 50);
        mp.start();
    }

    private void error() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.error);
        mp.setVolume(50, 50);
        mp.start();
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