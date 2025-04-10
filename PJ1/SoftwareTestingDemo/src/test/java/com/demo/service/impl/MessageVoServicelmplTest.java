package com.demo.service.impl;

import com.demo.dao.MessageDao;
import com.demo.dao.UserDao;
import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageVoServiceImplTest {

    @Mock
    private MessageDao messageDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private MessageVoServiceImpl messageVoService;

    // 正常路径
    @Test
    void testReturnMessageVoByMessageID_Normal() {
        Message message = new Message();
        message.setMessageID(1);
        message.setUserID("user123");
        message.setContent("hello");
        message.setTime(LocalDateTime.of(2024, 1, 1, 0, 0));
        message.setState(1);

        User user = new User();
        user.setUserID("user123");
        user.setUserName("Tom");
        user.setPicture("pic.png");

        when(messageDao.findByMessageID(1)).thenReturn(message);
        when(userDao.findByUserID("user123")).thenReturn(user);

        MessageVo vo = messageVoService.returnMessageVoByMessageID(1);
        assertNotNull(vo);
        assertEquals("user123", vo.getUserID());
        assertEquals("Tom", vo.getUserName());
        assertEquals("pic.png", vo.getPicture());

        verify(messageDao).findByMessageID(1);
        verify(userDao).findByUserID("user123");
    }

    // message 为 null
    @Test
    void testReturnMessageVoByMessageID_MessageNotFound() {
        when(messageDao.findByMessageID(999)).thenReturn(null);
        assertThrows(NullPointerException.class, () -> messageVoService.returnMessageVoByMessageID(999));
    }

    // user 为 null
    @Test
    void testReturnMessageVoByMessageID_UserNotFound() {
        Message message = new Message();
        message.setMessageID(1);
        message.setUserID("userX");

        when(messageDao.findByMessageID(1)).thenReturn(message);
        when(userDao.findByUserID("userX")).thenReturn(null);

        assertThrows(NullPointerException.class, () -> messageVoService.returnMessageVoByMessageID(1));
    }

    // 空列表
    @Test
    void testReturnVo_EmptyList() {
        List<MessageVo> result = messageVoService.returnVo(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // 字段值验证。输入一个元素的List能正确输出一个MessageVo
    @Test
    void testReturnVo_OneElement() {
        Message message = new Message();
        message.setMessageID(1);
        message.setUserID("user123");
        message.setContent("test");
        message.setTime(LocalDateTime.now());
        message.setState(0);

        User user = new User();
        user.setUserID("user123");
        user.setUserName("Alice");
        user.setPicture("img.jpg");

        when(messageDao.findByMessageID(1)).thenReturn(message);
        when(userDao.findByUserID("user123")).thenReturn(user);

        List<MessageVo> result = messageVoService.returnVo(Arrays.asList(message));

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getUserName());

        verify(messageDao).findByMessageID(1);
        verify(userDao).findByUserID("user123");
    }

    // 多余数据
    @Test
    void testReturnVo_MultipleElements() {
        Message m1 = new Message(); m1.setMessageID(1); m1.setUserID("u1");
        Message m2 = new Message(); m2.setMessageID(2); m2.setUserID("u2");

        User u1 = new User(); u1.setUserID("u1"); u1.setUserName("User1"); u1.setPicture("p1.png");
        User u2 = new User(); u2.setUserID("u2"); u2.setUserName("User2"); u2.setPicture("p2.png");

        when(messageDao.findByMessageID(1)).thenReturn(m1);
        when(messageDao.findByMessageID(2)).thenReturn(m2);
        when(userDao.findByUserID("u1")).thenReturn(u1);
        when(userDao.findByUserID("u2")).thenReturn(u2);

        List<MessageVo> result = messageVoService.returnVo(Arrays.asList(m1, m2));
        assertEquals(2, result.size());
        assertEquals("User2", result.get(1).getUserName());
    }
}
