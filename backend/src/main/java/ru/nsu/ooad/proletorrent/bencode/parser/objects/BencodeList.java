package ru.nsu.ooad.proletorrent.bencode.parser.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BencodeList implements IBencodeObject{

    private static final char PREFIX = 'l';

    public byte[] blob;
    private final List<IBencodeObject> list;

    public BencodeList(){
        this.list = new LinkedList<IBencodeObject>();
    }

    public Iterator<IBencodeObject> getIterator(){
        return list.iterator();
    }

    public void add(IBencodeObject obj) {
        this.list.add(obj);
    }

    @Override
    public byte[] bencode() {
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        bytes.add((byte)PREFIX);
        for (IBencodeObject entry : this.list)
            for (byte b : entry.bencode()) bytes.add(b);
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

        for (IBencodeObject entry : this.list)
            sb.append(entry.bencodedString());

        sb.append(POSTFIX);

        return sb.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Object entry : this.list) {
            sb.append(entry.toString());
        }
        sb.append(") ");

        return sb.toString();
    }

}
