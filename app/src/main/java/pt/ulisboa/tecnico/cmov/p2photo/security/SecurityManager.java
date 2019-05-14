package pt.ulisboa.tecnico.cmov.p2photo.security;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class SecurityManager {
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String ENCRYPTION_MODE = "AES/GCM/NoPadding";
    private static final String TAG = "SecurityManager";
    private static final String FIXED_IV = "ePJHHZOrVdbiYuFM";

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static SecretKey generateSecretKey(String alias){
        try {
            SecretKey secretKey = getSecretKey(alias);
            if(secretKey != null){
                Log.i(TAG, "Secret Key already generated: " + alias);
                return secretKey;
            }

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(alias,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            //Fixed IV
                            .setRandomizedEncryptionRequired(false)
                            .build());

            return keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String encrypt(String alias, String data){
        //Get secret key. If not exists, create it
        SecretKey secretKey = generateSecretKey(alias);
        if(secretKey == null) {
            Log.e(TAG, "Encrypt: secret key not found");
            return null;
        }

        byte[] dataBytes = Base64.encode(data.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        byte[] cipheredBytes = encrypt(secretKey, dataBytes);

        return Base64.encodeToString(cipheredBytes, Base64.DEFAULT);
    }

    private static GCMParameterSpec getIV(){
        return new GCMParameterSpec(128, Base64.decode(FIXED_IV, Base64.DEFAULT));
    }

    public static String decrypt(String alias, String cipher){
        SecretKey secretKey = getSecretKey(alias);
        if(secretKey == null) {
            Log.e(TAG, "Decrypt: secret key not found");
            return null;
        }

        byte[] dataBytes = decrypt(secretKey, Base64.decode(cipher, Base64.DEFAULT));
        if(dataBytes == null) {
            Log.e(TAG, "Decrypt: failed");
            return null;
        }
        return new String(Base64.decode(dataBytes, Base64.DEFAULT), StandardCharsets.UTF_8);
    }


    private static SecretKey getSecretKey(String alias){

        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE);
            keyStore.load(null);
            return (SecretKey) keyStore.getKey(alias, null);

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e(TAG, "GetSecretKey: failed");

        return null;
    }

    private static byte[] encrypt(SecretKey secretKey, byte[] data){
        try {

            Cipher c = Cipher.getInstance(ENCRYPTION_MODE);
            c.init(Cipher.ENCRYPT_MODE, secretKey, getIV());
            byte[] encodedBytes = c.doFinal(data);

            return encodedBytes;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "Encrypt: failed");
        return null;
    }

    private static byte[] decrypt(SecretKey secretKey, byte[] cipher){
        try {
            Cipher c = Cipher.getInstance(ENCRYPTION_MODE);
            c.init(Cipher.DECRYPT_MODE, secretKey, getIV());
            return c.doFinal(cipher);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        Log.e(TAG, "Decrypt: failed");
        return null;
    }
}
