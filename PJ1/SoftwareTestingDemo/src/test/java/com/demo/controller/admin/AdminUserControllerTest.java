package com.demo.controller.admin;

import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    public void setup() {
        sampleUser = new User();
        sampleUser.setId(1);
        sampleUser.setUserID("u001");
        sampleUser.setUserName("Alice");
        sampleUser.setPassword("pass123");
        sampleUser.setEmail("alice@example.com");
        sampleUser.setPhone("123456789");
        sampleUser.setPicture("");
    }

    @Test
    public void testUserManagePage() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<User> page = new PageImpl<>(Collections.singletonList(sampleUser), pageable, 1);
        Mockito.when(userService.findByUserID(pageable)).thenReturn(page);

        mockMvc.perform(get("/user_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_manage"))
                .andExpect(model().attribute("total", 1));
    }

    @Test
    public void testUserAddPage() throws Exception {
        mockMvc.perform(get("/user_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_add"));
    }

    @Test
    public void testUserList() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<User> page = new PageImpl<>(Collections.singletonList(sampleUser), pageable, 1);
        Mockito.when(userService.findByUserID(pageable)).thenReturn(page);

        mockMvc.perform(get("/userList.do?page=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userID").value("u001"));
    }

    @Test
    public void testUserEditPage() throws Exception {
        Mockito.when(userService.findById(1)).thenReturn(sampleUser);

        mockMvc.perform(get("/user_edit?id=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_edit"))
                .andExpect(model().attribute("user", hasProperty("userName", is("Alice"))));
    }

    @Test
    public void testCheckUserID_Available() throws Exception {
        Mockito.when(userService.countUserID("u001")).thenReturn(0);

        mockMvc.perform(post("/checkUserID.do").param("userID", "u001"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testCheckUserID_Taken() throws Exception {
        Mockito.when(userService.countUserID("u001")).thenReturn(1);

        mockMvc.perform(post("/checkUserID.do").param("userID", "u001"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testDelUser() throws Exception {
        mockMvc.perform(post("/delUser.do").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        Mockito.verify(userService).delByID(1);
    }

    // 跳转型重定向接口 addUser/modifyUser 只能做 redirect 验证，不校验返回内容
    @Test
    public void testAddUser() throws Exception {
        mockMvc.perform(post("/addUser.do")
                        .param("userID", "u002")
                        .param("userName", "Bob")
                        .param("password", "123")
                        .param("email", "bob@example.com")
                        .param("phone", "987654321"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));
    }

    @Test
    public void testModifyUser() throws Exception {
        Mockito.when(userService.findByUserID("u001")).thenReturn(sampleUser);

        mockMvc.perform(post("/modifyUser.do")
                        .param("userID", "u002")
                        .param("oldUserID", "u001")
                        .param("userName", "Bob")
                        .param("password", "456")
                        .param("email", "bob@example.com")
                        .param("phone", "987654321"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));
    }
}
