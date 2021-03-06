package com.project.reddital_backend.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.project.reddital_backend.DTOs.mappers.UserMapper;
import com.project.reddital_backend.DTOs.models.UserDto;
import com.project.reddital_backend.controllers.requests.LoginRequest;
import com.project.reddital_backend.controllers.requests.Request;
import com.project.reddital_backend.controllers.requests.SignupRequest;
import com.project.reddital_backend.exceptions.DuplicateEntityException;
import com.project.reddital_backend.exceptions.EntityNotFoundException;
import com.project.reddital_backend.exceptions.UnauthorizedException;
import com.project.reddital_backend.services.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    // ----------------------------------------------------- fields -----------------------------------------------------

    @MockBean
    private UserService mockUserService;

    @MockBean
    private UserMapper userMapper;

    @Autowired
    private MockMvc mockMvc;


    private ObjectMapper objectMapper;

    private final String USER_PATH  = "/user";

    private final long      id         = 42L;
    private final String    username   = "Sportalcraft";
    private final String    email      = "test@gmail.com";
    private final String    password   = "12345678";


    // ------------------------------------------------------- preparations -------------------------------------------------------

    @BeforeEach
    public void setUp() {
        openMocks(this);

        objectMapper = new ObjectMapper();
    }

    @AfterEach
    public void tearDown() {
        mockUserService = null;
        objectMapper = null;
    }

    // ----------------------------------------------------- tests -----------------------------------------------------


    @Test
    @DisplayName("test signup with good parameters")
    public void signup_good() throws Exception {
        Mockito.when(mockUserService.signup(any())).thenAnswer((Answer<UserDto>) invocation -> {
            Object[] args = invocation.getArguments();
            return (UserDto) args[0];
        });

        Mockito.when(userMapper.toUserDto((SignupRequest) any()))
                .thenReturn(UserDto.builder().username(username).email(email).build());

        Request request = SignupRequest.builder()
                        .username(username)
                        .email(email)
                        .password(password)
                .build();

        ResultActions result = post(USER_PATH + "/signup", requestAsString(request));
        String body = result.andReturn().getResponse().getContentAsString();

        result.andExpect(status().isCreated());

        checkProperty(username, "$.username", body);
        checkProperty(email, "$.email", body);

        try {
            checkProperty("", "$.password", body);
        } catch (PathNotFoundException ignored) {}

    }

    @Test
    @DisplayName("test signup with an existing username")
    public void signup_duplicate() throws Exception {
        Mockito.when(mockUserService.signup(any()))
                .thenThrow(new DuplicateEntityException("EXIST"));

        Request request = SignupRequest.builder()
                .username(username)
                .email(email)
                .password(password)
                .build();

        ResultActions result = post(USER_PATH + "/signup", requestAsString(request));
        String body = result.andReturn().getResponse().getContentAsString();

        result.andExpect(status().isUnprocessableEntity());

        checkProperty("EXIST", "$", body);

        checkNonProperty( "$.username", body);
    }

    @Test
    @DisplayName("test signup with missing username")
    public void signup_missingUsername() throws Exception {
        Mockito.when(mockUserService.signup(any())).thenAnswer((Answer<UserDto>) invocation -> {
            Object[] args = invocation.getArguments();
            return (UserDto) args[0];
        });

        ResultActions result = post(USER_PATH + "/signup", String.format("{\"email\": \"%s\", \"password\":\"%s\"}", email, password));
        String body = result.andReturn().getResponse().getContentAsString();

        result.andExpect(status().isBadRequest());

        checkNonProperty( "$.username", body);
    }






    @Test
    @DisplayName("test login with good info")
    public void login_good() throws Exception {
        Mockito.when(mockUserService.login(anyString(), anyString())).thenReturn(
                UserDto.builder()
                        .id(id)
                        .username(username)
                        .email(email)
                        .build()
        );

        Request request = LoginRequest.builder()
                .username(username)
                .password(password)
                .build();

        ResultActions result = post(USER_PATH + "/login", requestAsString(request));
        String body = result.andReturn().getResponse().getContentAsString();

        result.andExpect(status().isOk());

        checkProperty("" + id, "$.id", body);
        checkProperty(username, "$.username", body);
        checkProperty(email, "$.email", body);

        try {
            checkProperty("", "$.password", body);
        } catch (PathNotFoundException ignored) {}
    }

    @Test
    @DisplayName("test login with bad username")
    public void login_badUserName() throws Exception {
        Mockito.when(mockUserService.login(anyString(), anyString())).thenReturn(null);

        Request request = LoginRequest.builder()
                .username("")
                .password(password)
                .build();

        ResultActions result = post(USER_PATH + "/login", requestAsString(request));
        String body = result.andReturn().getResponse().getContentAsString();

        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("test login with wrong credentials")
    public void login_wrongCredentials() throws Exception {
        Mockito.when(mockUserService.login(anyString(), anyString())).thenThrow(UnauthorizedException.class);

        Request request = LoginRequest.builder()
                .username(username)
                .password(password + "!")
                .build();

        ResultActions result = post(USER_PATH + "/login", requestAsString(request));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("test login with non existing user")
    public void login_nonExistUser() throws Exception {
        Mockito.when(mockUserService.login(anyString(), anyString())).thenThrow(EntityNotFoundException.class);

        Request request = LoginRequest.builder()
                .username("fiovhrouvhrouvlsfn")
                .password(password )
                .build();

        ResultActions result = post(USER_PATH + "/login", requestAsString(request));
        result.andExpect(status().isNotFound());
    }






    // ----------------------------------------------------- private methods -----------------------------------------------------

    private void checkProperty(String expected, String jsonPath, String body){
        assertEquals(expected, "" + JsonPath.read(body, jsonPath));
    }

    private void checkNonProperty(String jsonPath, String body){
        try {
            JsonPath.read(body, jsonPath);
        } catch (PathNotFoundException ignored) {
            return;
        }

        Assertions.fail("the property " + jsonPath + " exist in " + body + ", but it shouldn't");
    }



    private ResultActions post(String uri, String content) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        );
    }

    private ResultActions get(String uri, String content) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        );
    }

    private String requestAsString(Request request) throws JsonProcessingException {
        return objectMapper.writeValueAsString(request);
    }

}
