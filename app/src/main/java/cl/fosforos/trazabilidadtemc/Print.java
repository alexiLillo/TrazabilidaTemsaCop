package cl.fosforos.trazabilidadtemc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.onbarcode.barcode.android.AndroidColor;
import com.onbarcode.barcode.android.DataMatrix;
import com.onbarcode.barcode.android.IBarcode;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

import java.io.InputStream;

public class Print extends AppCompatActivity {

    private LinearLayout layout;
    private static ImageView imageViewDataMatrix;

    private Connection connection;
    private UIHelper helper = new UIHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        getSupportActionBar().setTitle("IMPRIMIR ETIQUETAS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layout = (LinearLayout) findViewById(R.id.layoutPrint);
        ImageView imageView = (ImageView) findViewById(R.id.im1);
        imageViewDataMatrix = (ImageView) findViewById(R.id.datamatrix);

        //descargar imagen desde URL
        //new DownloadImageTask(imageView).execute("https://d30y9cdsu7xlg0.cloudfront.net/png/16618-200.png");

        //generateDatamatrix("datamatrix test 01");
        writeQRCode("datamatrix test 01");
    }

    //asignar imagen URL a un imageView
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

    //imprimir (Imagen bitmap, angulo de rotacion)
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
                            //helper.showLoadingDialog("Printer Ready \nProcessing to Print.");
                            helper.showLoadingDialog("Impresora lista\nEnviando información de etiquetas...");
                            //printer.printImage(new ZebraImageAndroid(bitmap), 0, 0, 550, 412, false);
                            //for (int i = 0; i <= 9; i++) {
                            printer.printImage(new ZebraImageAndroid(bitmap), 0, 0, 800, 1200, false);
                            //}
                        } catch (ConnectionException e) {
                            //helper.showErrorDialogOnGuiThread(e.getMessage());
                            helper.showErrorDialogOnGuiThread("Imposible conectar con impresora\nPor favor, verifique la conexión de Red");
                        }
                    } else if (printerStatus.isHeadOpen) {
                        //helper.showErrorMessage("Error: Head Open \nPlease Close Printer Head to Print.");
                        helper.showErrorMessage("Error: Cabezal abierto \nPor favor, cierre el cabezal de la impresora.");
                    } else if (printerStatus.isPaused) {
                        //helper.showErrorMessage("Error: Printer Paused.");
                        helper.showErrorMessage("Error: Impresora pausada...");
                    } else if (printerStatus.isPaperOut) {
                        //helper.showErrorMessage("Error: Media Out \nPlease Load Media to Print.");
                        helper.showErrorMessage("Error: Sin medios \nPor favor, cargue medios para imprimir.");
                    } else {
                        //helper.showErrorMessage("Error: Please check the Connection of the Printer.");
                        helper.showErrorMessage("Error: Por favor, verifique su conexión de Red.");
                    }

                    connection.close();

                } catch (ConnectionException e) {
                    //helper.showErrorDialogOnGuiThread(e.getMessage());
                    helper.showErrorDialogOnGuiThread("Imposible conectar con impresora\nPor favor, verifique la conexión de Red\n\nDetalles del error:\n" + e.getMessage() + "");
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
        int portNumber = 6101;
        String ip = "192.168.4.129";
        return new TcpConnection(ip, portNumber);
        //return isBluetoothSelected() ? new BluetoothConnection(getMacAddressFieldText()) : new TcpConnection(getTcpAddress(), portNumber);
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
                //Toast.makeText(Print.this, "Imprimiendo etiqueta...", Toast.LENGTH_SHORT).show();
            }
        });
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

    //OnBarcode generador de codigos (crea marca de agua)
    public void generateDatamatrix(String dataValue){
        DataMatrix barcode = new DataMatrix();

	/*
	   Data Matrix Valid data char set:
	        ASCII values 0 - 127 in accordance with the US national version of ISO/IEC 646
	            ASCII values 128 - 255 in accordance with ISO 8859-1. These are referred to as extended ASCII.

	*/
        barcode.setData(dataValue);

        barcode.setDataMode(DataMatrix.M_AUTO);

        // if your selected format mode doesnot have enough space to encode your data,
        // the library will choose the right format mode for you automatically.
        barcode.setFormatMode(DataMatrix.F_10X10);

        //  Set the processTilde property to true, if you want use the tilde character "~" to
        //  specify special characters in the input data. Default is false.
        //  1-byte character: ~ddd (character value from 0 ~ 255)
        //  ASCII (with EXT): from ~000 to ~255
        //  2-byte character: ~6ddddd (character value from 0 ~ 65535)
        //  Unicode: from ~600000 to ~665535
        //  ECI: from ~7000000 to ~7999999
        barcode.setProcessTilde(true);

        // if you want to encode GS1 compatible Data Matrix, you need set FNC1 mode to IBarcode.FNC1_ENABLE
        barcode.setFnc1Mode(IBarcode.FNC1_NONE);

        // Unit of Measure, pixel, cm, or inch
        barcode.setUom(IBarcode.UOM_PIXEL);
        // barcode bar module width (X) in pixel
        barcode.setX(3f);

        barcode.setLeftMargin(10f); //10f originalmente
        barcode.setRightMargin(10f);
        barcode.setTopMargin(10f);
        barcode.setBottomMargin(10f);
        // barcode image resolution in dpi
        barcode.setResolution(72); //72 originalmente

        // barcode bar color and background color in Android device
        barcode.setForeColor(AndroidColor.black);
        barcode.setBackColor(AndroidColor.white);

	/*
	specify your barcode drawing area
	    */
        RectF bounds = new RectF(30, 30, 0, 0);

        //bitmap para asignarle canvas
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        try {
            barcode.drawBarcode(canvas,bounds);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //asignar el bitmap a un imageView
        imageViewDataMatrix.setImageBitmap(bitmap);
    }

    //ZXING generador de codigos
    public static void writeQRCode(String codeValue) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            //int width = imageViewDataMatrix.getWidth();
            //int height = imageViewDataMatrix.getHeight();
            int width = 180;
            int height = 180;
            BitMatrix bitMatrix = writer.encode(codeValue, BarcodeFormat.QR_CODE, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    bitmap.setPixel(i, j, bitMatrix.get(i, j) ? Color.BLACK: Color.WHITE);
                }
            }
            imageViewDataMatrix.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
