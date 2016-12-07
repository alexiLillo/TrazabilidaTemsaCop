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
import android.widget.SimpleCursorAdapter;
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

public class Fajado_Inicio extends AppCompatActivity {

    TextView txtturno;
    TextView txtturnoIni;
    TextView txtturnoFin;
    TextView txtturnoCod;
    TextView txtMarca;
    TextView txtMarcaTipo;
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
    public int escaneos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fajado__inicio);
        getSupportActionBar().setTitle("INICIO FAJADO");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        helperSQLServer = new ConexionHelperSQLServer();
        txtturno = (TextView) findViewById(R.id.txtTurno);
        txtturnoIni = (TextView) findViewById(R.id.txtTurnoInicio);
        txtturnoFin = (TextView) findViewById(R.id.txtTurnoFinal);
        txtturnoCod = (TextView) findViewById(R.id.txtTurnoCodigo);
        txtMarca = (TextView) findViewById(R.id.txtMarca);
        txtMarcaTipo = (TextView) findViewById(R.id.txtMarcaTipo);
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
                if (escaneos == 0) {
                    scan("ESCANEAR CAJA ORIGEN");
                } else {
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

        botGrabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(txtCajaOrigen.getText().toString()).startsWith("EN") || !(txtCajaDestino.getText().toString()).startsWith("EN") || !(txtMaquina.getText().toString()).startsWith("MF")) {
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
                            " Caj_CodTurno_FAJ=" + Integer.parseInt(txtturnoCod.getText().toString()) +
                            ",Caj_CodMaq_FAJ=" + Integer.parseInt(txtcodmaquina.getText().toString()) +
                            ",Caj_FecHora_Ini_FAJ ='" + fechahora + "'" +
                            ",Caj_QRCode_FAJ ='" + txtCajaDestino.getText().toString() + "'" +
                            " Where Caj_LetraCajTraz='" + CodTraz[0] + "' And Caj_NumCajTraz=" + CodTraz[1];
                    try {
                        Statement stmt = con.createStatement();
                        stmt.executeUpdate(query);
                        con.close();
                        Toast.makeText(v.getContext(), "INICIO DE FAJADO REGISTRADO", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (Exception ex) {
                        Toast.makeText(v.getContext(), "ERROR AL GUARDAR INICIO FAJADO! INTENTE NUEVAMENTE", Toast.LENGTH_SHORT).show();
                        System.out.println("------>" + ex.toString());
                        System.out.println("------>" + query);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null) {
            if (scanningResult.getContents() != null) {
                scanContent = scanningResult.getContents();
                scanFormat = scanningResult.getFormatName();

                if (escaneos == 2) {
                    if (scanContent.startsWith("MF") && !txtCajaOrigen.getText().toString().equals("") && !txtCajaDestino.getText().toString().equals("") && !scanContent.equals(txtCajaOrigen.getText()) && !scanContent.equals(txtCajaDestino.getText())) {
                        txtMaquina.setText(scanContent);
                        escaneos = 3;
                        Connection con = helperSQLServer.CONN();
                        String query = "select * from  MAQ_FAJADO Where MaqFAJ_QRCode='" + scanContent + "'";
                        try {
                            Statement stmt = con.createStatement();
                            ResultSet rs = stmt.executeQuery(query);
                            while (rs.next()) {
                                ok();
                                txtcodmaquina.setText(rs.getString("MaqFAJ_Codigo"));
                            }
                            con.close();
                        } catch (Exception ex) {
                            System.out.println("------>" + ex.toString());
                            escaneos = 2;
                            txtMaquina.setText("");
                            txtcodmaquina.setText("");
                            error();
                            Toast.makeText(this, "CODIGO DE MAQUINA INVALIDO", Toast.LENGTH_SHORT).show();
                            scan("ESCANEAR CODIGO MAQUINA");
                        }
                    } else {
                        error();
                        Toast.makeText(this, "CODIGO DE MAQUINA INVALIDO", Toast.LENGTH_SHORT).show();
                        scan("ESCANEAR CODIGO MAQUINA");
                    }
                }

                if (escaneos == 1) {
                    if (scanContent.startsWith("EN") && !txtCajaOrigen.getText().toString().equals("") && !scanContent.equals(txtCajaOrigen.getText())) {
                        txtCajaDestino.setText(scanContent);
                        escaneos = 2;
                        ok();
                        scan("ESCANEAR CODIGO MAQUINA");
                    } else {
                        error();
                        Toast.makeText(this, "CODIGO DE CAJA INVALIDO", Toast.LENGTH_SHORT).show();
                        scan("ESCANEAR CAJA DESTINO");
                    }
                }

                if (escaneos == 0) {
                    if (scanContent.startsWith("EN") && txtCajaOrigen.getText().toString().equals("")) {
                        //procedencia caja
                        if (procedenciaCaja(scanContent)) {
                            txtCajaOrigen.setText(scanContent);
                            escaneos = 1;
                            ok();
                            scan("ESCANEAR CAJA DESTINO");
                        } else {
                            error();
                            Toast.makeText(this, "CODIGO DE CAJA INVALIDO", Toast.LENGTH_SHORT).show();
                            scan("ESCANEAR CAJA DE ORIGEN");
                        }
                    }
                }
            }
        } else {
            error();
            Toast.makeText(this, "NO SE ESCANEO NINGUN CODIGO", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean procedenciaCaja(String qrCaja) {
        Connection con = helperSQLServer.CONN();

        /*Validacion Alexis
        String query = "SELECT Caj_LetraCajTraz, Caj_NumCajTraz, PB_Descrip, TM_Descrip, Marca_Descrip FROM TRAZA_CAJA " +
                "INNER JOIN PRODUCTO_BASE ON TRAZA_CAJA.Caj_CodProdBase=PRODUCTO_BASE.PB_Codigo " +
                "INNER JOIN MARCAS ON TRAZA_CAJA.Caj_CodMarca_MAR=MARCAS.Marca_Codigo " +
                "INNER JOIN TIPOS_MARCAS ON TRAZA_CAJA.Caj_CodTipMarca_MAR=TIPOS_MARCAS.TM_Codigo " +
                "Where Caj_QRCode_Mar='" + qrCaja + "' And left(Caj_QRCode_Faj,2)<>'EN' Order by Caj_FecHora_Ter_ST Desc";*/

        String query = "SELECT        TOP (1) * " +
                "FROM            dbo.TRAZA_CAJA " +
                "INNER JOIN PRODUCTO_BASE ON TRAZA_CAJA.Caj_CodProdBase=PRODUCTO_BASE.PB_Codigo " +
                "INNER JOIN MARCAS ON TRAZA_CAJA.Caj_CodMarca_MAR=MARCAS.Marca_Codigo " +
                "INNER JOIN TIPOS_MARCAS ON TRAZA_CAJA.Caj_CodTipMarca_MAR=TIPOS_MARCAS.TM_Codigo " +
                "WHERE        (Caj_IDPal = 0) AND (Caj_QRCode_Mar = N'" + qrCaja + "') AND  (Caj_QRCode_FAJ = N'') " +
                "ORDER BY Caj_LetraCajTraz DESC, Caj_NumCajTraz DESC";

        boolean marcadora;
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                txtproducto.setText(rs.getString("PB_Descrip"));
                txtCajaTrazable.setText(rs.getString("Caj_LetraCajTraz") + "-" + rs.getString("Caj_NumCajTraz"));
                txtMarca.setText(rs.getString("Marca_Descrip"));
                txtMarcaTipo.setText(rs.getString("TM_Descrip"));
                marcadora = true;
            } else {
                marcadora = false;
                txtMarca.setText("");
                txtMarcaTipo.setText("");
                txtCajaOrigen.setText("");
                txtproducto.setText("");
                txtCajaTrazable.setText("");
            }
            con.close();
        } catch (Exception ex) {
            marcadora = false;
            System.out.println(ex.toString());
        }

        if (!marcadora) {
            Connection con2 = helperSQLServer.CONN();

            /*Valicadion Alexis
            String query2 = "SELECT Caj_LetraCajTraz, Caj_NumCajTraz, PB_Descrip, TM_Descrip, Marca_Descrip FROM TRAZA_CAJA " +
                    "INNER JOIN PRODUCTO_BASE ON TRAZA_CAJA.Caj_CodProdBase=PRODUCTO_BASE.PB_Codigo " +
                    "INNER JOIN MARCAS ON TRAZA_CAJA.Caj_CodMarca_MAR=MARCAS.Marca_Codigo " +
                    "INNER JOIN TIPOS_MARCAS ON TRAZA_CAJA.Caj_CodTipMarca_MAR=TIPOS_MARCAS.TM_Codigo " +
                    //"Where Caj_QRCode_Mar='" + qrCaja + "' And left(Caj_QRCode_Faj,2)<>'EN' Order by Caj_FecHora_Ter_ST Desc";
                    "Where Caj_QRCode_ST='" + qrCaja + "' And left(Caj_QRCode_Faj,2)<>'EN' Order by Caj_FecHora_Ter_ST Desc";]*/

            String query2 = "SELECT        TOP (1) *" +
                    "FROM            dbo.TRAZA_CAJA " +
                    "INNER JOIN PRODUCTO_BASE ON TRAZA_CAJA.Caj_CodProdBase=PRODUCTO_BASE.PB_Codigo " +
                    "INNER JOIN MARCAS ON TRAZA_CAJA.Caj_CodMarca_MAR=MARCAS.Marca_Codigo " +
                    "INNER JOIN TIPOS_MARCAS ON TRAZA_CAJA.Caj_CodTipMarca_MAR=TIPOS_MARCAS.TM_Codigo " +
                    "WHERE        (Caj_IDPal = 0) AND (Caj_QRCode_ST = N'" + qrCaja + "') AND (Caj_QRCode_Mar = N'') AND (Caj_QRCode_FAJ = N'') " +
                    "ORDER BY Caj_LetraCajTraz DESC, Caj_NumCajTraz DESC";

            try {
                Statement stmt = con2.createStatement();
                ResultSet rs = stmt.executeQuery(query2);
                if (rs.next()) {
                    txtproducto.setText(rs.getString("PB_Descrip"));
                    txtCajaTrazable.setText(rs.getString("Caj_LetraCajTraz") + "-" + rs.getString("Caj_NumCajTraz"));
                    txtMarca.setText(rs.getString("Marca_Descrip"));
                    txtMarcaTipo.setText(rs.getString("TM_Descrip"));
                    con.close();
                    return true;
                } else {
                    con.close();
                    txtMarca.setText("");
                    txtMarcaTipo.setText("");
                    txtCajaOrigen.setText("");
                    txtproducto.setText("");
                    txtCajaTrazable.setText("");
                    return false;
                }
            } catch (Exception ex) {
                System.out.println(ex.toString());
                return false;
            }
        } else {
            return true;
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
