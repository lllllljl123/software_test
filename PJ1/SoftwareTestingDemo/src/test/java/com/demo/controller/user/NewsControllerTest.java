package com.demo.controller.user;

import com.demo.entity.News;
import com.demo.service.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsController.class)
public class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;

    private News sampleNews;

    @BeforeEach
    void setup() {
        sampleNews = new News();
        sampleNews.setNewsID(1);
        sampleNews.setTitle("Test News");
        sampleNews.setTime(LocalDateTime.now());
    }

    @Test
    void testNewsDetail_shouldReturnNewsView() throws Exception {
        Mockito.when(newsService.findById(1)).thenReturn(sampleNews);

        mockMvc.perform(get("/news").param("newsID", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("news"))
                .andExpect(model().attributeExists("news"));
    }

    @Test
    void testNewsListJson_shouldReturnPageJson() throws Exception {
        List<News> newsList = Collections.singletonList(sampleNews);
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        Page<News> newsPage = new PageImpl<>(newsList, pageable, 1);
        Mockito.when(newsService.findAll(any(Pageable.class))).thenReturn(newsPage);

        mockMvc.perform(get("/news/getNewsList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testNewsListPage_shouldReturnHtmlPage() throws Exception {
        List<News> newsList = Collections.singletonList(sampleNews);
        Page<News> newsPage = new PageImpl<>(newsList, PageRequest.of(0, 5), 1);
        Mockito.when(newsService.findAll(any(Pageable.class))).thenReturn(newsPage);

        mockMvc.perform(get("/news_list"))
                .andExpect(status().isOk())
                .andExpect(view().name("news_list"))
                .andExpect(model().attributeExists("news_list"))
                .andExpect(model().attributeExists("total"));
    }
}
