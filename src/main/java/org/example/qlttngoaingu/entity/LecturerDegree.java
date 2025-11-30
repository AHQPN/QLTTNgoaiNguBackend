package org.example.qlttngoaingu.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "bangcap")
@Data
public class LecturerDegree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma; // ma INT PRIMARY KEY

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magv", nullable = false, foreignKey = @ForeignKey(name = "fk_bangcap_giangvien"))
    private Lecturer lecturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maloai", nullable = false, foreignKey = @ForeignKey(name = "fk_bangcap_loaibangcap"))
    private Degree degree;

    @Column(name = "trinhdo", length = 255)
    private String level;
}
