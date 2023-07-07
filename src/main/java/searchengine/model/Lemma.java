package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "lemma", uniqueConstraints = {@UniqueConstraint(name = "lemma_by_site_id_unique",
        columnNames = {"lemma", "site_id"})})
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false, foreignKey = @ForeignKey(name = "site_lemma_FK"))
    private Site site;
    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String lemma;
    @Column(nullable = false)
    private int frequency;
    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
    private List<Index> indexes;

    @Override
    public String toString() {
        return lemma + ", frequency - " + frequency + " id - " + id;
    }
}
