package cl.fosforos.trazabilidadtemc;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
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

public class Marcado_Inicio extends AppCompatActivity {

    TextView txtturno;
    TextView txtturnoIni;
    TextView txtturnoFin;
    TextView txtturnoCod;
    Spinner spinerMarca;
    Spinner spinerMarcaTipo;
    TextView txtproducto;
    TextView txtcodproducto;
    TextView txtCajaTrazable;
    TextView txtCajaOrigen;
    TextView txtCajaDestino;
    TextView txtMaquina;
    TextView txtcodmaquina;
    String scanContent;
    String scanFormat;
    Button botLeerQR;
    Button botGrabar;
    String fechahora;
    private ConexionHelperSQLServer helperSQLServer;
    private int escaneos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcado__inicio);
        getSupportActionBar().setTitle("INICIO MARCADO");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        helperSQLServer = new ConexionHelperSQLServer();
        txtturno = (TextView) findViewById(R.id.txtTurno);
        txtturnoIni = (TextView) findViewById(R.id.txtTurnoInicio);
        txtturnoFin = (TextView) findViewById(R.id.txtTurnoFinal);
        txtturnoCod = (TextView) findViewById(R.id.txtTurnoCodigo);
        spinerMarca = (Spinner) findViewById(R.id.spinerMarca);
        spinerMarcaTipo = (Spinner) findViewById(R.id.spinerMarcaTipo);
        txtproducto = (TextView) findViewById(R.id.txtproducto);
        txtcodproducto = (TextView) findViewById(R.id.txtcodproducto);
        txtCajaTrazable = (TextView) findViewById(R.id.txtCajaTrazable);
        txtCajaOrigen = (TextView) findViewById(R.id.txtCajaOrigen);
        txtCajaDestino = (TextView) findViewById(R.id.txtCajaDestino);
        txtMaquina = (TextView) findViewById(R.id.txtMaquina);
        txtcodmaquina = (TextView) findViewById(R.id.txtcodmaquina);
        botLeerQR = (Button) findViewById(R.id.botLeerQR);
        botGrabar = (Button) findViewById(R.id.botGrabar);

        botLeerQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (escaneos == 0)
                    scan("ESCANEAR CAJA ORIGEN");
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
                                            txtCajaOrigen.setText("");
                                            txtCajaDestino.setText("");
                                            txtMaquina.setText("");
                                            escaneos = 0;
                                            scan("ESCANEAR CAJA ORIGEN");
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
                if (spinerMarca.getSelectedItemPosition() == 0 || spinerMarcaTipo.getSelectedItemPosition() == 0 || !(txtCajaOrigen.getText().toString()).startsWith("EN") || !(txtCajaDestino.getText().toString()).startsWith("EN") || !(txtMaquina.getText().toString()).startsWith("MM")) {
                    Toast.makeText(v.getContext(), "FALTA INFORMACION, COMPLETE TODOS LOS CAMPOS", Toast.LENGTH_SHORT).show();
                } else {
                    Connection conF = helperSQLServer.CONN();
                    String queryFecha = "SELECT GETDATE() AS FECHA";
                    try {
                        Statement stmt = conF.createStatement();
                        ResultSet rs = stmt.executeQuery(queryFecha);
                        if (rs.next()) {
                            fechahora = new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("FECHA")) + " " + new SimpleDateFormat("HH:mm").format(rs.getTime("FECHA"));
                        }
                        conF.close();
                    } catch (Exception ex) {
                        Toast.makeText(v.getContext(), "ERROR AL SELECCIONAR FECHA DEL SERVIDOR", Toast.LENGTH_SHORT).show();
                    }

                    String[] CodTraz = txtCajaTrazable.getText().toString().split("-");

                    Connection con = helperSQLServer.CONN();
                    String query = "UPDATE TRAZA_CAJA SET " +
                            " Caj_CodTurno_MAR=" + Integer.parseInt(txtturnoCod.getText().toString()) +
                            ",Caj_CodMaq_MAR=" + Integer.parseInt(txtcodmaquina.getText().toString()) +
                            ",Caj_FecHora_Ini_MAR ='" + fechahora + "'" +
                            ",Caj_CodMarca_MAR =" + Integer.parseInt(llenalistaMarcaCod().get(spinerMarca.getSelectedItemPosition())) +
                            ",Caj_CodTipMarca_MAR =" + Integer.parseInt(llenalistaMarcaTipoCod().get(spinerMarcaTipo.getSelectedItemPosition())) +
                            ",Caj_QRCode_MAR ='" + txtCajaDestino.getText().toString() + "'" +
                            " Where Caj_LetraCajTraz='" + CodTraz[0] + "' And Caj_NumCajTraz=" + CodTraz[1];
                    try {
                        Statement stmt = con.createStatement();
                        stmt.executeUpdate(query);
                        con.close();
                        Toast.makeText(v.getContext(), "INICIO DE MARCADO REGISTRADO", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (Exception ex) {
                        Toast.makeText(v.getContext(), "ERROR AL GUARDAR INICIO MARCADO! INTENTE NUEVAMENTE", Toast.LENGTH_SHORT).show();
                        System.out.println("------>" + ex.toString());
                        System.out.println("------>" + query);
                    }
                }
            }
        });

        ArrayAdapter llenaspiner1 = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, llenalistaMarcaDes());
        spinerMarca.setAdapter(llenaspiner1);

        ArrayAdapter llenaspiner2 = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, llenalistaMarcaTipoDes());
        spinerMarcaTipo.setAdapter(llenaspiner2);

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

    public ArrayList<String> llenalistaMarcaCod() {
        Connection con = helperSQLServer.CONN();
        ArrayList<String> lista = new ArrayList<>();
        String query = "select Marca_Codigo from MARCAS order by Marca_Descrip";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            lista.add("Seleccione Marca");
            while (rs.next()) {
                lista.add(rs.getString("Marca_Codigo"));
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
        return lista;
    }

    public ArrayList<String> llenalistaMarcaDes() {
        Connection con = helperSQLServer.CONN();
        ArrayList<String> lista = new ArrayList<>();
        String query = "select Marca_Descrip from MARCAS order by Marca_Descrip";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            lista.add("Seleccione Marca");
            while (rs.next()) {
                lista.add(rs.getString("Marca_Descrip"));
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
        return lista;
    }

    public ArrayList<String> llenalistaMarcaTipoCod() {
        Connection con = helperSQLServer.CONN();
        ArrayList<String> lista = new ArrayList<>();
        String query = "select TM_Codigo from TIPOS_MARCAS Where TM_Codigo>0 order by TM_Codigo";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            lista.add("Seleccione Tipo Marca");
            while (rs.next()) {
                lista.add(rs.getString("TM_Codigo"));
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
        return lista;
    }

    public ArrayList<String> llenalistaMarcaTipoDes() {
        Connection con = helperSQLServer.CONN();
        ArrayList<String> lista = new ArrayList<>();
        String query = "select TM_Descrip from TIPOS_MARCAS Where TM_Codigo>0 order by TM_Codigo";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            lista.add("Seleccione Tipo Marca");
            while (rs.next()) {
                lista.add(rs.getString("TM_Descrip"));
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

                if (escaneos == 2) {
                    if (scanContent.startsWith("MM")) {
                        txtMaquina.setText(scanContent);
                        Connection con = helperSQLServer.CONN();
                        String query = "select * from  MAQ_MARCADO Where MaqMar_QRCode='" + scanContent + "'";
                        try {
                            Statement stmt = con.createStatement();
                            ResultSet rs = stmt.executeQuery(query);
                            while (rs.next()) {
                                ok();
                                escaneos = 3;
                                txtcodmaquina.setText(rs.getString("MaqMar_Codigo"));
                            }
                            con.close();
                        } catch (Exception ex) {
                            escaneos = 2;
                            txtcodmaquina.setText("");
                            txtMaquina.setText("");
                            error();
                            Toast.makeText(this, "ERROR AL LEER CODIGO DE MAQUINA", Toast.LENGTH_SHORT).show();
                            scan("ESCANEAR CODIGO MAQUINA");
                            System.out.println("------>" + ex.toString());
                        }
                    } else {
                        error();
                        Toast.makeText(this, "CODIGO DE MAQUINA INVALIDO", Toast.LENGTH_SHORT).show();
                        scan("ESCANEAR CODIGO MAQUINA");
                    }
                }

                if (escaneos == 1) {
                    if (scanContent.startsWith("EN") && !txtCajaOrigen.getText().equals("") && !scanContent.equals(txtCajaOrigen.getText())) {
                        txtCajaDestino.setText(scanContent);
                        escaneos = 2;
                        ok();
                        scan("ESCANEAR CODIGO MAQUINA");
                    } else {
                        error();
                        Toast.makeText(this, "CODIGO CAJA INVALIDO", Toast.LENGTH_SHORT).show();
                        scan("ESCANEAR CAJA DESTINO");
                    }
                }

                if (escaneos == 0) {
                    if (scanContent.startsWith("EN") && txtCajaOrigen.getText().equals("")) {
                        txtCajaOrigen.setText(scanContent);
                        Connection con = helperSQLServer.CONN();

                        /*Validacion Alexis
                        String query = "SELECT Caj_LetraCajTraz,Caj_NumCajTraz,PB_Descrip FROM TRAZA_CAJA " +
                                "INNER JOIN PRODUCTO_BASE ON TRAZA_CAJA.Caj_CodProdBase=PRODUCTO_BASE.PB_Codigo " +
                                "Where Caj_QRCode_ST='" + scanContent.toString() + "' And left(Caj_QRCode_Mar,2)<>'EN' And left(Caj_QRCode_Faj,2)<>'EN' Order by Caj_FecHora_Ter_ST Desc";*/

                        String query = "SELECT TOP (1) * FROM TRAZA_CAJA " +
                                "INNER JOIN PRODUCTO_BASE ON TRAZA_CAJA.Caj_CodProdBase=PRODUCTO_BASE.PB_Codigo " +
                                "WHERE (Caj_IDPal = 0) AND (Caj_QRCode_ST = N'" + scanContent + "')  AND (Caj_QRCode_Mar = N'')  AND (Caj_QRCode_FAJ = N'') " +
                                "ORDER BY Caj_LetraCajTraz DESC, Caj_NumCajTraz DESC";

                        try {
                            Statement stmt = con.createStatement();
                            ResultSet rs = stmt.executeQuery(query);
                            if (rs.next()) {
                                txtproducto.setText(rs.getString("PB_Descrip"));
                                txtCajaTrazable.setText(rs.getString("Caj_LetraCajTraz") + "-" + rs.getString("Caj_NumCajTraz"));
                                escaneos = 1;
                                ok();
                                scan("ESCANEAR CAJA DESTINO");
                            } else {
                                escaneos = 0;
                                txtCajaOrigen.setText("");
                                txtproducto.setText("");
                                txtCajaTrazable.setText("");
                                error();
                                Toast.makeText(this, "CODIGO CAJA INVALIDO", Toast.LENGTH_SHORT).show();
                                scan("ESCANEAR CAJA ORIGEN");
                            }
                            con.close();
                        } catch (Exception ex) {
                            escaneos = 0;
                            txtCajaOrigen.setText("");
                            txtproducto.setText("");
                            txtCajaTrazable.setText("");
                            error();
                            Toast.makeText(this, "CODIGO CAJA INVALIDO", Toast.LENGTH_SHORT).show();
                            scan("ESCANEAR CAJA ORIGEN");
                            System.out.println(ex.toString());
                        }
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
