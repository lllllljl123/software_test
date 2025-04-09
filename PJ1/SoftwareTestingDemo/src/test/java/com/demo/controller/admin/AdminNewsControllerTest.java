package com.demo.controller.admin;

import com.demo.entity.News;
import com.demo.service.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminNewsController.class)
class AdminNewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;

    private News news;
    private Pageable pageable;
    private Page<News> page;

    @BeforeEach
    void setUp() {
        news = new News();
        news.setNewsID(1);
        news.setTitle("Test News");
        news.setContent("This is a test content.");
        news.setTime(LocalDateTime.now());

        pageable = PageRequest.of(0, 10, Sort.by("time").ascending());
        page = new PageImpl<>(Collections.singletonList(news), pageable, 1);
    }

    @Test
    void testNewsManage() throws Exception {
        when(newsService.findAll(pageable)).thenReturn(page);

        mockMvc.perform(get("/news_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/news_manage"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    void testNewsAddView() throws Exception {
        mockMvc.perform(get("/news_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_add"));
    }

    @Test
    void testNewsEdit() throws Exception {
        when(newsService.findById(1)).thenReturn(news);

        mockMvc.perform(get("/news_edit").param("newsID", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_edit"))
                .andExpect(model().attributeExists("news"));
    }

    @Test
    void testNewsList() throws Exception {
        Pageable paged = PageRequest.of(0, 10, Sort.by("time").descending());
        when(newsService.findAll(paged)).thenReturn(page);

        mockMvc.perform(get("/newsList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Test News"));
    }

    @Test
    void testDelNews() throws Exception {
        doNothing().when(newsService).delById(1);

        mockMvc.perform(post("/delNews.do").param("newsID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(newsService).delById(1);
    }

    @Test
    void testModifyNews() throws Exception {
        when(newsService.findById(1)).thenReturn(news);
        doNothing().when(newsService).update(any(News.class));

        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "1")
                        .param("title", "Updated Title")
                        .param("content", "Updated Content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));

        verify(newsService).update(any(News.class));
    }

    @Test
    void testAddNews() throws Exception {
        when(newsService.create(any(News.class))).thenReturn(1);

        mockMvc.perform(post("/addNews.do")
                        .param("title", "New Title")
                        .param("content", "New Content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));

        verify(newsService).create(any(News.class));
    }
}
