package com.demo.controller;

import com.demo.entity.Message;
import com.demo.entity.News;
import com.demo.entity.Venue;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import com.demo.service.NewsService;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;

    @MockBean
    private VenueService venueService;

    @MockBean
    private MessageVoService messageVoService;

    @MockBean
    private MessageService messageService;

    private List<Venue> venueList;
    private List<News> newsList;
    private List<Message> messageList;
    private List<MessageVo> messageVoList;

    @BeforeEach
    void setUp() {
        venueList = Arrays.asList(new Venue());
        newsList = Arrays.asList(new News());
        messageList = Arrays.asList(new Message());
        messageVoList = Arrays.asList(new MessageVo());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());

        Mockito.when(venueService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(venueList));
        Mockito.when(newsService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(newsList));
        Mockito.when(messageService.findPassState(any(Pageable.class)))
                .thenReturn(new PageImpl<>(messageList));
        Mockito.when(messageVoService.returnVo(messageList))
                .thenReturn(messageVoList);
    }

    @Test
    void testIndexPageShouldRenderWithData() throws Exception {
        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("news_list"))
                .andExpect(model().attributeExists("venue_list"))
                .andExpect(model().attributeExists("message_list"));
    }

    @Test
    void testAdminIndexShouldRender() throws Exception {
        mockMvc.perform(get("/admin_index"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin_index"));
    }
}
