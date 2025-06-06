package ru.etu.controlservice.entity;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "file")
public class File {
    @Id
    private UUID id;

    @Column(name = "uri")
    private String uri;

    @Column(name = "storage_type")
    @Enumerated(EnumType.STRING)
    private StorageType storageType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static File fromS3(String uri) {
        File file = new File();
        file.setUri(uri);
        file.setStorageType(StorageType.S3);
        return file;
    }

    public static File fromPacs(String uri) {
        File file = new File();
        file.setUri(uri);
        file.setStorageType(StorageType.PACS);
        return file;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = Generators.timeBasedEpochGenerator().generate(); // UUID v7 generation
        }
    }
}
