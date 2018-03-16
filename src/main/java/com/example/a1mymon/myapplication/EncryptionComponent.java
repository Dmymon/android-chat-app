package com.example.a1mymon.myapplication;

import org.encryptor4j.Encryptor;
import org.encryptor4j.factory.KeyFactory;

import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;

/**
 * Created by 1mymon on 22/07/2017.
 */

/**EncryptionComponent holds the interface for data encryption
 */

public class EncryptionComponent {

    private Encryptor encryptor;
    private SecretKey secretKey;

    /**
     * Constructor (generate random key)
     */
    public EncryptionComponent()
    {
        if (encryptor == null && secretKey == null) {
            secretKey = (SecretKey) KeyFactory.AES.randomKey();
            encryptor = new Encryptor(secretKey, "AES/GCM/NoPadding");
        }
    }

    /**
     * Constructor
     * @param key the shared key for the encryption/decryption process
     */
    public EncryptionComponent(String key)
    {
        if (encryptor == null && secretKey == null)
        {
            secretKey = (SecretKey) KeyFactory.AES.keyFromPassword(key.toCharArray());
            encryptor = new Encryptor(secretKey,"AES/GCM/NoPadding");
        }
    }

    /**
     * encrypts a message- String
     * @param message message to encrypt
     * @return
     */
    public String Encrypt(String message)
    {
        String s = null;
        try {
            s = encryptor.encrypt(message.getBytes()).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return s;
    }

    /**
     * decrypts a message- String
     * @param encrypted message to decrypt
     * @return
     */
    public String Decrypt(String encrypted)
    {
        String s = null;
        try
        {
            s = encryptor.decrypt(encrypted.getBytes()).toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return s;
    }

    /**
     * @return the generated secret key
     */
    public String GetSecret()
    {
        return secretKey.getEncoded().toString();
    }

}
