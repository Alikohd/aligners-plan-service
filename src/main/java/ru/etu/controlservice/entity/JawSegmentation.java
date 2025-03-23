package ru.etu.controlservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "jaw_segmentation")
public class JawSegmentation extends BaseTreatmentStep {
    @Column(name = "jaw_upper_stl")
    private String jawUpperStl;

    @Column(name = "jaw_lower_stl")
    private String jawLowerStl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "jaws_json")
    private List<String> jawsJson;

}
