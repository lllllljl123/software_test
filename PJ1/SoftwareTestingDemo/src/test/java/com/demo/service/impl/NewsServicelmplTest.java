package com.demo.service.impl;

import com.demo.dao.NewsDao;
import com.demo.entity.News;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Date;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

    @Mock
    private NewsDao newsDao;

    @InjectMocks
    private NewsServiceImpl newsService;

    @Test
    void testFindAll_Normal() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<News> mockPage = new PageImpl<>(Collections.singletonList(new News()));

        when(newsDao.findAll(pageable)).thenReturn(mockPage);

        Page<News> result = newsService.findAll(pageable);
        assertEquals(1, result.getTotalElements());
        verify(newsDao).findAll(pageable);
    }

    @Test
    void testFindAll_EmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(newsDao.findAll(pageable)).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<News> result = newsService.findAll(pageable);
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testFindById_Normal() {
        News news = new News();
        news.setNewsID(1);
        when(newsDao.getOne(1)).thenReturn(news);

        News result = newsService.findById(1);
        assertEquals(1, result.getNewsID());
        verify(newsDao).getOne(1);
    }

    @Test
    void testCreate_Normal() {
        News news = new News();
        news.setNewsID(42);

        when(newsDao.save(any())).thenReturn(news);

        int id = newsService.create(news);
        assertEquals(42, id);
        verify(newsDao).save(news);
    }

    @Test
    void testCreateNews_FieldCheck() {
        News news = new News();
        news.setNewsID(1);
        news.setTitle("Important Update");
        news.setContent("This is a detailed announcement.");
        news.setTime(LocalDateTime.of(2025, 4, 10, 12, 30));  // 用 Date 替代 LocalDateTime

        when(newsDao.save(any())).thenReturn(news);

        int id = newsService.create(news);
        assertEquals(1, id);

        verify(newsDao).save(argThat(n ->
                n.getTitle().equals("Important Update") &&
                        n.getContent().contains("announcement") &&
                        n.getTime().equals(LocalDateTime.of(2025, 4, 10, 12, 30))
        ));
    }

    @Test
    void testCreateNews_WithEmptyTitle() {
        News news = new News();
        news.setNewsID(2);
        news.setTitle(""); // 空标题
        news.setContent("Some content");
        news.setTime(LocalDateTime.of(2025, 4, 10, 12, 30));

        when(newsDao.save(any())).thenReturn(news);

        int id = newsService.create(news);
        assertEquals(2, id);
        verify(newsDao).save(news);
    }

    @Test
    void testCreateNews_MaxLengthTitleAndContent() {
        String maxTitle = String.join("", Collections.nCopies(100, "a"));
        String maxContent = String.join("", Collections.nCopies(5000, "b"));

        News news = new News();
        news.setNewsID(3);
        news.setTitle(maxTitle);
        news.setContent(maxContent);
        news.setTime(LocalDateTime.of(2025, 4, 10, 12, 30));

        when(newsDao.save(any())).thenReturn(news);

        int id = newsService.create(news);
        assertEquals(3, id);
    }

    @Test
    void testUpdateNews_FieldCheck() {
        News news = new News();
        news.setNewsID(5);
        news.setTitle("Updated Title");
        news.setContent("New content after update.");
        news.setTime(LocalDateTime.of(2025, 4, 10, 12, 30));

        assertDoesNotThrow(() -> newsService.update(news));
        verify(newsDao).save(news);
    }

    @Test
    void testUpdate() {
        News news = new News();
        news.setNewsID(100);

        newsService.update(news);
        verify(newsDao).save(news);
    }

    @Test
    void testDelById() {
        assertDoesNotThrow(() -> newsService.delById(1));
        verify(newsDao).deleteById(1);
    }

    @Test
    void testDeleteNews_ExceptionOnMissing() {
        doThrow(new RuntimeException("删除失败")).when(newsDao).deleteById(999);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> newsService.delById(999));
        assertEquals("删除失败", ex.getMessage());
    }

    @Test
    void testFindById_NotFound() {
        when(newsDao.getOne(999)).thenThrow(new RuntimeException("新闻不存在"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> newsService.findById(999));
        assertEquals("新闻不存在", ex.getMessage());
    }

    @Test
    void testFindById_NegativeId() {
        when(newsDao.getOne(-1)).thenThrow(new IllegalArgumentException("ID must be positive"));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> newsService.findById(-1));
        assertEquals("ID must be positive", ex.getMessage());
    }

    @Test
    void testCreate_SaveFails() {
        when(newsDao.save(any())).thenThrow(new RuntimeException("保存失败"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> newsService.create(new News()));
        assertEquals("保存失败", ex.getMessage());
    }
}
