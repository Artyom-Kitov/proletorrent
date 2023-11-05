package ru.nsu.ooad.proletorrent.bencode.parser;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Utils {

    static final char[] HEX = "0123456789ABCDEF".toCharArray();

    /**
     * Takes an array of bytes and converts them to a string of
     * hexadecimal characters.
     *
     * @author <a href="https://stackoverflow.com/a/9855338">Link</a>
     * @param bytes byte array
     * @return String
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX[v >>> 4];
            hexChars[j * 2 + 1] = HEX[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Given a byte and a position, returns true if byte at
     * position is set, false otherwise.
     *
     * @param b        byte
     * @param position position in byte
     * @return boolean indicating if bit is 1.
     */
    public static boolean isBitSet(byte b, int position) {
        return ((b >> position) & 1) == 1;
    }

    /**
     * Prints a byte.
     *
     * @param b byte
     */
    public static void printByte(byte b) {
        String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
        System.out.println(s1);
    }

    /**
     * Checks if a list of bytes are all printable ascii chars.
     * They are if the mostleft bit is never set (always 0).
     *
     * @param data array of bytes
     * @return bolean indicating wether this byte is a valid ascii char.
     */
    public static boolean isAllAscii(byte[] data) {
        for (byte b : data)
            if (isBitSet(b, 7))
                return false;
        return true;
    }

    /**
     * Creates the SHA1 hash for a given array of bytes.
     * @param input array of bytes.
     * @return String representation of SHA1 hash.
     */
    public static String SHAsum(byte[] input) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            return byteArray2Hex(md.digest(input));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Takes an array of bytes that should represent a hexadecimal value.
     * Returns string representation of these values.
     * @param bytes bytes containing hex symbols.
     * @return String representation of the byte[]
     */
    private static String byteArray2Hex(final byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    /**
     * Takes a String and returns a byte[] arrays. EAch byte contains the ascii
     * representation of the character in the string.
     * @param s String
     * @return byte array of ascii chars.
     */
    public static byte[] stringToAsciiBytes(String s) {
        byte[] ascii = new byte[s.length()];
        for(int charIdx = 0; charIdx < s.length(); charIdx++) {
            ascii[charIdx] = (byte) s.charAt(charIdx);
        }
        return ascii;
    }

    public static byte readNthByteFromFile(String path, long nth) {
        RandomAccessFile rf = null;
        try {
            rf = new RandomAccessFile(path, "r");

            if (rf.length() < nth)
                throw new EOFException("Reading outside of bounds of file");

            rf.seek(nth);
            byte curr = rf.readByte();
            rf.close();

            return curr;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }  finally {
            assert rf != null;
            try {
                rf.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        return 0;
    }
}
