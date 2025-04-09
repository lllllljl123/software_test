package com.demo.controller.user;

import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUserID("testuser");
        user.setPassword("123456");
        user.setIsadmin(0);
    }

    @Test
    void testLogin_user_shouldReturnIndex() throws Exception {
        Mockito.when(userService.checkLogin("testuser", "123456")).thenReturn(user);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "testuser")
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("/index"));
    }

    @Test
    void testLogin_admin_shouldReturnAdminIndex() throws Exception {
        user.setIsadmin(1);
        Mockito.when(userService.checkLogin("admin", "adminpass")).thenReturn(user);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "admin")
                        .param("password", "adminpass"))
                .andExpect(status().isOk())
                .andExpect(content().string("/admin_index"));
    }

    @Test
    void testLogin_invalid_shouldReturnFalse() throws Exception {
        Mockito.when(userService.checkLogin(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/loginCheck.do")
                        .param("userID", "wrong")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testLogout_shouldRedirectToIndex() throws Exception {
        mockMvc.perform(get("/logout.do"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    void testRegister_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/register.do")
                        .param("userID", "test")
                        .param("userName", "test name")
                        .param("password", "pass")
                        .param("email", "test@example.com")
                        .param("phone", "123456789"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));
    }

    @Test
    void testUpdateUser_shouldRedirectToUserInfo() throws Exception {
        MockMultipartFile mockPicture = new MockMultipartFile("picture", "", "image/jpeg", new byte[0]);
        Mockito.when(userService.findByUserID("testuser")).thenReturn(user);

        mockMvc.perform(multipart("/updateUser.do")
                        .file(mockPicture)
                        .param("userID", "testuser")
                        .param("userName", "newName")
                        .param("passwordNew", "newpass")
                        .param("email", "new@example.com")
                        .param("phone", "111111111"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));
    }

    @Test
    void testCheckPassword_shouldReturnTrue() throws Exception {
        user.setPassword("secret");
        Mockito.when(userService.findByUserID("test")).thenReturn(user);

        mockMvc.perform(get("/checkPassword.do")
                        .param("userID", "test")
                        .param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testCheckPassword_shouldReturnFalse() throws Exception {
        user.setPassword("secret");
        Mockito.when(userService.findByUserID("test")).thenReturn(user);

        mockMvc.perform(get("/checkPassword.do")
                        .param("userID", "test")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
