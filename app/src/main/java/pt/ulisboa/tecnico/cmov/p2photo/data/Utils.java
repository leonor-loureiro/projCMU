package pt.ulisboa.tecnico.cmov.p2photo.data;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;

public class Utils {

    /**
     * Opens a warning box with the given title and message and button OK
     * @param context application context
     * @param title dialog box title
     * @param message dialog box message
     */
    public static void openWarningBox(Context context, String title, String message){

        AlertDialog alertDialog = createDialog(context, title, message);

        //Set listener for OK button
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /**
     * Creates dialog box with given title and message
     * @param context application context
     * @param title dialog box title
     * @param message dialog box message
     * @return dialog box
     */
    private static AlertDialog createDialog(Context context, String title, String message){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        //Sets dialog box title and message, if exists
        if(title!=null)
            alertDialog.setTitle(title);

        if(message!=null)
            alertDialog.setMessage(message);
        return alertDialog;
    }

    /**
     * Create dialog box with given message and title and YES and NO buttons
     * @param context application context
     * @param title dialog box title
     * @param message dialog box message
     * @param actionYes callback to be called when user select yes
     * @param actionNo callback to be called when user selects no
     */
    public static void openYesNoBox(Context context, String title, String message,
                                      final Callable<Void> actionYes, final Callable<Void> actionNo ){

       AlertDialog alertDialog = createDialog(context, title, message);

        //Set listener for button yes
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            actionYes.call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                });

        //Sets listener for button no
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            actionNo.call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static String getPath( Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        return result;
    }

    public static byte[] encodeBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap decodeBitmap(byte[] encodedBitmap){
        return BitmapFactory.decodeByteArray(encodedBitmap, 0, encodedBitmap.length);
    }

}
