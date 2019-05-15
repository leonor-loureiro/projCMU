package pt.ulisboa.tecnico.cmov.p2photo.security;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SecurityManager {
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String ENCRYPTION_MODE = "AES/GCM/NoPadding";
    private static final String TAG = "SecurityManager";
    private static final String FIXED_IV = "ePJHHZOrVdbiYuFM";


    /*****************************************************************************
     *
     *                         ASYMMETRIC ENCRYPTION
     *
     *****************************************************************************/

    /**
     * Generates an RSA key pair for encryption/decryption using RSA OAEP
     * @param alias key alias
     * @return keypair
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static KeyPair generateRSAKeyPair(String alias){
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE);

            keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .build()
            );
            return keyPairGenerator.generateKeyPair();

        } catch (Exception e) {
            Log.i(TAG, "Generate key pair failed: " + alias);
            e.printStackTrace();
        }
        return null;
    }

    public static PublicKey getPublicKeyFromString(String keyStr){
        try {
            byte[] encodedKey = Base64.decode(keyStr, Base64.DEFAULT);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(encodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String publicKeyToString(PublicKey publicKey){
        try {
            KeyFactory fact = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec spec = fact.getKeySpec(
                    publicKey,
                    X509EncodedKeySpec.class);
            return Base64.encodeToString(spec.getEncoded(), Base64.DEFAULT);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Gets private key from keystore
     * @param alias key alias
     * @return private key
     */
    public static PrivateKey getPrivateKey(String alias){
        try {
            KeyStore keyStore = getAndroidKeyStore();
            return  (PrivateKey) keyStore.getKey(alias, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets public key from keystore
     * @param alias key alias
     * @return public key
     */
    public static PublicKey getPublicKey(String alias){
        try {
            KeyStore keyStore = getAndroidKeyStore();
            return keyStore.getCertificate(alias).getPublicKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String encryptRSA(PublicKey publicKey, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] cipheredBytes = cipher.doFinal(data);
            return Base64.encodeToString(cipheredBytes, Base64.DEFAULT);
        }catch (GeneralSecurityException e){
            Log.e(TAG, "Encrypt RSA failed");
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decryptRSA(PrivateKey privateKey, String cipheredData) {
        try {
            byte[] cipheredBytes = Base64.decode(cipheredData, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(cipheredBytes);
        }catch (GeneralSecurityException e){
            Log.e(TAG, "Decrypt RSA failed");
            e.printStackTrace();
            return null;
        }

    }


    /*****************************************************************************
     *
     *                         SYMETRIC ENCRYPTION
     *
     *****************************************************************************/


    public static String encryptAES(SecretKey secretKey, String data){

        byte[] dataBytes = Base64.encode(data.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        try {

            byte[] cipheredBytes = encryptAES(secretKey, dataBytes);
            return Base64.encodeToString(cipheredBytes, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public static String decryptAES(SecretKey secretKey, String cipher){
        try {
            byte[] dataBytes = decryptAES(secretKey, Base64.decode(cipher, Base64.DEFAULT));
            return new String(Base64.decode(dataBytes, Base64.DEFAULT), StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static SecretKey getSecretKey(String alias){

        try {
            KeyStore keyStore = getAndroidKeyStore();
            return (SecretKey) keyStore.getKey(alias, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "GetSecretKey: failed");

        return null;
    }


    public static SecretKey generateSecretKey(){
        try {

            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            return keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Generate secret key failed");
            e.printStackTrace();
        }
        return null;
    }


    public static SecretKey getSecretKeyFromBytes(byte[] encodedKey){
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

    private static byte[] encryptAES(SecretKey secretKey, byte[] data)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    private static byte[] decryptAES(SecretKey secretKey, byte[] cipheredData)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(cipheredData);
    }


    /*
    private static GCMParameterSpec getIV(){
        return new GCMParameterSpec(128, Base64.decode(FIXED_IV, Base64.DEFAULT));
    }

    private static byte[] encryptAES(SecretKey secretKey, byte[] data){
        try {

            Cipher c = Cipher.getInstance(ENCRYPTION_MODE);
            c.init(Cipher.ENCRYPT_MODE, secretKey, getIV());
            byte[] encodedBytes = c.doFinal(data);

            return encodedBytes;

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "Encrypt: failed");
        return null;
    }

    private static byte[] decryptAES(SecretKey secretKey, byte[] cipher){
        try {
            Cipher c = Cipher.getInstance(ENCRYPTION_MODE);
            c.init(Cipher.DECRYPT_MODE, secretKey, getIV());
            return c.doFinal(cipher);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e(TAG, "Decrypt: failed");
        return null;
    }

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

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

    /*****************************************************************************
     *
     *                           AUXILIAR FUNCTIONS
     *
     *****************************************************************************/

    /**
     * Loads the android keystore
     */
    private static KeyStore getAndroidKeyStore()
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE);
        keyStore.load(null);
        return keyStore;
    }
}
