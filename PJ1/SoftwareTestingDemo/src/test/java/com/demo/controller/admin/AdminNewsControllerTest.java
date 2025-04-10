package com.demo.controller.admin;

import com.demo.entity.News;
import com.demo.service.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void testNewsManage_emptyPage() throws Exception {
        Page<News> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(newsService.findAll(pageable)).thenReturn(emptyPage);

        mockMvc.perform(get("/news_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/news_manage"))
                .andExpect(model().attribute("total", 0));
    }

    @Test
    void testNewsManage_nullContent() throws Exception {
        Page<News> nullPage = new PageImpl<>(null, pageable, 0);
        when(newsService.findAll(pageable)).thenReturn(nullPage);

        mockMvc.perform(get("/news_manage"))
                .andExpect(status().isBadRequest()); // 你需要在 controller 中加 null 检查并抛异常
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
                .andExpect(model().attribute("news", news)); // 精确比对

        verify(newsService, times(1)).findById(1); // 确认调用
    }

    @Test
    void testNewsEdit_missingParam() throws Exception {
        mockMvc.perform(get("/news_edit")) // 没有参数
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNewsEdit_illegalParam_nonNumeric() throws Exception {
        mockMvc.perform(get("/news_edit").param("newsID", "abc")) // 非数字
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNewsEdit_illegalParam_decimal() throws Exception {
        mockMvc.perform(get("/news_edit").param("newsID", "1.5")) // 小数
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNewsEdit_illegalParam_negative() throws Exception {
        mockMvc.perform(get("/news_edit").param("newsID", "-1")) // 负数
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNewsEdit_notFound() throws Exception {
        when(newsService.findById(999)).thenReturn(null);

        mockMvc.perform(get("/news_edit").param("newsID", "999")) // 合法但查不到
                .andExpect(status().isNotFound());

        verify(newsService, times(1)).findById(999);
    }

    @Test
    void testNewsList() throws Exception {
        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by("time").descending());
        Page<News> expectedPage = new PageImpl<>(Collections.singletonList(news), expectedPageable, 1);

        when(newsService.findAll(expectedPageable)).thenReturn(expectedPage);

        mockMvc.perform(get("/newsList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].newsID").value(news.getNewsID()))
                .andExpect(jsonPath("$[0].title").value(news.getTitle()))
                .andExpect(jsonPath("$[0].content").value(news.getContent()));

        verify(newsService, times(1)).findAll(expectedPageable);
    }

    @Test
    void testNewsList_invalidPage() throws Exception {
        mockMvc.perform(get("/newsList.do").param("page", "-1"))
                .andExpect(status().isBadRequest());  // 或 is4xxClientError()
    }

    @Test
    void testNewsList_defaultPage() throws Exception {
        Pageable defaultPageable = PageRequest.of(0, 10, Sort.by("time").descending());
        when(newsService.findAll(defaultPageable)).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/newsList.do"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(newsService).findAll(defaultPageable);
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
    void testDelNews_missingParam() throws Exception {
        mockMvc.perform(post("/delNews.do")) // 没传参数
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDelNews_invalidParam_nonNumeric() throws Exception {
        mockMvc.perform(post("/delNews.do").param("newsID", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDelNews_invalidParam_decimal() throws Exception {
        mockMvc.perform(post("/delNews.do").param("newsID", "1.5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDelNews_invalidParam_negative() throws Exception {
        mockMvc.perform(post("/delNews.do").param("newsID", "-1"))
                .andExpect(status().isBadRequest()); // 如果 controller 做了限制
    }

    @Test
    void testDelNews_serviceThrowsException() throws Exception {
        doThrow(new RuntimeException("Deletion failed")).when(newsService).delById(1);

        mockMvc.perform(post("/delNews.do").param("newsID", "1"))
                .andExpect(status().isInternalServerError()); // 或根据异常处理策略定义响应

        verify(newsService).delById(1);
    }

    @Test
    void testDelNews_newsNotFound() throws Exception {
        doThrow(new RuntimeException("News not found")).when(newsService).delById(999);

        mockMvc.perform(post("/delNews.do").param("newsID", "999"))
                .andExpect(status().isInternalServerError()); // 或 isNotFound(), 取决于你 controller 的处理逻辑

        verify(newsService).delById(999);
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

    // 查不到新闻
    @Test
    void testModifyNews_newsNotFound() throws Exception {
        when(newsService.findById(1)).thenReturn(null); // 模拟查不到新闻

        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "1")
                        .param("title", "Updated Title")
                        .param("content", "Updated Content"))
                .andExpect(status().isNotFound()); // 需 controller 抛出 404

        verify(newsService, never()).update(any()); // 没有执行更新操作
    }

    @Test
    void testModifyNews_invalidNewsID_nonNumeric() throws Exception {
        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "abc")
                        .param("title", "Updated Title")
                        .param("content", "Updated Content"))
                .andExpect(status().isBadRequest()); // Spring 会自动抛出 400
    }

    @Test
    void testModifyNews_invalidNewsID_decimal() throws Exception {
        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "1.5")
                        .param("title", "Updated Title")
                        .param("content", "Updated Content"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testModifyNews_invalidNewsID_negative() throws Exception {
        mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", "-1")
                        .param("title", "Updated Title")
                        .param("content", "Updated Content"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testModifyNews_missingNewsID() throws Exception {
        mockMvc.perform(post("/modifyNews.do")
                        .param("title", "Updated Title")
                        .param("content", "Updated Content"))
                .andExpect(status().isBadRequest());
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
