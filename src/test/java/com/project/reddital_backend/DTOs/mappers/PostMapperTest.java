package com.project.reddital_backend.DTOs.mappers;

import com.project.reddital_backend.DTOs.models.PostDto;
import com.project.reddital_backend.controllers.requests.PostingRequest;
import com.project.reddital_backend.models.Post;
import com.project.reddital_backend.models.SubReddit;
import com.project.reddital_backend.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class PostMapperTest {

    // ------------------------------------------------------- properties -------------------------------------------------------

    final String title      = "i'm new here!";
    final String content    = "hello! just registered!";
    final Date time         = new Date();

    final User user = User.builder().username("a").password("123456").email("a@a.a").build();
    final SubReddit sub = SubReddit.builder().name("r/askTal").build();

    // ------------------------------------------------------- tests -------------------------------------------------------

    @Test
    @DisplayName("test toPostDto")
    public void toPostDto() {
        // Run the test
        Post post = Post.builder()
                .title(title)
                .content(content)
                .time(time)
                .user(user)
                .subreddit(sub)
                .build();

        final PostDto result = PostMapper.toPostDto(post);

        assertEquals(title, result.getTitle());
        assertEquals(content, result.getContent());
        assertEquals(time.getTime(), result.getTime());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(sub.getName(), result.getSubReddit());
    }

    @Test
    @DisplayName("test toPostDto")
    public void toPostDto_request() {
        // Run the test
        PostingRequest request = PostingRequest.builder()
                .title(title)
                .content(content)
                .subReddit(sub.getName())
                .authenticationKey("TODO") //TODO
                .build();

        final PostDto result = PostMapper.toPostDto(request);

        assertEquals(title, result.getTitle());
        assertEquals(content, result.getContent());
        assertEquals("tal", result.getUsername());
        assertEquals(sub.getName(), result.getSubReddit());
    }
}
