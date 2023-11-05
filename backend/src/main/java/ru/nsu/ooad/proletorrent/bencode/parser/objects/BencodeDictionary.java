package ru.nsu.ooad.proletorrent.bencode.parser.objects;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class BencodeDictionary implements IBencodeObject{

    private static final char PREFIX = 'd';

    private final Map<BencodeString, IBencodeObject> dictionary;
    public byte[] blob;

    public BencodeDictionary(){
        this.dictionary = new LinkedHashMap<>();
    }

    public void add(BencodeString key, IBencodeObject value){
        this.dictionary.put(key, value);
    }

    public IBencodeObject find(BencodeString key){
        return this.dictionary.get(key);
    }

    @Override
    public byte[] bencode() {
        // Get the total size of the keys and values.
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        bytes.add((byte)PREFIX);

        for (Map.Entry<BencodeString, IBencodeObject> entry : this.dictionary.entrySet()) {
            byte[] keyBencoded = entry.getKey().bencode();
            byte[] valBencoded = entry.getValue().bencode();
            for (byte b : keyBencoded)
                bytes.add(b);
            for (byte b : valBencoded)
                bytes.add(b);
        }
        bytes.add((byte)POSTFIX);
        byte[] bencoded = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++)
            bencoded[i] = bytes.get(i);

        return bencoded;
    }

    @Override
    public String bencodedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX);
        for (Map.Entry<BencodeString, IBencodeObject> entry : this.dictionary.entrySet()) {
            sb.append(entry.getKey().bencodedString());
            sb.append(entry.getValue().bencodedString());
        }
        sb.append(POSTFIX);
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n[\n");
        for (Map.Entry<BencodeString, IBencodeObject> entry : this.dictionary.entrySet())
        {
            sb.append(entry.getKey()).append(" :: ").append(entry.getValue()).append("\n");
        }
        sb.append("]");

        return sb.toString();
    }
}
