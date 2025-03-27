package pl.patrykkukula.MovieReviewPortal.Dto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDtoWithUser {
    @NotEmpty(message = "Text cannot be null or empty")
    @Size(max = 255, message = "Title must not exceed 2048 characters")
    private String text;
    @Positive(message = "Topic ID must be greater than 0")
    @ToString.Exclude
    private Long topicId;
    private Long commentIdInPost;
    private String user;
}
