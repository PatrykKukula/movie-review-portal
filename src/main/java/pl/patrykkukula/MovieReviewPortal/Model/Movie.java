package pl.patrykkukula.MovieReviewPortal.Model;

import jakarta.persistence.*;
import lombok.*;
import pl.patrykkukula.MovieReviewPortal.Constants.MovieCategory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Movie extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movieId;
    @Column(nullable = false)
    private String title;
    private String description;
    @Column(nullable = false)
    private LocalDate releaseDate;
    @Enumerated(EnumType.STRING)
    private MovieCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    private Director director;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "actors_and_movies",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    private List<Actor> actors = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Topic> topics = new ArrayList<>();

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MovieRate> movieRates = new ArrayList<>();
}
