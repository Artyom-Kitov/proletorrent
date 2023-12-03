package ru.nsu.ooad.proletorrent.torrent;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.util.Pair;
import ru.nsu.ooad.proletorrent.bencode.parser.Utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
public class Piece {

    private final int index;
    private final int size;
    private final String expectedHash;
    private final int[] partsSizes;

    private final byte[] partsDownloaded;
    private int downloadedPartsCount = 0;

    private byte[] data;

    @Setter
    private Set<Peer> peers;

    @Builder
    public Piece(int index, int size, Set<Peer> peers, int partSize, String expectedHash) {
        this.index = index;
        this.size = size;
        this.expectedHash = expectedHash;

        int partsCount = size / partSize;
        int actualPartsCount = partsCount + (size % partSize == 0 ? 0 : 1);
        partsSizes = new int[actualPartsCount];
        partsDownloaded = new byte[actualPartsCount];
        for (int i = 0; i < partsCount; i++) {
            partsSizes[i] = partSize;
        }
        if (size % partSize != 0) {
            partsSizes[partsCount] = size - partsCount * partSize;
        }
        this.peers = new HashSet<>();
        this.peers.addAll(peers);
    }

    public void writePart(byte[] bytes, int offset) {
        if (data == null) {
            data = new byte[size];
        }
        int partIndex = offset / partsSizes[0];
        if (partsDownloaded[partIndex] == 2) {
            return;
        }
        System.arraycopy(bytes, 8, data, offset, bytes.length - 8);
        partsDownloaded[partIndex] = 2;
        downloadedPartsCount++;
    }

    public void addPeers(Set<Peer> peers) {
        this.peers.addAll(peers);
    }

    public boolean isComplete() {
        return downloadedPartsCount == partsSizes.length;
    }

    // offset, length
    public Pair<Integer, Integer> getEmptyPart() {
        if (isComplete()) {
            return null;
        }
        int part = 0;
        while (part < partsDownloaded.length && partsDownloaded[part] != 0) {
            part++;
        }
        if (part == partsDownloaded.length) {
            boolean available = false;
            for (int i = partsDownloaded.length - 1; i >= 0; i--) {
                if (partsDownloaded[i] == 1) {
                    partsDownloaded[i] = 0;
                    available = true;
                    part = i;
                }
            }
            if (!available) {
                return null;
            }
        }
        partsDownloaded[part] = 1;
        return Pair.of(part * partsSizes[0], partsSizes[part]);
    }

    public boolean isValidHash() {
        String hash = Utils.SHAsum(data);
        if (hash == null) {
            return false;
        }
        return Objects.equals(expectedHash, hash.toUpperCase());
    }

}
