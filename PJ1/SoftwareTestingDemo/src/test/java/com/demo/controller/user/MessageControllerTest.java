package com.demo.controller.user;

import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private MessageVoService messageVoService;

    private Message message;
    private User user;
    private MessageVo messageVo;
    private MockHttpSession session;

    @BeforeEach
    void setup() {
        LocalDateTime time = LocalDateTime.now();
        message = new Message(1, "user1", "test content", time, 1);
        user = new User(1, "user1", "username", "pwd", "email", "phone", 0, "pic.png");
        messageVo = new MessageVo(1, "user1", "test content", time, "username", "pic.png", 1);

        session = new MockHttpSession();
        session.setAttribute("user", user);
    }

    @Test
    void testMessageListPage_authenticated_OK() throws Exception {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        Page<Message> page = new PageImpl<>(Collections.singletonList(message), pageable, 1);

        when(messageService.findPassState(pageable)).thenReturn(page);
        when(messageVoService.returnVo(anyList())).thenReturn(Collections.singletonList(messageVo));
        when(messageService.findByUser(eq(user.getUserID()), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/message_list").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("message_list"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("user_total"));
    }

    @Test
    void testMessageListPage_notLoggedIn_throwsLoginException() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        when(messageService.findPassState(pageable)).thenReturn(new PageImpl<>(Collections.emptyList()));

        assertThatThrownBy(() -> mockMvc.perform(get("/message_list")))
                .hasCauseInstanceOf(com.demo.exception.LoginException.class)
                .hasMessageContaining("请登录");
    }

    @Test
    void testGetMessageList_OK() throws Exception {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        Page<Message> page = new PageImpl<>(Collections.singletonList(message), pageable, 1);

        when(messageService.findPassState(pageable)).thenReturn(page);
        when(messageVoService.returnVo(anyList())).thenReturn(Collections.singletonList(messageVo));

        mockMvc.perform(get("/message/getMessageList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("test content"));
    }

    @Test
    void testFindUserList_OK() throws Exception {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        Page<Message> page = new PageImpl<>(Collections.singletonList(message), pageable, 1);

        when(messageService.findByUser(eq(user.getUserID()), eq(pageable))).thenReturn(page);
        when(messageVoService.returnVo(anyList())).thenReturn(Collections.singletonList(messageVo));

        mockMvc.perform(get("/message/findUserList").param("page", "1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testSendMessage_OK() throws Exception {
        when(messageService.create(any(Message.class))).thenReturn(1);

        mockMvc.perform(post("/sendMessage")
                        .param("userID", "user1")
                        .param("content", "hello"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testModifyMessage_OK() throws Exception {
        when(messageService.findById(1)).thenReturn(message);
        doNothing().when(messageService).update(any(Message.class));

        mockMvc.perform(post("/modifyMessage.do")
                        .param("messageID", "1")
                        .param("content", "updated content"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testDelMessage_OK() throws Exception {
        doNothing().when(messageService).delById(1);

        mockMvc.perform(post("/delMessage.do")
                        .param("messageID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService).delById(1);
    }
}
