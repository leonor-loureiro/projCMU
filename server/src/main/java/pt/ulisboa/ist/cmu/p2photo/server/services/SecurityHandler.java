package pt.ulisboa.ist.cmu.p2photo.server.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.mindrot.jbcrypt.BCrypt;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Random;

public class SecurityHandler {

    /**
     * Token's valid period
     */
    private static final int validPeriod = 1800000; // 30 minutes

    private static String keystoreFile = System.getProperty("user.dir") + "/keystore";
    private static String keystorePwd = "password";
    private static String alias = "server";

    private static Random random = new Random();

    public SecurityHandler(){
    }


    /**
     * jBCrypt is an implementation the OpenBSD Blowfish password hashing
     * algorithm, as described in "A Future-Adaptable Password Scheme" by Niels
     * Provos and David Mazieres: http://www.openbsd.org/papers/bcrypt-paper.ps
     * @param password the password to be hashed
     * @return the hashed password
     */
    public String hashPassword (String password){
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    /**
     * Compares the hashed password to the password
     * @param password password
     * @param hashed hashed verion of the password
     * @return true if both match
     */
    public boolean passwordMatches(String password, String hashed) {

        return BCrypt.checkpw(password, hashed);
    }

    /**
     * generates a token for a user
     * @param username the user whom the token is aimed for
     * @return the token generated
     */
    public String generateToken(String username){

        return createJTW(username);
    }


    /**
     *
     * @param subject the user to which the token is being created for
     * @return a jason web token for the user
     */
    private String createJTW(String subject){

        Key signingKey = getPrivateKey();

        String id = "" + random.nextInt(9000000) + 1000000;
        String issuer = "server";

        if(id == null || id.isEmpty() || issuer == null || issuer.isEmpty() ||
                subject == null || subject.isEmpty() || validPeriod < 0)
            return null;

        //The JWT signature algorithm we will be using to sign the token
        //PKCS#1 signature with SHA-256
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;

        long currentTimeMillis = System.currentTimeMillis();
        Date currDate = new Date(currentTimeMillis);
        Date expiredDate = new Date(currentTimeMillis + validPeriod);


        // Set the JWT Claims
        JwtBuilder tokenBuilder = Jwts.builder().setId(id)     //unique identifier of the token
                .setIssuedAt(currDate)                         //time the token was issued
                .setSubject(subject)                           //the subject the token was issued to
                .setIssuer(issuer)                             //principal that issued the token
                .setExpiration(expiredDate)
                .signWith(signatureAlgorithm, signingKey);     //signature


        //Builds the token and serializes it to a compact, URL safe string
        return tokenBuilder.compact();
    }


    /**
     * Checks if the token is valid and satisfies all claims
     * @param jtw token
     * @param subject subject the token was supposedly created for
     * @return true if valid; false otherwise
     */
    public boolean validateJTW(String jtw, String subject){

        PublicKey key = (PublicKey) getPublicKey();

        String issuer = "server";

        try {
            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jtw).getBody();

            String jtwSubject = claims.getSubject();
            String jtwIssuer = claims.getIssuer();
            Date jtwExpireDate = claims.getExpiration();
            Date jtwIssuedAt = claims.getIssuedAt();
            Date currentDate = new Date(System.currentTimeMillis());

            return jtwSubject.equals(subject) && jtwIssuer.equals(issuer) &&
                    jtwIssuedAt.before(currentDate) && jtwExpireDate.after(currentDate);

        }catch (Exception e){
            return false;
        }
    }

    /**
     *
     * @return the servers' public key
     */
    private Key getPublicKey(){

        KeyStore keystore = loadKeystore();
          // Get public key certificate

        try {
            if(keystore!= null)
                return  keystore.getCertificate(alias).getPublicKey();

        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * @return the server's private key
     */
    private Key getPrivateKey(){
        KeyStore keystore = loadKeystore();

        // Get the key
        try {
            return keystore.getKey(alias, keystorePwd.toCharArray());

        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Loads the keystore
     * @return the keystore
     */
    private static KeyStore loadKeystore() {
        try {
            FileInputStream is = new FileInputStream(keystoreFile);

            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, keystorePwd.toCharArray());

            return keystore;

        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            e.printStackTrace();
        }

        return null;

    }



}
