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
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", nullable = false)
    private Lemma lemma;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pages_id", nullable = false)
    private Page page;
    @Column(name = "`rank`", nullable = false)
    private float rank;
}
