package searchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table (name = "indexes")
public class Indexx {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(cascade = CascadeType.ALL)
    private Lemma lemma;
//    @ManyToOne(cascade = CascadeType.ALL)
//    private PageSite pageSites;
    @Column(name = "count_lemmas", nullable = false)
    private float countLemmas;
}
