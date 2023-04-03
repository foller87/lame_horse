package searchengine.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table (name = "`index`")
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(cascade = CascadeType.ALL)
    private Lemma lemma;
    @ManyToOne(cascade = CascadeType.ALL)
    private Page pageSites;
    @Column(name = "`rank`", nullable = false)
    private float rank;
}
