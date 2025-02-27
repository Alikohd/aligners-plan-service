package ru.etu.controlservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Getter
@Setter
public class AlignmentSegmentation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ct_segmentation_id", nullable = false)
    private CtSegmentation ctSegmentation;

    @ManyToOne
    @JoinColumn(name = "jaw_segmentation_id", nullable = false)
    private JawSegmentation jawSegmentation;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> initTeethMatrices;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> stlToothRefs;

    @OneToOne
    @JoinColumn(name = "node_id", unique = true)
    private Node node;
}
