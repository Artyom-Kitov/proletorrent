package ru.nsu.ooad.proletorrent.torrent;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class PieceQueue {

    private static final int PART_SIZE = 16384;

    private final List<String> hashes;
    private final int pieceSize;
    private final int piecesCount;
    private final Piece[] pieces;
    private final Queue<Piece> queue;
    private final boolean[] downloaded;

    public PieceQueue(List<String> hashes, int pieceSize) {
        this.hashes = hashes;
        this.pieceSize = pieceSize;
        piecesCount = hashes.size();
        pieces = new Piece[piecesCount];
        queue = new ArrayDeque<>();
        downloaded = new boolean[piecesCount];
    }

    public void addPiece(int index, Set<Peer> peers) {
        if (pieces[index] != null) {
            pieces[index].addPeers(peers);
            return;
        }
        pieces[index] = Piece.builder()
                .index(index)
                .size(pieceSize)
                .peers(peers)
                .expectedHash(hashes.get(index))
                .partSize(PART_SIZE)
                .build();
        queue.add(pieces[index]);
    }

    public void addPiecesByMask(byte[] mask, Peer peer) {
        for (int i = 0; i < piecesCount; i++) {
            boolean isPresent = (mask[i / 8] & (1 << (i % 8))) != 0;
            if (isPresent) {
                addPiece(i, Set.of(peer));
            }
        }
    }

    public Piece getPendingPiece() {
        return queue.peek();
    }

    public Piece remove() {
        Piece p = queue.poll();
        if (p == null) {
            return null;
        }
        downloaded[p.getIndex()] = true;
        return p;
    }

    public int size() {
        return queue.size();
    }

    public boolean isDownloaded() {
        for (boolean b : downloaded) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

}
