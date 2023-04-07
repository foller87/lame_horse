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
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false, foreignKey = @ForeignKey(name = "site_lemma_FK"),
    columnDefinition = "Integer")
    private Site site;
    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String lemma;
    @Column(nullable = false)
    private int frequency;
    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
    private List<Index> indexes;
}
