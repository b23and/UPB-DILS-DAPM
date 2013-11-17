package com.example.savedisplay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.util.Log;

public class Utils 
{
	static final String SAVED_FILE = "savedData.txt"; //The file in which we save data
	public static Context activityContext; //A reference to the context from which we want to write/read to/from the file;
										   //The methods openFileInput() and openFileOutput() belong to an activity context
	static final String SEED = "Master-Password"; //Seed used for randomization
	
	//***************READ, WRITE, DELETE FILE******************
	public static String readFromFile() 
	{
		String ret = "";

		try 
		{
			if (activityContext != null)
			{
				InputStream inputStream = activityContext.openFileInput(SAVED_FILE);

				if ( inputStream != null ) 
				{
					InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
					String receiveString = "";
					StringBuilder stringBuilder = new StringBuilder();

					while ( (receiveString = bufferedReader.readLine()) != null ) 
					{
						stringBuilder.append(receiveString);
					}

					inputStream.close();
					ret = stringBuilder.toString();
				}
			}
			else
			{
				Log.d("CONTEXT", "Activity context is null");
			}
		}
		catch (FileNotFoundException e) 
		{
			Log.e("login activity", "File not found: " + e.toString());
			return "";
		} 
		catch (IOException e) 
		{
			Log.e("login activity", "Can not read file: " + e.toString());
		}

		return ret;
	}

	public static void writeToFile(String data, int mode) 
	{
		try 
		{
			if (activityContext != null)
			{
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(activityContext.openFileOutput(SAVED_FILE, mode));
				outputStreamWriter.write(data);
				outputStreamWriter.close();
			}
		}
		catch (IOException e) 
		{
			Log.e("Exception", "File write failed: " + e.toString());
		}

		Log.d("FILE_CHECK", "FILE CHECK " + readFromFile());
	}
	
	public static void deleteFile()
	{
		if (activityContext != null)
		{
			File dir = activityContext.getFilesDir();
			File fileToUpdate = new File(dir, Utils.SAVED_FILE);
			boolean isDeleted = fileToUpdate.delete();
			Log.d("DELETE", " is " + isDeleted);
		}
	}
	//***************ENCRYPT & DECRYPT******************
	public static String encrypt(String seed, String cleartext) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return toHex(result);
	}

	public static String decrypt(String seed, String encrypted) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}

	private static byte[] getRawKey(byte[] seed) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seed);
		kgen.init(128, sr); // 192 and 256 bits may not be available
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();
		return raw;
	}


	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public static String toHex(String txt) {
		return toHex(txt.getBytes());
	}
	public static String fromHex(String hex) {
		return new String(toByte(hex));
	}

	public static byte[] toByte(String hexString) {
		int len = hexString.length()/2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
		return result;
	}

	public static String toHex(byte[] buf) {
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2*buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}
	private final static String HEX = "0123456789ABCDEF";
	private static void appendHex(StringBuffer sb, byte b) {
		sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
	}
}
