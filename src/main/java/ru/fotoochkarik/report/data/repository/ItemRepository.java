package ru.fotoochkarik.report.data.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fotoochkarik.report.data.model.Item;

public interface ItemRepository extends JpaRepository<Item, UUID> {

}
