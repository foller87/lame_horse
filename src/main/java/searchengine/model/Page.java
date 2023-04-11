package searchengine.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "pages", uniqueConstraints = {
        @UniqueConstraint(name = "pages_path_unique", columnNames = "path")
})
public class Page implements Serializable {
    @Id
    @GeneratedValue(generator = "page_generator_id")
    @GenericGenerator(name = "page_generator_id", strategy = "increment")
    private Long id;
//    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Site.class, cascade = CascadeType.REMOVE, optional = false)
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    @JoinColumn(foreignKey = @ForeignKey(name = "site_page_FK"), columnDefinition = "Integer",
//            referencedColumnName = "id", nullable = false , name = "site_id", updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    @Column(columnDefinition = "TEXT NOT NULL, UNIQUE KEY path_index Index(path(512))")
    private String path;
    @Column(nullable = false)
    private int code;
    @Column(length = 16777215, columnDefinition = "mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
    private String content;
    @OneToMany (mappedBy = "page", cascade = CascadeType.ALL)
    private List<Index> indexes;
}
