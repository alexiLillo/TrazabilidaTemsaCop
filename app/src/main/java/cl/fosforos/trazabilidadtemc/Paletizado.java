package cl.fosforos.trazabilidadtemc;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

    TextView txtturno, txtturnoIni, txtturnoFin, txtturnoCod, txtCodPallet, txtCantCajas, txtOperador, txtCCalidad;
    Spinner spinerPTerminado, spinerMarca;
    Button botLeerQR, btReimprimir;
    String scanContent, scanFormat, fechahora, PT_DiaProceso, PT_MesProceso, PT_AñoProceso, correMensu, Caj_LetraCajTraz, rutOper, rutCCalidad;
    private ConexionHelperSQLServer helperSQLServer;
    public int escaneos = 0, cantidadCajas = 0;
    //variables de PRODTERM_LOCAL
    public int prol_codprodbase = -1, prol_codindi_fajgran = -1, prol_codindi_marsinmar = -1, prol_palxcaj = -1, marca_codigo = -1, operFicha, Caj_NumCajTraz;

    private int LastIdPal = 0;

    String prol_codigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paletizado);
        getSupportActionBar().setTitle("PALETIZADO");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        helperSQLServer = new ConexionHelperSQLServer();
        txtturno = (TextView) findViewById(R.id.txtTurno);
        txtturnoIni = (TextView) findViewById(R.id.txtTurnoInicio);
        txtturnoFin = (TextView) findViewById(R.id.txtTurnoFinal);
        txtturnoCod = (TextView) findViewById(R.id.txtTurnoCodigo);
        spinerPTerminado = (Spinner) findViewById(R.id.spinerPTerminado);
        spinerMarca = (Spinner) findViewById(R.id.spinerMarca);
        txtOperador = (TextView) findViewById(R.id.txtOperador);
        txtCCalidad = (TextView) findViewById(R.id.txtCCalidad);
        txtCodPallet = (TextView) findViewById(R.id.txtCodPallet);
        txtCantCajas = (TextView) findViewById(R.id.txtCantCajas);
        botLeerQR = (Button) findViewById(R.id.botLeerQR);
        btReimprimir = (Button) findViewById(R.id.btReimprimir);

        botLeerQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (escaneos == 0)
                    scan("ESCANEAR CODIGO OPERADOR");
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
                                            clearLocalData();
                                            scan("ESCANEAR CODIGO OPERADOR");
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        ArrayAdapter adapterPTerminados = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, llenalistaPTerminadoDes());
        spinerPTerminado.setAdapter(adapterPTerminados);

        spinerPTerminado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String[] cadena = spinerPTerminado.getItemAtPosition(position).toString().split(" - ");
                prol_codigo = cadena[0];

                if (position != 0) {
                    getDatosPRODTERM_LOCAL(prol_codigo);
                    ArrayAdapter adapterMarcas = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, llenalistaMarcas());
                    spinerMarca.setAdapter(adapterMarcas);

                    correMensu = String.valueOf(getCorreMensu(prol_codigo, PT_MesProceso, PT_AñoProceso));
                    if (getCorreMensu(prol_codigo, PT_MesProceso, PT_AñoProceso) < 10)
                        correMensu = "0" + getCorreMensu(prol_codigo, PT_MesProceso, PT_AñoProceso);

                    txtCodPallet.setText(prol_codigo + "-" + correMensu + "/" + PT_DiaProceso);
                } else {
                    ArrayAdapter adapterMarcas = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList());
                    spinerMarca.setAdapter(adapterMarcas);

                    correMensu = "";
                    txtCodPallet.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinerMarca.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinerMarca.getItemAtPosition(position).toString().contains("Seleccione")) {
                    String[] cadena = spinerMarca.getItemAtPosition(position).toString().split(" - ");
                    marca_codigo = Integer.parseInt(cadena[1]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
                PT_DiaProceso = new SimpleDateFormat("yyyy/MM/dd").format(rs.getDate("PT_DiaProceso")).replace("/", "");
                PT_MesProceso = new SimpleDateFormat("MM").format(rs.getDate("PT_DiaProceso")).replace("/", "");
                PT_AñoProceso = new SimpleDateFormat("yyyy").format(rs.getDate("PT_DiaProceso")).replace("/", "");
                break;
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
    }

    public void getDatosPRODTERM_LOCAL(String prol_codigo) {
        Connection con = helperSQLServer.CONN();
        String query = "Select * from PRODTERM_LOCAL where Prol_Codigo='" + prol_codigo + "'";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                prol_codprodbase = rs.getInt("ProL_CodProdBase");
                prol_codindi_fajgran = rs.getInt("ProL_CodIndi_FajGran");
                prol_codindi_marsinmar = rs.getInt("ProL_CodIndi_MarSinMar");
                prol_palxcaj = rs.getInt("ProL_PalxCaj");
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
    }

    public int getCorreMensu(String prol_codigo, String month, String year) {
        Connection con = helperSQLServer.CONN();
        int correMensu = 0;
        String query = "Select top 1 Pal_CorreMensu from VIS_TRAZA_PALLET where Pal_CodEmp=2 and MONTH(PT_DiaProceso)='" + month + "' and YEAR(PT_DiaProceso)='" + year + "' and Pal_CodProd_LOC='" + prol_codigo + "' order by Pal_CorreMensu desc";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                correMensu = rs.getInt("Pal_CorreMensu");
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
        if (correMensu == 0)
            return 1;
        else
            return correMensu + 1;
    }

    public int getLastIdPallet() {
        Connection con = helperSQLServer.CONN();
        String query = "Select max(Pal_IDPal) as lastPal from TRAZA_PALLET";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                LastIdPal = rs.getInt("lastPal");
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
        return LastIdPal;
    }

    public ArrayList<String> llenalistaPTerminadoDes() {
        Connection con = helperSQLServer.CONN();
        ArrayList<String> lista = new ArrayList<>();
        String query = "Select * from PRODTERM_LOCAL order by ProL_Descrip";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            lista.add("Seleccione Producto Terminado");
            while (rs.next()) {
                lista.add(rs.getString("ProL_Codigo") + " - " + rs.getString("ProL_Descrip"));
            }
            con.close();
        } catch (Exception ex) {
            //error
            Toast.makeText(this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
        }
        return lista;
    }

    public ArrayList<String> llenalistaMarcas() {
        Connection con = helperSQLServer.CONN();
        ArrayList<String> lista = new ArrayList<>();
        String query = "";

        if (prol_codindi_marsinmar == 1) {
            lista.add("Seleccione Marca");
            query = "Select * from MARCAS EXCEPT Select * from MARCAS where Marca_Codigo=0 or Marca_Codigo=18 order by Marca_Codigo";
        } else if (prol_codindi_marsinmar == 0)
            query = "Select * from MARCAS where Marca_Codigo=0";

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                lista.add(rs.getString("Marca_Descrip") + " - " + rs.getString("Marca_Codigo"));
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
        return lista;
    }

    public boolean validarCaja(String qrCaja) {
        Connection con = helperSQLServer.CONN();
        String query = "";
        boolean valida = false;
        if (prol_codindi_fajgran == 1)
            query = "Select * from TRAZA_CAJA where Caj_QRCode_FAJ='" + qrCaja + "'";
        else if (prol_codindi_marsinmar == 1 && prol_codindi_fajgran == 0)
            query = "Select * from TRAZA_CAJA where Caj_QRCode_Mar='" + qrCaja + "'";
        else if (prol_codindi_marsinmar == 0 && prol_codindi_fajgran == 0)
            query = "Select * from TRAZA_CAJA where Caj_QRCode_ST='" + qrCaja + "'";

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                if (marca_codigo == 0) {
                    if (rs.getInt("Caj_IDPal") == 0 && prol_codprodbase == rs.getInt("Caj_CodProdBase") && prol_codindi_fajgran == rs.getInt("Caj_CodIndi_FajGran") && (marca_codigo == rs.getInt("Caj_CodMarca_MAR") || rs.getInt("Caj_CodMarca_MAR") == 18))
                        valida = true;

                } else {
                    if (rs.getInt("Caj_IDPal") == 0 && prol_codprodbase == rs.getInt("Caj_CodProdBase") && prol_codindi_fajgran == rs.getInt("Caj_CodIndi_FajGran") && marca_codigo == rs.getInt("Caj_CodMarca_MAR"))
                        valida = true;
                }

                Caj_LetraCajTraz = rs.getString("Caj_LetraCajTraz");
                Caj_NumCajTraz = rs.getInt("Caj_NumCajTraz");

            }
            con.close();
        } catch (Exception ex) {
            //error
        }
        return valida;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null) {
            if (scanningResult.getContents() != null) {
                scanContent = scanningResult.getContents();
                scanFormat = scanningResult.getFormatName();

                if (escaneos >= 2) {
                    //validar caja (aumentar contador), generar pallet (o aumnetar cajas de pallet), actualizar idpallet en la caja e imprimir etiqueta, repetir proceso
                    if (validarCaja(scanContent)) {
                        cantidadCajas += 1;
                        if (cantidadCajas == 1) {
                            //agregar pallet(LastIdPallet + 1)
                            getLastIdPallet();
                            LastIdPal += 1;

                            insertarPallet(2, LastIdPal, Integer.parseInt(txtturnoCod.getText().toString()), prol_codigo, Integer.parseInt(correMensu), txtCodPallet.getText().toString(), cantidadCajas, getFichaOper(rutOper), getNombreOper(rutOper), getFichaOper(rutCCalidad), getNombreOper(rutCCalidad));

                        } else if (cantidadCajas > 1) {
                            //actualizar cantidadCajas pallet (LastIdPallet)
                            updateCantidadCajasPallet(LastIdPal, cantidadCajas);
                        }

                        updateTrazaCaja(LastIdPal);

                        escaneos += 1;
                        txtCantCajas.setText(String.valueOf(cantidadCajas));
                        ok();
                        scan("ESCANEAR CAJAS PRODUCTO TERMINADO, ESCANEADAS: " + cantidadCajas);
                    } else {
                        //ventana emergente con datos de caja invalida
                        error();
                        //Toast.makeText(this, "CODIGO DE CAJA INVALIDO", Toast.LENGTH_SHORT).show();
                        //scan("ESCANEAR CAJAS PRODUCTO TERMINADO, ESCANEADAS: " + cantidadCajas);

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(infoCajaTraz())
                                .setTitle("Atención! Caja inválida, QR: " + scanContent)
                                .setCancelable(false)
                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                scan("ESCANEAR CAJAS PRODUCTO TERMINADO, ESCANEADAS: " + cantidadCajas);
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }

                if (escaneos == 1) {
                    //escanear CCalidad (VALIDAR CCALIDAD)
                    if (scanContent.substring(0, scanContent.length() - 1).endsWith("-")) {
                        rutCCalidad = scanContent;
                        txtCCalidad.setText(getNombreOper(rutCCalidad));
                        escaneos = 2;
                        ok();
                        scan("ESCANEAR CAJAS PRODUCTO TERMINADO, ESCANEADAS: " + cantidadCajas);
                    } else {
                        error();
                        Toast.makeText(this, "CODIGO DE CONTROL DE CALIDAD INVALIDO", Toast.LENGTH_SHORT).show();
                        scan("ESCANEAR CODIGO DE CONTROL DE CALIDAD");
                    }
                }

                if (escaneos == 0) {
                    //escanear Operador (VALIDAR OPERADOR)
                    if (scanContent.substring(0, scanContent.length() - 1).endsWith("-")) {
                        rutOper = scanContent;
                        txtOperador.setText(getNombreOper(rutOper));
                        escaneos = 1;
                        ok();
                        scan("ESCANEAR CODIGO DE CONTROL DE CALIDAD");
                    } else {
                        error();
                        Toast.makeText(this, "CODIGO DE OPERADOR INVALIDO", Toast.LENGTH_SHORT).show();
                        scan("ESCANEAR CODIGO OPERADOR");
                    }
                }

            }
        } else {
            error();
            Toast.makeText(this, "NO SE ESCANEO NINGUN CODIGO", Toast.LENGTH_SHORT).show();
        }
    }

    public String infoCajaTraz() {
        String info = "";
        Connection con = helperSQLServer.CONN();
        String query = "Select * from VIS_CAJA_TRAZABLE where Caj_LetraCajTraz='" + Caj_LetraCajTraz + "' and Caj_NumCajTraz='" + Caj_NumCajTraz + "'";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String fajado;
                if (rs.getInt("Caj_CodIndi_FajGran") == 1)
                    fajado = "SI";
                else
                    fajado = "NO";

                info =    "COD. CAJA:\t" + Caj_LetraCajTraz + "-" + Caj_NumCajTraz + "\n"
                        + "PROD. BASE:\t" + rs.getString("PB_Descrip") + "\n"
                        + "MARCA:\t" + rs.getString("Marca_Descrip") + "\n"
                        + "FAJADO:\t" + fajado + "\n"
                        + "COD. PALLET:\t" + rs.getString("Pal_CodPallet");
            }
            con.close();
        } catch (Exception ex) {
            //error
        }
        return info;
    }

    public void insertarPallet(int pal_codemp, int pal_idpal, int pal_codturno, String pal_codprod_loc, int pal_corremensu, String pal_codpallet, int pal_canticajas, int pal_fichaoper, String pal_nombreoper, int pal_fichaccali, String pal_nombccali) {
        try {
            Connection con = helperSQLServer.CONN();
            if (con == null) {
            } else {
                //Consulta SQL
                String query = "insert into TRAZA_PALLET values ('" + pal_codemp + "', '" + pal_idpal + "', '" + pal_codturno + "', '" + pal_codprod_loc + "', '" + pal_corremensu + "', '" + pal_codpallet + "', '" + pal_canticajas + "', '" + pal_fichaoper + "', '" + pal_nombreoper + "', '" + pal_fichaccali + "', '" + pal_nombccali + "')";
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.close();
            }
        } catch (Exception ex) {
        }
    }

    public void updateCantidadCajasPallet(int pal_idpal, int pal_canticajas) {
        try {
            Connection con = helperSQLServer.CONN();
            if (con == null) {
            } else {
                //Consulta SQL
                String query = "update TRAZA_PALLET set Pal_CantiCajas='" + pal_canticajas + "' where Pal_IDPal='" + pal_idpal + "'";
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.close();
            }
        } catch (Exception ex) {
        }
    }

    public int getFichaOper(String rut) {
        Connection conOpe = helperSQLServer.CONN_Oper();
        int ficha = 0;
        String queryOperario = "SELECT * FROM MAESTRO Where Rut=" + Integer.parseInt(rut.substring(0, rut.length() - 2));
        try {
            Statement stmt = conOpe.createStatement();
            ResultSet rs = stmt.executeQuery(queryOperario);
            if (rs.next()) {
                ficha = rs.getInt("Ficha");
            }
            conOpe.close();
        } catch (Exception ex) {
        }
        return ficha;
    }

    public String getNombreOper(String rut) {
        Connection conOpe = helperSQLServer.CONN_Oper();
        String nombre = "";
        String queryOperario = "SELECT * FROM MAESTRO Where Rut=" + Integer.parseInt(rut.substring(0, rut.length() - 2));
        try {
            Statement stmt = conOpe.createStatement();
            ResultSet rs = stmt.executeQuery(queryOperario);
            if (rs.next()) {
                nombre = rs.getString("Nombre");
            }
            conOpe.close();
        } catch (Exception ex) {
        }
        return nombre;
    }

    public void updateTrazaCaja(int caj_idpal) {
        //traer datos de TIPOS_MPRIMAS
        String Caj_MP_Envase = "", Caj_MP_Tinta = "", Caj_MP_Papel = "", Caj_MP_Parafina = "", Caj_MP_Adhesivo = "";
        //String MP_Env_PH93 = "", MP_Env_PHCBT114 = "", MP_Env_CBT93, MP_Parafina = "", MP_AdhesivoCBT = "", MP_AdhesivoPH = "", MP_Tinta = "", MP_Papel = "";
        String query0 = "Select * from TIPOS_MPRIMAS";
        try {
            Connection con0 = helperSQLServer.CONN();
            Statement stmt0 = con0.createStatement();
            ResultSet rs0 = stmt0.executeQuery(query0);
            while (rs0.next()) {

                if (prol_codprodbase == 1) {
                    Caj_MP_Envase = rs0.getString("MP_Env_PH93");
                } else if (prol_codprodbase == 2 || prol_codprodbase == 4) {
                    Caj_MP_Envase = rs0.getString("MP_Env_PHCBT114");
                } else if (prol_codprodbase == 3) {
                    Caj_MP_Envase = rs0.getString("MP_Env_CBT93");
                }

                if (prol_codindi_marsinmar == 1)
                    Caj_MP_Tinta = rs0.getString("MP_Tinta");

                if (prol_codindi_fajgran == 1)
                    Caj_MP_Papel = rs0.getString("MP_Papel");

                Caj_MP_Parafina = rs0.getString("MP_Parafina");

                if (prol_codprodbase == 1 || prol_codprodbase == 2) {
                    Caj_MP_Adhesivo = rs0.getString("MP_AdhesivoPH");
                } else if (prol_codprodbase == 3 || prol_codprodbase == 4) {
                    Caj_MP_Adhesivo = rs0.getString("MP_AdhesivoCBT");
                }

            }
            con0.close();
        } catch (Exception ex) {
            //error
        }

        //ACTUALIZAR TRAZA_CAJA
        try {
            Connection con = helperSQLServer.CONN();
            if (con == null) {
            } else {
                //Consulta SQL
                String query = "update TRAZA_CAJA set Caj_IDPal='" + caj_idpal + "', Caj_MP_Envase='" + Caj_MP_Envase + "', Caj_MP_Tinta='" + Caj_MP_Tinta + "', Caj_MP_Papel='" + Caj_MP_Papel + "', Caj_MP_Parafina='" + Caj_MP_Parafina + "', Caj_MP_Adhesivo='" + Caj_MP_Adhesivo + "' where Caj_LetraCajTraz='" + Caj_LetraCajTraz + "' and Caj_NumCajTraz='" + Caj_NumCajTraz + "'";
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.close();
            }
        } catch (Exception ex) {
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

    private void clearLocalData() {
        txtOperador.setText("");
        rutOper = "";
        txtCCalidad.setText("");
        rutCCalidad = "";
        txtCodPallet.setText("");
        txtCantCajas.setText("");
        correMensu = "";
        escaneos = 0;

        prol_codprodbase = -1;
        prol_codindi_fajgran = -1;
        prol_codindi_marsinmar = -1;
        prol_palxcaj = -1;
        marca_codigo = -1;

        cantidadCajas = 0;
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
