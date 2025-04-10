package com.demo.service.impl;

import com.demo.dao.MessageDao;
import com.demo.entity.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static com.demo.service.MessageService.STATE_NO_AUDIT;  // ✅ 使用接口里的常量
import static com.demo.service.MessageService.STATE_PASS;
import static com.demo.service.MessageService.STATE_REJECT;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageDao messageDao;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    void testFindById() {
        Message mockMsg = new Message();
        mockMsg.setMessageID(1);

        when(messageDao.getOne(1)).thenReturn(mockMsg);
        Message result = messageService.findById(1);

        assertEquals(1, result.getMessageID());
    }

    @Test
    void testFindByUser() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Message> msgList = Arrays.asList(new Message());
        Page<Message> mockPage = new PageImpl<>(msgList, pageable, msgList.size());

        when(messageDao.findAllByUserID("user123", pageable)).thenReturn(mockPage);
        Page<Message> result = messageService.findByUser("user123", pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testCreate() {
        Message message = new Message();
        message.setMessageID(5);

        when(messageDao.save(any())).thenReturn(message);
        int result = messageService.create(message);

        assertEquals(5, result);
    }

    @Test
    void testDelById() {
        assertDoesNotThrow(() -> messageService.delById(1));
        verify(messageDao).deleteById(1);
    }

    @Test
    void testUpdate() {
        Message message = new Message();
        message.setMessageID(2);

        messageService.update(message);
        verify(messageDao).save(message);
    }

    @Test
    void testConfirmMessage_Success() {
        Message message = new Message();
        message.setMessageID(4);

        when(messageDao.findByMessageID(4)).thenReturn(message);
        doNothing().when(messageDao).updateState(eq(STATE_PASS), eq(4));

        messageService.confirmMessage(4);
        verify(messageDao).updateState(eq(STATE_PASS), eq(4));
    }

    @Test
    void testConfirmMessage_NotFound() {
        when(messageDao.findByMessageID(999)).thenReturn(null);
        Exception e = assertThrows(RuntimeException.class, () -> messageService.confirmMessage(999));
        assertEquals("留言不存在", e.getMessage());
    }

    @Test
    void testRejectMessage_Success() {
        Message message = new Message();
        message.setMessageID(4);

        when(messageDao.findByMessageID(4)).thenReturn(message);
        doNothing().when(messageDao).updateState(eq(STATE_REJECT), eq(4));

        messageService.rejectMessage(4);
        verify(messageDao).updateState(eq(STATE_REJECT), eq(4));
    }

    @Test
    void testRejectMessage_NotFound() {
        when(messageDao.findByMessageID(404)).thenReturn(null);
        Exception e = assertThrows(RuntimeException.class, () -> messageService.rejectMessage(404));
        assertEquals("留言不存在", e.getMessage());
    }

    @Test
    void testFindWaitState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(messageDao.findAllByState(STATE_NO_AUDIT, pageable))
                .thenReturn(new PageImpl<>(Arrays.asList()));

        Page<Message> result = messageService.findWaitState(pageable);
        assertNotNull(result);
    }

    @Test
    void testFindPassState() {
        Pageable pageable = PageRequest.of(0, 10);
        when(messageDao.findAllByState(STATE_PASS, pageable))
                .thenReturn(new PageImpl<>(Arrays.asList()));

        Page<Message> result = messageService.findPassState(pageable);
        assertNotNull(result);
    }
}
