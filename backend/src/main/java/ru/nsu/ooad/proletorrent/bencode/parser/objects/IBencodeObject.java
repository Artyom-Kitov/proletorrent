package ru.nsu.ooad.proletorrent.bencode.parser.objects;

public interface IBencodeObject {

    public final char POSTFIX = 'e';

    /**
     * Returns the byte representation of the bencoded object.
     * @return byte representation of the bencoded object.
     */
    byte[] bencode();

    /**
     * Returns string representation of bencode object.
     * @return string representation of bencode object.
     */
    String bencodedString();

}
