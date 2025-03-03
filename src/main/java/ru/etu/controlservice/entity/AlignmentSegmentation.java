package ru.etu.controlservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "alignment_segmentation")
public class AlignmentSegmentation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ct_segmentation_id", nullable = false)
    private CtSegmentation ctSegmentation;

    @OneToOne
    @JoinColumn(name = "jaw_segmentation_id", nullable = false)
    private JawSegmentation jawSegmentation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "init_teeth_marices")
    private List<String> initTeethMatrices;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stl_tooth_refs")
    private List<String> stlToothRefs;

    @OneToOne
    @JoinColumn(name = "node_id", unique = true)
    private Node node;
}
