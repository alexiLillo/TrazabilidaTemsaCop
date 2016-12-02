package cl.fosforos.trazabilidadtemc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.device.ZebraIllegalArgumentException;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Print extends AppCompatActivity {

    private LinearLayout layout;
    private ImageView imageView;

    private Connection connection;
    private UIHelper helper = new UIHelper(this);

    private String ip = "192.168.4.129";
    private String port = "6101";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        layout = (LinearLayout) findViewById(R.id.layoutPrint);
        imageView = (ImageView) findViewById(R.id.im1);

        //asignar imagen URL a un imageView
        new DownloadImageTask(imageView).execute("https://d30y9cdsu7xlg0.cloudfront.net/png/16618-200.png");
    }

    //descargar imagen desde URL
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    //imprimir
    public void printTest(View view) {
        Bitmap rotatedBitmap = rotateBitmap(ConvertToBitmap(layout), 90);
        printBitmap(rotatedBitmap);
    }

    //convertir layout en bitmap (imagen)
    protected Bitmap ConvertToBitmap(LinearLayout layout) {
        layout.setDrawingCacheEnabled(true);
        layout.buildDrawingCache();
        return layout.getDrawingCache();
    }

    //rotar imagen
    private Bitmap rotateBitmap(final Bitmap bitmap, int rotationAngle) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }

    private void printBitmap(final Bitmap bitmap) {

        new Thread(new Runnable() {
            public void run() {

                try {
                    Looper.prepare();
                    connection = getZebraPrinterConn();
                    connection.open();
                    ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
                    getPrinterStatus();

                    ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);

                    PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();

                    /**
                     * check if the printer is ready or not and then send the image to print
                     */

                    if (printerStatus.isReadyToPrint) {
                        try {
                            helper.showLoadingDialog("Printer Ready \nProcessing to Print.");
                            //printer.printImage(new ZebraImageAndroid(bitmap), 0, 0, 550, 412, false);
                            printer.printImage(new ZebraImageAndroid(bitmap), 0, 0, 800, 800, false);
                        } catch (ConnectionException e) {
                            helper.showErrorDialogOnGuiThread(e.getMessage());
                        }
                    } else if (printerStatus.isHeadOpen) {
                        helper.showErrorMessage("Error: Head Open \nPlease Close Printer Head to Print.");
                    } else if (printerStatus.isPaused) {
                        helper.showErrorMessage("Error: Printer Paused.");
                    } else if (printerStatus.isPaperOut) {
                        helper.showErrorMessage("Error: Media Out \nPlease Load Media to Print.");
                    } else {
                        helper.showErrorMessage("Error: Please check the Connection of the Printer.");
                    }

                    connection.close();

                } catch (ConnectionException e) {
                    helper.showErrorDialogOnGuiThread(e.getMessage());
                } catch (ZebraPrinterLanguageUnknownException e) {
                    helper.showErrorDialogOnGuiThread(e.getMessage());
                } finally {
                    bitmap.recycle();
                    helper.dismissLoadingDialog();
                    Looper.myLooper().quit();

                }
            }

        }).start();

    }

    //devolver conexion pot TCP/IP
    private Connection getZebraPrinterConn() {
        int portNumber;
        try {
            portNumber = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            portNumber = 0;
        }
        //return isBluetoothSelected() ? new BluetoothConnection(getMacAddressFieldText()) : new TcpConnection(getTcpAddress(), portNumber);
        return new TcpConnection(ip, portNumber);
    }

    //devolver estado de la impresora
    private void getPrinterStatus() throws ConnectionException {
        final String printerLanguage = SGD.GET("device.languages", connection);
        final String displayPrinterLanguage = "Printer Language is " + printerLanguage;
        SGD.SET("device.languages", "zpl", connection);
        Print.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(Print.this, displayPrinterLanguage + "\n" + "Language set to ZPL", Toast.LENGTH_LONG).show();
                Toast.makeText(Print.this, "Imprimiendo etiqueta...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
