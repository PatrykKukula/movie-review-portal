package pl.patrykkukula.MovieReviewPortal.Service;
import pl.patrykkukula.MovieReviewPortal.Dto.CommentDto;
import pl.patrykkukula.MovieReviewPortal.Dto.CommentDtoWithUser;
import java.util.List;

public interface ICommentService {

    Long addComment(CommentDto commentDto);
    void removeComment(Long commentId);
    List<CommentDtoWithUser> fetchAllCommentsForTopic(String sorted, Long topicI);
    List<CommentDtoWithUser> fetchAllCommentsForUser(String username);
    List<CommentDtoWithUser> fetchAllComments(String sorted);
    void updateComment(Long commentId, CommentDto commentDto);
}
