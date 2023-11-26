package ru.nsu.ooad.proletorrent.bencode.torrent;

import ru.nsu.ooad.proletorrent.bencode.BencodeException;
import ru.nsu.ooad.proletorrent.bencode.parser.Reader;
import ru.nsu.ooad.proletorrent.bencode.parser.Utils;
import ru.nsu.ooad.proletorrent.bencode.parser.objects.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

public class TorrentParser {
    public static TorrentInfo parseTorrent(InputStream input) throws IOException {
        Reader r = new Reader(input);
        return parseTorrent(r);
    }

    public static TorrentInfo parseTorrent(String filePath) throws IOException
    {
        Reader r = new Reader(new File(filePath));
        return parseTorrent(r);
    }

    public static TorrentInfo parseTorrent(Reader r) throws IOException
    {
        List<IBencodeObject> x = r.read();
        if (x.size() != 1)
            throw new Error("Parsing .torrent yielded wrong number of bencoding structs.");
        try
        {
            return parseTorrent(x.get(0));
        } catch (ParseException e)
        {
            System.err.println("Error parsing torrent!");
        }
        return null;
    }

    private static TorrentInfo parseTorrent(Object o) throws ParseException
    {
        if (o instanceof BencodeDictionary)
        {
            BencodeDictionary torrentDictionary = (BencodeDictionary) o;
            BencodeDictionary infoDictionary = parseInfoDictionary(torrentDictionary);

            TorrentInfo t = new TorrentInfo();

            t.setAnnounce(parseAnnounce(torrentDictionary));
            t.setInfoHash(Utils.SHAsum(infoDictionary.bencode()));
            t.setName(parseTorrentLocation(infoDictionary));
            t.setPieceLength( parsePieceLength(infoDictionary));
            t.setPieces(parsePiecesHashes(infoDictionary));
            t.setPiecesBlob(parsePiecesBlob(infoDictionary));

            t.setFileList(parseFileList(infoDictionary));
            t.setCommentary(parseComment(torrentDictionary));
            t.setAuthor(parseCreatorName(torrentDictionary));
            t.setDateOfCreation(parseCreationDate(torrentDictionary));
            t.setAnnounceList(parseAnnounceList(torrentDictionary));
            t.setTotalSize(parseSingleFileTotalSize(infoDictionary));

            t.setSingleFileTorrent(null != infoDictionary.find(new BencodeString("length")));
            return t;
        } else
        {
            throw new ParseException("Could not parse Object to BencodeDictionary", 0);
        }
    }

    /**
     * @param info info dictionary
     * @return length — size of the file in bytes (only when one file is being shared)
     */
    private static Long parseSingleFileTotalSize(BencodeDictionary info)
    {
        if (null != info.find(new BencodeString("length")))
            return ((BencodeInteger) info.find(new BencodeString("length"))).getValue();
        return null;
    }

    /**
     * @param dictionary root dictionary of torrent
     * @return info — this maps to a dictionary whose keys are dependent on whether
     * one or more files are being shared.
     */
    private static BencodeDictionary parseInfoDictionary(BencodeDictionary dictionary)
    {
        if (null != dictionary.find(new BencodeString("info")))
            return (BencodeDictionary) dictionary.find(new BencodeString("info"));
        else
            return null;
    }

    /**
     * @param dictionary root dictionary of torrent
     * @return creation date: (optional) the creation time of the torrent, in standard UNIX epoch format
     * (integer, seconds since 1-Jan-1970 00:00:00 UTC)
     */
    private static Date parseCreationDate(BencodeDictionary dictionary)
    {
        if (null != dictionary.find(new BencodeString("creation date"))) {
            BencodeInteger date = (BencodeInteger) dictionary.find(new BencodeString("creation date"));
            return new Date(date.getValue() * 1000);
        }
        return null;
    }

    /**
     * @param dictionary root dictionary of torrent
     * @return created by: (optional) name and version of the program used to create the .torrent (string)
     */
    private static String parseCreatorName(BencodeDictionary dictionary)
    {
        if (null != dictionary.find(new BencodeString("created by")))
            return dictionary.find(new BencodeString("created by")).toString();
        return null;
    }

    /**
     * @param dictionary root dictionary of torrent
     * @return comment: (optional) free-form textual comments of the author (string)
     */
    private static String parseComment(BencodeDictionary dictionary)
    {
        if (null != dictionary.find(new BencodeString("comment")))
            return dictionary.find(new BencodeString("comment")).toString();
        else
            return null;
    }

    /**
     * @param info infodictionary of torrent
     * @return piece length — number of bytes per piece. This is commonly 28 KiB = 256 KiB = 262,144 B.
     */
    private static Long parsePieceLength(BencodeDictionary info)
    {
        if (null != info.find(new BencodeString("piece length")))
            return ((BencodeInteger) info.find(new BencodeString("piece length"))).getValue();
        else
            return null;
    }

    /**
     * @param info info dictionary of torrent
     * @return name — suggested filename where the file is to be saved (if one file)/suggested directory name
     * where the files are to be saved (if multiple files)
     */
    private static String parseTorrentLocation(BencodeDictionary info)
    {
        if (null != info.find(new BencodeString("name")))
            return info.find(new BencodeString("name")).toString();
        else
            return null;
    }

    /**
     * @param dictionary root dictionary of torrent
     * @return announce — the URL of the tracke
     */
    private static String parseAnnounce(BencodeDictionary dictionary)
    {
        if (null != dictionary.find(new BencodeString("announce")))
            return dictionary.find(new BencodeString("announce")).toString();
        else
            return null;
    }

    /**
     * @param info info dictionary of .torrent file.
     * @return pieces — a hash list, i.e., a concatenation of each piece's SHA-1 hash. As SHA-1 returns a 160-bit hash,
     * pieces will be a string whose length is a multiple of 160-bits.
     */
    private static byte[] parsePiecesBlob(BencodeDictionary info)
    {
        if (null != info.find(new BencodeString("pieces")))
        {
            return ((BencodeString) info.find(new BencodeString("pieces"))).getData();
        } else
        {
            throw new BencodeException("Info dictionary does not contain pieces bytestring!");
        }
    }

    /**
     * @param info info dictionary of .torrent file.
     * @return pieces — a hash list, i.e., a concatenation of each piece's SHA-1 hash. As SHA-1 returns a 160-bit hash,
     * pieces will be a string whose length is a multiple of 160-bits.
     */
    private static List<String> parsePiecesHashes(BencodeDictionary info)
    {
        if (null != info.find(new BencodeString("pieces")))
        {
            List<String> sha1HexRenders = new ArrayList<String>();
            byte[] piecesBlob = ((BencodeString) info.find(new BencodeString("pieces"))).getData();
            // Split the piecesData into multiple hashes. 1 hash = 20 bytes.
            if (piecesBlob.length % 20 == 0)
            {
                int hashCount = piecesBlob.length / 20;
                for (int currHash = 0; currHash < hashCount; currHash++)
                {
                    byte[] currHashByteBlob = Arrays.copyOfRange(piecesBlob, 20 * currHash, (20 * (currHash + 1)));
                    String sha1 = Utils.bytesToHex(currHashByteBlob);
                    sha1HexRenders.add(sha1);
                }
            } else
            {
                throw new BencodeException("Error parsing SHA1 piece hashes. Bytecount was not a multiple of 20.");
            }
            return sha1HexRenders;
        } else
        {
            throw new BencodeException("Info dictionary does not contain pieces bytestring!");
        }
    }

    /**
     * @param info info dictionary of torrent
     * @return files — a list of dictionaries each corresponding to a file (only when multiple files are being shared).
     */
    private static List<TorrentFile> parseFileList(BencodeDictionary info)
    {
        if (null != info.find(new BencodeString("files")))
        {
            List<TorrentFile> fileList = new ArrayList<TorrentFile>();
            BencodeList filesBList = (BencodeList) info.find(new BencodeString("files"));

            Iterator<IBencodeObject> fileBDicts = filesBList.getIterator();
            while (fileBDicts.hasNext())
            {
                Object fileObject = fileBDicts.next();
                if (fileObject instanceof BencodeDictionary)
                {
                    BencodeDictionary fileBDict = (BencodeDictionary) fileObject;
                    BencodeList filePaths = (BencodeList) fileBDict.find(new BencodeString("path"));
                    BencodeInteger fileLength = (BencodeInteger) fileBDict.find(new BencodeString("length"));
                    // Pick out each subdirectory as a string.
                    List<String> paths = new LinkedList<String>();
                    Iterator<IBencodeObject> filePathsIterator = filePaths.getIterator();
                    while (filePathsIterator.hasNext())
                        paths.add(filePathsIterator.next().toString());

                    fileList.add(new TorrentFile(fileLength.getValue(), paths));
                }
            }
            return fileList;
        }
        return null;
    }

    /**
     * @param dictionary root dictionary of torrent
     * @return announce-list: (optional) this is an extention to the official specification, offering
     * backwards-compatibility. (list of lists of strings).
     */
    private static List<String> parseAnnounceList(BencodeDictionary dictionary)
    {
        if (null != dictionary.find(new BencodeString("announce-list")))
        {
            List<String> announceUrls = new LinkedList<String>();

            BencodeList announceList = (BencodeList) dictionary.find(new BencodeString("announce-list"));
            Iterator<IBencodeObject> subLists = announceList.getIterator();
            while (subLists.hasNext())
            {
                BencodeList subList = (BencodeList) subLists.next();
                Iterator<IBencodeObject> elements = subList.getIterator();
                while (elements.hasNext())
                {
                    // Assume that each element is a BencodeString
                    BencodeString tracker = (BencodeString) elements.next();
                    announceUrls.add(tracker.toString());
                }
            }
            return announceUrls;
        } else
        {
            return null;
        }
    }
}
