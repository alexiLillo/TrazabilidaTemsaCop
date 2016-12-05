package cl.fosforos.trazabilidadtemc;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import net.sourceforge.jtds.jdbc.DateTime;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import BaseDatos.ConexionHelperSQLServer;

public class Seleccion_Final extends AppCompatActivity {

    String scanContent;
    String scanFormat;
    TextView txtDiaProceso;
    TextView txtturno;
    TextView txtturnoIni;
    TextView txtturnoFin;
    TextView txtturnoCod;
    TextView txtlinea;
    TextView txtproducto;
    TextView txtmaquina;
    TextView txtcaja;
    TextView txtoperador;
    TextView txthorainicio;
    TextView txtcodlinea;
    TextView txtcodproducto;
    TextView txtcodmaquina;
    TextView txtarqueado;
    TextView txtcurvo;
    TextView txtcolor;
    TextView txtlargo;
    TextView txtancho;
    TextView txtangosto;
    Button botLeerQR;
    Button botGrabar;
    private ConexionHelperSQLServer helperSQLServer;
    String fechahora;
    Date fechahora1;
    Date fechahora2;
    Long diferencia;
    Integer ultDoc;
    Integer operFicha;
    String operNombre;
    public int escaneos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion__final);
        getSupportActionBar().setTitle("TERMINO SELECCIONADORAS");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        helperSQLServer = new ConexionHelperSQLServer();
        txtturno = (TextView) findViewById(R.id.txtTurno);
        txtDiaProceso = (TextView) findViewById(R.id.txtDiaProceso);
        txtturnoIni = (TextView) findViewById(R.id.txtTurnoInicio);
        txtturnoFin = (TextView) findViewById(R.id.txtTurnoFinal);
        txtturnoCod = (TextView) findViewById(R.id.txtTurnoCodigo);
        txtlinea = (TextView) findViewById(R.id.txtlinea);
        txtproducto = (TextView) findViewById(R.id.txtproducto);
        txtmaquina = (TextView) findViewById(R.id.txtmaquina);
        txtcaja = (TextView) findViewById(R.id.txtcaja);
        txtoperador = (TextView) findViewById(R.id.txtoperador);
        txthorainicio = (TextView) findViewById(R.id.txthorainicio);
        txtcodlinea = (TextView) findViewById(R.id.txtcodLinea);
        txtcodproducto = (TextView) findViewById(R.id.txtcodproducto);
        txtcodmaquina = (TextView) findViewById(R.id.txtcodmaquina);
        txtarqueado = (TextView) findViewById(R.id.txtarqueado);
        txtcurvo = (TextView) findViewById(R.id.txtcurvo);
        txtcolor = (TextView) findViewById(R.id.txtcolor);
        txtlargo = (TextView) findViewById(R.id.txtlargo);
        txtancho = (TextView) findViewById(R.id.txtancho);
        txtangosto = (TextView) findViewById(R.id.txtangosto);
        botLeerQR = (Button) findViewById(R.id.botLeerQR);
        botGrabar = (Button) findViewById(R.id.botGrabar);

        botLeerQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (escaneos == 0)
                    scan("ESCANEAR CODIGO DE CAJA");
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
                                            txtoperador.setText("");
                                            escaneos = 0;
                                            scan("ESCANEAR CODIGO DE CAJA");
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
                if (!(txtoperador.getText().toString().substring(0, txtoperador.getText().length() - 1).endsWith("-")) || !(txtcaja.getText().toString()).startsWith("EN")) {
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

                    Connection conU = helperSQLServer.CONN();
                    String queryUltimo = "SELECT top 1 Caj_NumCajTraz FROM TRAZA_CAJA Where Caj_LetraCajTraz='" + txtDiaProceso.getText().toString().substring(8, 10) + txtDiaProceso.getText().toString().substring(3, 5) + "' Order BY Caj_NumCajTraz Desc";
                    ultDoc = 1;
                    try {
                        Statement stmt = conU.createStatement();
                        ResultSet rs = stmt.executeQuery(queryUltimo);
                        if (rs.next()) {
                            ultDoc = rs.getInt("Caj_NumCajTraz") + 1;
                        }
                        conU.close();
                    } catch (Exception ex) {
                        Toast.makeText(v.getContext(), "Error con Buscar ultimo Código", Toast.LENGTH_SHORT).show();
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

                    Connection con = helperSQLServer.CONN();
                    String query = "Insert Into TRAZA_CAJA Values (2" +
                            ",'" + txtDiaProceso.getText().toString().substring(8, 10) + txtDiaProceso.getText().toString().substring(3, 5) + "'" +
                            "," + ultDoc +
                            "," + Integer.parseInt(txtcodproducto.getText().toString()) +
                            "," + Integer.parseInt(txtcodmaquina.getText().toString()) +
                            "," + Integer.parseInt(txtturnoCod.getText().toString()) +
                            "," + Integer.parseInt(txtcodlinea.getText().toString()) +
                            ",'" + txthorainicio.getText().toString() + "'" +
                            ",'" + fechahora + "'" +
                            "," + diferencia +
                            "," + operFicha +
                            ",'" + operNombre + "'" +
                            ",'" + txtcaja.getText().toString() + "'" +
                            ",0,0,NULL,NULL,0,0,'',0,0,'',0,0,NULL,NULL,0,0,'','',0,0" +
                            "," + Integer.parseInt("0" + txtarqueado.getText().toString()) +
                            "," + Integer.parseInt("0" + txtcurvo.getText().toString()) +
                            "," + Integer.parseInt("0" + txtcolor.getText().toString()) +
                            "," + Integer.parseInt("0" + txtlargo.getText().toString()) +
                            "," + Integer.parseInt("0" + txtancho.getText().toString()) +
                            "," + Integer.parseInt("0" + txtangosto.getText().toString()) +
                            ",'','','','','')";
                    try {
                        Statement stmt = con.createStatement();
                        stmt.executeUpdate(query);
                        //Borra temporal
                        String queryborra = "Delete From TMP_SELECCIONADORA Where Tmp_Cod_Caja='" + txtcaja.getText().toString() + "'";
                        try {
                            stmt.executeUpdate(queryborra);
                        } catch (Exception ex) {
                            Toast.makeText(v.getContext(), "Error al Borrar", Toast.LENGTH_SHORT).show();
                            System.out.println("------>" + ex.toString());
                            System.out.println("------>" + query);
                        }
                        //
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
                txtDiaProceso.setText(new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("PT_DiaProceso")));
                break;
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
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
                        ok();
                    } else {
                        error();
                        Toast.makeText(this, "CODIGO NO CORRESPONDE A OPERADOR", Toast.LENGTH_SHORT).show();
                        scan("ESCANEAR CODIGO OPERADOR");
                    }
                }

                if (escaneos == 0) {
                    if (scanContent.startsWith("EN") && txtcaja.getText().equals("")) {
                        txtcaja.setText(scanContent);
                        //Busca datos de caja en temporal
                        Connection con = helperSQLServer.CONN();

                        //validacion Alexis
                        String query = "SELECT Tmp_Fecha,MaqST_Codigo,MaqST_Descrip,Tmp_Cod_Producto,PB_Descrip,Tmp_Cod_Linea,Lin_Descrip FROM TMP_SELECCIONADORA " +
                                "INNER JOIN TIPOS_LINEAS ON TMP_SELECCIONADORA.Tmp_Cod_Linea=TIPOS_LINEAS.Lin_Codigo " +
                                "INNER JOIN PRODUCTO_BASE ON TMP_SELECCIONADORA.Tmp_Cod_Producto=PRODUCTO_BASE.PB_Codigo " +
                                "INNER JOIN MAQ_STI_STO ON TMP_SELECCIONADORA.Tmp_COD_Maquina=MAQ_STI_STO.MaqST_QRCode " +
                                "Where Tmp_Cod_Caja='" + scanContent + "'";

                        /*String query = "SELECT        TOP (1) *" +
                                "FROM  dbo.TMP_SELECCIONADORA" +
                                "INNER JOIN TIPOS_LINEAS ON TMP_SELECCIONADORA.Tmp_Cod_Linea=TIPOS_LINEAS.Lin_Codigo " +
                                "INNER JOIN PRODUCTO_BASE ON TMP_SELECCIONADORA.Tmp_Cod_Producto=PRODUCTO_BASE.PB_Codigo " +
                                "INNER JOIN MAQ_STI_STO ON TMP_SELECCIONADORA.Tmp_COD_Maquina=MAQ_STI_STO.MaqST_QRCode " +
                                "WHERE  Tmp_Cod_Caja = '" + scanContent + "'";*/
                        try {
                            Statement stmt = con.createStatement();
                            ResultSet rs = stmt.executeQuery(query);
                            if (rs.next()) {
                                txtlinea.setText(rs.getString("Lin_Descrip"));
                                txtproducto.setText(rs.getString("PB_Descrip"));
                                txtmaquina.setText(rs.getString("MaqST_Descrip"));
                                txthorainicio.setText(new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("Tmp_Fecha")) + " " + new SimpleDateFormat("HH:mm").format(rs.getTime("Tmp_Fecha")));
                                txtcodlinea.setText(rs.getString("Tmp_Cod_Linea"));
                                txtcodproducto.setText(rs.getString("Tmp_Cod_Producto"));
                                txtcodmaquina.setText(rs.getString("MaqST_Codigo"));
                                escaneos = 1;
                                ok();
                                scan("ESCANEAR CODIGO OPERADOR");
                            } else {
                                txtlinea.setText("");
                                txtproducto.setText("");
                                txtmaquina.setText("");
                                txthorainicio.setText("");
                                txtcodlinea.setText("");
                                txtcodproducto.setText("");
                                txtcodmaquina.setText("");
                                txtcaja.setText("");
                                error();
                                Toast.makeText(this, "CAJA INCORRECTA, INTENTE CON OTRA CAJA", Toast.LENGTH_SHORT).show();
                                scan("ESCANEAR CODIGO CAJA");
                            }
                            con.close();
                        } catch (Exception ex) {
                            //error
                            escaneos = 0;
                            txtcaja.setText("");
                            error();
                            Toast.makeText(this, "CAJA INCORRECTA, INTENTE CON OTRA CAJA", Toast.LENGTH_SHORT).show();
                            scan("ESCANEAR CODIGO CAJA");
                            System.out.println(ex.toString());
                        }
                    } else {
                        error();
                        Toast.makeText(this, "CODIGO NO CORRESPONDE A CAJA", Toast.LENGTH_SHORT).show();
                        scan("ESCANEAR CODIGO CAJA");
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


    //Diferencias entre dos fechas
    //@param fechaInicial La fecha de inicio
    //@param fechaFinal  La fecha de fin
    //@return Retorna el numero de dias entre dos fechas
    public static synchronized int diferenciasDeFechas(Date fechaInicial, Date fechaFinal) {

        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        String fechaInicioString = df.format(fechaInicial);
        try {
            fechaInicial = df.parse(fechaInicioString);
        } catch (ParseException ex) {
        }

        String fechaFinalString = df.format(fechaFinal);
        try {
            fechaFinal = df.parse(fechaFinalString);
        } catch (ParseException ex) {
        }

        long fechaInicialMs = fechaInicial.getTime();
        long fechaFinalMs = fechaFinal.getTime();
        long diferencia = fechaFinalMs - fechaInicialMs;
        double dias = Math.floor(diferencia / (1000 * 60 * 60 * 24));
        return ((int) dias);
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
}
