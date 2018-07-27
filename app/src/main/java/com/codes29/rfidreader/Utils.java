package com.codes29.rfidreader;

import android.util.Log;

import java.math.BigInteger;

/**
 * Created by LWPHP1 on 5/31/2018.
 */

public class Utils {




    /**
     * Get hex string from byte array
     * @param buf Byte buffer
     * @return Hex string
     */
    public static String getHexString(byte[] buf)
    {
        Log.i("Utils","Converting Bytes to Binary String...");

        StringBuilder sb = new StringBuilder();

        for (byte b : buf)
            sb.append(String.format("%02X ", b));

        Log.w("Utils","Converted Byte string is:  "+sb.toString().trim());
        return sb.toString().trim();
    }

    /**
     * Get byte array from binary string
     * @param s Binary string
     * @return Byte array
     */
    public static byte[] binaryStringToByteArray(String s)
    {
        byte[] ret = new byte[(s.length()+8-1) / 8];

        BigInteger bigint = new BigInteger(s, 2);
        byte[] bigintbytes = bigint.toByteArray();

        if (bigintbytes.length > ret.length) {
            //get rid of preceding 0
            for (int i = 0; i < ret.length; i++) {
                ret[i] = bigintbytes[i+1];
            }
        }
        else {
            ret = bigintbytes;
        }
        return ret;
    }

    /**
     * Get binary string from byte array
     * @param input Byte array
     * @return Binary string
     */
    public static String getBinaryString(byte[] input)
    {
        Log.i("Utils","Converting Bytes to Binary String...");
        StringBuilder sb = new StringBuilder();

        for (byte c : input)
        {
            for (int n = 128; n > 0; n >>= 1)
            {
                String res = ((c & n) == 0) ? "0" : "1";
                sb.append(res);
            }
        }

        Log.w("Utils","Converted Byte string is:  "+sb.toString().trim());

        return sb.toString().trim();
    }



}
