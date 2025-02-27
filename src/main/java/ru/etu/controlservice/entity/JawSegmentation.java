package ru.etu.controlservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Getter
@Setter
public class JawSegmentation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String jawUpperStl;
    private String jawLowerStl;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> jawsJson;

    @OneToOne
    @JoinColumn(name = "node_id", unique = true)
    private Node node;
}
