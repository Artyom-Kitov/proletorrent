package ru.nsu.ooad.proletorrent.bencode.parser.objects;

import lombok.Getter;
import ru.nsu.ooad.proletorrent.bencode.parser.Utils;

import java.nio.charset.Charset;
import java.util.Arrays;

public class BencodeString implements IBencodeObject{

    @Getter
    private final byte[] data;

    public BencodeString(byte[] data){
        this.data = data;
    }

    public BencodeString(String name){
        this.data = name.getBytes();
    }

    @Override
    public byte[] bencode() {
        byte[] lengthStringAsBytes = Utils.stringToAsciiBytes(Long.toString(data.length));
        byte[] bencoded = new byte[lengthStringAsBytes.length + 1 + data.length];

        bencoded[lengthStringAsBytes.length] = ':';
        System.arraycopy(lengthStringAsBytes, 0, bencoded, 0, lengthStringAsBytes.length);
        for (int i = 0; i < data.length; i++)
            bencoded[i + lengthStringAsBytes.length + 1] = data[i];

        return bencoded;
    }

    @Override
    public String bencodedString() {
        return data.length + ":" + new String(data);
    }

    @Override
    public String toString()
    {
        return new String(data, Charset.forName("UTF-8"));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BencodeString that = (BencodeString) o;

        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode()
    {
        return data != null ? Arrays.hashCode(data) : 0;
    }
}
