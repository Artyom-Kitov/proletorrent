package ru.nsu.ooad.proletorrent.bencode.parser;

import org.apache.commons.io.IOUtils;
import ru.nsu.ooad.proletorrent.bencode.BencodeException;
import ru.nsu.ooad.proletorrent.bencode.parser.objects.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Reader {
    private int currentByteIndex;
    private byte[] datablob;

    public Reader(File file) throws IOException
    {
        datablob = IOUtils.toByteArray(new FileInputStream(file));
    }

    public Reader(InputStream input) throws IOException
    {
        datablob = IOUtils.toByteArray(input);
    }

    public Reader(String s)
    {
        datablob = s.getBytes();
    }

    /**
     * Starts reading from the beginning of the file.
     * Keeps reading single types and adds them to the list to finally return
     * them.
     *
     * @return List<Object> containing all the parsed bencoded objects.
     */
    public synchronized List<IBencodeObject> read()
    {
        this.currentByteIndex = 0;
        long fileSize = datablob.length;

        List<IBencodeObject> dataTypes = new ArrayList<>();
        while (currentByteIndex < fileSize)
            dataTypes.add(readSingleType());

        return dataTypes;
    }

    /**
     * Tries to read in an object starting at the current byte index.
     * If not possible throws an exception.
     *
     * @return Returns an Object that represents either BByteString,
     * BDictionary, BInt or BList.
     */
    private IBencodeObject readSingleType()
    {
        // Read in the byte at current position and dispatch over it.
        byte current = datablob[currentByteIndex];
        switch (current)
        {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return readByteString();
            case 'd':
                return readDictionary();
            case 'i':
                return readInteger();
            case 'l':
                return readList();
        }
        throw new BencodeException("Parser in invalid state at byte " + currentByteIndex);
    }

    /**
     * Reads in a list starting from the current byte index. Throws an error if
     * not called on an appropriate index.
     * A list of values is encoded as l<contents>e .
     *
     * @return BList object.
     */
    private BencodeList readList() {
        if (readCurrentByte() != 'l')
            throw new Error("Error parsing list. Was expecting a 'l' but got " + readCurrentByte());
        currentByteIndex++; // Skip over the 'l'

        BencodeList list = new BencodeList();
        while (readCurrentByte() != 'e')
            list.add(readSingleType());

        currentByteIndex++;
        return list;
    }

    /**
     * Reads in a bytestring strating at the current position.
     * Throws an error if not possible.
     * A byte string (a sequence of bytes, not necessarily characters) is encoded as <length>:<contents>.
     *
     * @return BByteString
     */
    private BencodeString readByteString()
    {
        String lengthAsString = "";
        int lengthAsInt;
        byte[] bsData;

        byte current = readCurrentByte();
        while (current >= 48 && current <= 57) {
            lengthAsString = lengthAsString + (char) current;
            currentByteIndex++;
            current = readCurrentByte();
        }
        lengthAsInt = Integer.parseInt(lengthAsString);

        if (readCurrentByte() != ':')
            throw new Error("Read length of byte string and was expecting ':' but got " + readCurrentByte());
        currentByteIndex++;

        // Read the actual data
        bsData = new byte[lengthAsInt];
        for (int i = 0; i < lengthAsInt; i++)
        {
            bsData[i] = readCurrentByte();
            currentByteIndex++;
        }

        return new BencodeString(bsData);
    }

    /**
     * Reads in a dictionary. Each dictionary consists of N bytestrings mapped to any other value.
     * Example: d3:foo3:bare == ({foo, bar})
     *
     * @return BDictionary representing the dictionary.
     */
    public BencodeDictionary readDictionary() {
        if (readCurrentByte() != 'd')
            throw new BencodeException("Error parsing dictionary. Was expecting a 'd' but got " + readCurrentByte());
        currentByteIndex++;

        BencodeDictionary dict = new BencodeDictionary();
        while (readCurrentByte() != 'e') {
            BencodeString key = (BencodeString) readSingleType();
            IBencodeObject value = readSingleType();

            dict.add(key, value);
        }
        currentByteIndex++;

        return dict;
    }

    /**
     * Parses an integer in Bencode fromat.
     * Example: 123 == i123e
     *
     * @return BInt representing the value of the parsed integer.
     */
    private BencodeInteger readInteger() {
        if (readCurrentByte() != 'i')
            throw new Error("Error parsing integer. Was expecting an 'i' but got " + readCurrentByte());
        currentByteIndex++;

        String intString = "";
        byte current = readCurrentByte();
        //45 negative mark
        while (current >= 48 && current <= 57 || current == 45) {
            intString = intString + Character.toString((char)current);
            currentByteIndex++;
            current = readCurrentByte();
        }

        if (readCurrentByte() != 'e')
            throw new Error("Error parsing integer. Was expecting 'e' at end but got " + readCurrentByte());

        currentByteIndex++;
        return new BencodeInteger(Long.parseLong(intString));
    }

    /**
     * Returns the byte in the current position of the file.
     *
     * @return byte
     */
    private byte readCurrentByte() {
        return datablob[currentByteIndex];
    }
}
