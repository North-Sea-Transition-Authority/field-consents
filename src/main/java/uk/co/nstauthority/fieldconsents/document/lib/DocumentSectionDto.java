package uk.co.nstauthority.fieldconsents.document.lib;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface DocumentSectionDto<T extends DocumentSectionDto<T>> {

  UUID id();

  @Nullable
  UUID parentId();

  String title();

  String content();

  int displayOrder();

  List<T> children();
}
