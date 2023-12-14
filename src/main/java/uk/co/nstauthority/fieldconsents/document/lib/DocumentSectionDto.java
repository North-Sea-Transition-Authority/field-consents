package uk.co.nstauthority.fieldconsents.document.lib;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface DocumentSectionDto<T extends DocumentSectionDto<T>> {

  UUID id();

  @Nullable
  UUID parentId();

  String title();

  String content();

  int displayOrder();

  List<T> children();

  default List<T> descendants() {
    return children().stream()
        .flatMap(child -> Stream.concat(Stream.of(child), child.descendants().stream()))
        .toList();
  }
}
