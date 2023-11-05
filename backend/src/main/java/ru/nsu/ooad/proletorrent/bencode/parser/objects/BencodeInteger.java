package ru.nsu.ooad.proletorrent.bencode.parser.objects;

import lombok.Getter;
import ru.nsu.ooad.proletorrent.bencode.parser.Utils;

import java.util.ArrayList;

public class BencodeInteger implements IBencodeObject{

    private static final char PREFIX = 'i';

    @Getter
    private final Long value;
    public byte[] blob;

    public BencodeInteger(Long value){
        this.value = value;
    }


    @Override
    public byte[] bencode() {
        byte[] sizeInAsciiBytes = Utils.stringToAsciiBytes(value.toString());

        ArrayList<Byte> bytes = new ArrayList<Byte>();

        bytes.add((byte)PREFIX);

        for (byte sizeByte : sizeInAsciiBytes)
            bytes.add(sizeByte);

        bytes.add((byte)POSTFIX);

        byte[] bencoded = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++)
            bencoded[i] = bytes.get(i);

        return bencoded;
    }

    @Override
    public String bencodedString() {
        return Character.toString(PREFIX) + value + Character.toString(POSTFIX);
    }
}
