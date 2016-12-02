package cl.fosforos.trazabilidadtemc;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import BaseDatos.ConexionHelperSQLServer;

public class Marcado_Final extends AppCompatActivity {

    TextView txtturno;
    TextView txtturnoIni;
    TextView txtturnoFin;
    TextView txtturnoCod;
    TextView txtproducto;
    TextView txtMarca;
    TextView txtMarcaTipo;
    TextView txtCajaTrazable;
    TextView txtCajaOrigen;
    TextView txtcodmaquina;
    TextView txtMaquina;
    TextView txthorainicio;
    TextView txtCajaDestino;
    TextView txtoperador;
    String scanContent;
    String scanFormat;
    Button botLeerQR;
    Button botGrabar;
    String fechahora;
    Date fechahora1;
    Date fechahora2;
    Long diferencia;
    Integer operFicha;
    String operNombre;
    private ConexionHelperSQLServer helperSQLServer;
    public int escaneos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcado__final);
        getSupportActionBar().setTitle("TERMINO MARCADO");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        helperSQLServer = new ConexionHelperSQLServer();
        txtturno = (TextView) findViewById(R.id.txtTurno);
        txtturnoIni = (TextView) findViewById(R.id.txtTurnoInicio);
        txtturnoFin = (TextView) findViewById(R.id.txtTurnoFinal);
        txtturnoCod = (TextView) findViewById(R.id.txtTurnoCodigo);
        txtproducto = (TextView) findViewById(R.id.txtproducto);
        txtMarca = (TextView) findViewById(R.id.txtMarca);
        txtMarcaTipo = (TextView) findViewById(R.id.txtMarcaTipo);
        txtCajaTrazable = (TextView) findViewById(R.id.txtCajaTrazable);
        txtCajaOrigen = (TextView) findViewById(R.id.txtCajaOrigen);
        txtcodmaquina = (TextView) findViewById(R.id.txtcodmaquina);
        txtMaquina = (TextView) findViewById(R.id.txtMaquina);
        txthorainicio = (TextView) findViewById(R.id.txthorainicio);
        txtCajaDestino = (TextView) findViewById(R.id.txtCajaDestino);
        txtoperador = (TextView) findViewById(R.id.txtoperador);
        botLeerQR = (Button) findViewById(R.id.botLeerQR);
        botGrabar = (Button) findViewById(R.id.botGrabar);

        botLeerQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (escaneos == 0)
                    scan("ESCANEAR CAJA DESTINO");
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
                                            txtCajaDestino.setText("");
                                            txtoperador.setText("");
                                            escaneos = 0;
                                            scan("ESCANEAR CAJA DESTINO");
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
                if (!(txtoperador.getText().toString().substring(0, txtoperador.getText().length() - 1).endsWith("-")) || !(txtCajaDestino.getText().toString()).startsWith("EN")) {
                    Toast.makeText(v.getContext(), "Falta Información", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(v.getContext(), "Error con Fecha del Servidor", Toast.LENGTH_SHORT).show();
                    }

                    Connection conOpe = helperSQLServer.CONN_Oper();
                    String queryOperario = "SELECT * FROM MAESTRO Where Rut=" + Integer.parseInt(txtoperador.getText().toString().substring(0, txtoperador.length() - 2));
                    operFicha = 0;
                    operNombre = "";
                    try {
                        Statement stmt = conOpe.createStatement();
                        ResultSet rs = stmt.executeQuery(queryOperario);
                        if (rs.next()) {
                            operFicha = rs.getInt("Ficha");
                            operNombre = rs.getString("Nombre");
                        }
                        conOpe.close();
                    } catch (Exception ex) {
                        Toast.makeText(v.getContext(), "Error con Buscar Operario", Toast.LENGTH_SHORT).show();
                        System.out.println("------>" + ex.toString());
                        System.out.println("------>" + queryOperario);
                    }

                    fechahora1 = deStringToDate(txthorainicio.getText().toString());
                    fechahora2 = deStringToDate(fechahora);
                    diferencia = (fechahora2.getTime() - fechahora1.getTime()) / (1000 * 60);
                    String[] CodTraz = txtCajaTrazable.getText().toString().split("-");

                    Connection con = helperSQLServer.CONN();
                    String query = "UPDATE TRAZA_CAJA SET " +
                            " Caj_FecHora_Ter_MAR ='" + fechahora + "'" +
                            ",Caj_TiempoTotMinu_MAR =" + diferencia +
                            ",Caj_FichaOpe_MAR =" + operFicha +
                            ",Caj_NombOpe_MAR ='" + operNombre + "'" +
                            " Where Caj_LetraCajTraz='" + CodTraz[0] + "' And Caj_NumCajTraz=" + CodTraz[1];
                    try {
                        Statement stmt = con.createStatement();
                        stmt.executeUpdate(query);
                        con.close();
                        Toast.makeText(v.getContext(), "Grabado", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (Exception ex) {
                        Toast.makeText(v.getContext(), "Error al Grabar", Toast.LENGTH_SHORT).show();
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
                scanContent = scanningResult.getContents().toString();
                scanFormat = scanningResult.getFormatName().toString();

                if (escaneos == 1) {
                    if (scanContent.substring(0, scanContent.length() - 1).endsWith("-")) {
                        txtoperador.setText(scanContent);
                        escaneos = 2;
                    } else {
                        Toast.makeText(this, "CODIGO DE OPERADOR INVALIDO", Toast.LENGTH_SHORT).show();
                        scan("ESCANEAR CODIGO OPERADOR");
                    }
                }

                if (escaneos == 0) {
                    if (scanContent.startsWith("EN") && txtCajaDestino.getText().equals("")) {
                        txtCajaDestino.setText(scanContent);
                        Connection con = helperSQLServer.CONN();
                        /*Validacion Alexis
                        String query = "SELECT Caj_LetraCajTraz, Caj_NumCajTraz, PB_Descrip, TM_Descrip, Marca_Descrip, Caj_QRCode_ST, Caj_QRCode_MAR, Caj_FecHora_Ini_MAR, Caj_CodMaq_MAR, MaqMar_Descrip FROM TRAZA_CAJA " +
                                "INNER JOIN PRODUCTO_BASE ON TRAZA_CAJA.Caj_CodProdBase=PRODUCTO_BASE.PB_Codigo " +
                                "INNER JOIN MARCAS ON TRAZA_CAJA.Caj_CodMarca_MAR=MARCAS.Marca_Codigo " +
                                "INNER JOIN TIPOS_MARCAS ON TRAZA_CAJA.Caj_CodTipMarca_MAR=TIPOS_MARCAS.TM_Codigo " +
                                "INNER JOIN MAQ_MARCADO ON TRAZA_CAJA.Caj_CodMaq_MAR=MAQ_MARCADO.MaqMar_Codigo " +
                                "Where Caj_QRCode_MAR='" + scanContent.toString() + "'";*/

                        String query = "SELECT        TOP (1) * " +
                                "FROM            dbo.TRAZA_CAJA " +
                                "INNER JOIN PRODUCTO_BASE ON TRAZA_CAJA.Caj_CodProdBase=PRODUCTO_BASE.PB_Codigo " +
                                "INNER JOIN MARCAS ON TRAZA_CAJA.Caj_CodMarca_MAR=MARCAS.Marca_Codigo " +
                                "INNER JOIN TIPOS_MARCAS ON TRAZA_CAJA.Caj_CodTipMarca_MAR=TIPOS_MARCAS.TM_Codigo " +
                                "INNER JOIN MAQ_MARCADO ON TRAZA_CAJA.Caj_CodMaq_MAR=MAQ_MARCADO.MaqMar_Codigo " +
                                "WHERE        (Caj_IDPal = 0) AND  (Caj_QRCode_Mar = N'" + scanContent + "') AND (Caj_QRCode_FAJ = N'') " +
                                "ORDER BY Caj_LetraCajTraz DESC, Caj_NumCajTraz DESC";

                        try {
                            Statement stmt = con.createStatement();
                            ResultSet rs = stmt.executeQuery(query);
                            if (rs.next()) {
                                txtproducto.setText(rs.getString("PB_Descrip"));
                                txtMarca.setText(rs.getString("Marca_Descrip"));
                                txtMarcaTipo.setText(rs.getString("TM_Descrip"));
                                txtCajaTrazable.setText(rs.getString("Caj_LetraCajTraz") + "-" + rs.getString("Caj_NumCajTraz"));
                                txtCajaOrigen.setText(rs.getString("Caj_QRCode_ST"));
                                txtMaquina.setText(rs.getString("MaqMar_Descrip"));
                                txtcodmaquina.setText(rs.getString("Caj_CodMaq_MAR"));
                                txthorainicio.setText(new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("Caj_FecHora_Ini_MAR")) + " " + new SimpleDateFormat("HH:mm").format(rs.getTime("Caj_FecHora_Ini_MAR")));
                                escaneos = 1;
                                scan("ESCANEAR CODIGO OPERADOR");
                            } else {
                                escaneos = 0;
                                txtCajaDestino.setText("");
                                txtproducto.setText("");
                                txtMarca.setText("");
                                txtMarcaTipo.setText("");
                                txtCajaTrazable.setText("");
                                txtCajaOrigen.setText("");
                                txtMaquina.setText("");
                                txtcodmaquina.setText("");
                                txthorainicio.setText("");
                                Toast.makeText(this, "CODIGO DE CAJA INVALIDO", Toast.LENGTH_SHORT).show();
                                scan("ESCANEAR CAJA DESTINO");
                            }
                            con.close();
                        } catch (Exception ex) {
                            escaneos = 0;
                            txtCajaDestino.setText("");
                            txtproducto.setText("");
                            txtMarca.setText("");
                            txtMarcaTipo.setText("");
                            txtCajaTrazable.setText("");
                            txtCajaOrigen.setText("");
                            txtMaquina.setText("");
                            txtcodmaquina.setText("");
                            txthorainicio.setText("");
                            Toast.makeText(this, "CODIGO DE CAJA INVALIDO", Toast.LENGTH_SHORT).show();
                            scan("ESCANEAR CAJA DESTINO");
                            System.out.println("------------>" + ex.toString());
                        }
                    } else {
                        Toast.makeText(this, "CODIGO DE CAJA INVALIDO", Toast.LENGTH_SHORT).show();
                        scan("ESCANEAR CAJA DESTINO");
                    }
                }
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

    //Devuele un java.util.Date desde un String en formato dd/MM/yyyy HH:mm
    //@param La fecha a convertir a formato date
    //@return Retorna la fecha en formato Date
    public static synchronized java.util.Date deStringToDate(String fecha) {
        SimpleDateFormat formatoDelTexto = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date fechaEnviar = null;
        try {
            fechaEnviar = formatoDelTexto.parse(fecha);
            return fechaEnviar;
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
