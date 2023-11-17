package ru.nsu.ooad.proletorrent.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder
@Getter
public class TorrentFileTreeNode {

    private final String name;
    private long size;
    private final List<TorrentFileTreeNode> children = new ArrayList<>();

    public void addFile(List<String> path, long size) {
        if (path.isEmpty()) {
            return;
        }
        Optional<TorrentFileTreeNode> folder = children.stream()
                .filter(file -> file.getName().equals(path.get(0)))
                .findAny();
        TorrentFileTreeNode node;
        if (folder.isEmpty()) {
            node = TorrentFileTreeNode.builder()
                    .name(path.get(0))
                    .size(size)
                    .build();
            children.add(node);
        } else {
            node = folder.get();
            node.size += size;
        }
        node.addFile(path.subList(1, path.size()), size);
    }

}
