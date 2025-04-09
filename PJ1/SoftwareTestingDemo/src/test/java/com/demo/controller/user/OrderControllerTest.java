package com.demo.controller.user;

import com.demo.entity.Order;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.entity.vo.OrderVo;
import com.demo.entity.vo.VenueOrder;
import com.demo.exception.LoginException;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderVoService orderVoService;

    @MockBean
    private VenueService venueService;

    private MockHttpSession session;
    private User mockUser;

    @BeforeEach
    public void setUp() {
        mockUser = new User();
        mockUser.setUserID("test_user");
        session = new MockHttpSession();
        session.setAttribute("user", mockUser);
    }

    @Test
    void testOrderManage_shouldReturnViewAndModel() throws Exception {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("orderTime").descending());
        Page<Order> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
        Mockito.when(orderService.findUserOrder(eq("test_user"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/order_manage").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("total"))
                .andExpect(view().name("order_manage"));
    }



    @Test
    void testOrderManage_noLogin_shouldThrow() throws Exception {
        mockMvc.perform(get("/order_manage"))
                .andExpect(result -> {
                    Exception ex = result.getResolvedException();
                    assertTrue(ex instanceof LoginException, "应抛出 LoginException");
                });
    }


    @Test
    void testGetOrderList_shouldReturnJsonArray() throws Exception {
        Order mockOrder = new Order();
        List<OrderVo> mockVoList = Arrays.asList(new OrderVo()); // 修改这里
        Page<Order> mockPage = new PageImpl<>(Arrays.asList(mockOrder)); // 修改这里

        session.setAttribute("user", mockOrder); // 确保 session 中有用户对象，具体应为 User 实例

        Mockito.when(orderService.findUserOrder(eq("test_user"), any(Pageable.class))).thenReturn(mockPage);
        Mockito.when(orderVoService.returnVo(anyList())).thenReturn(mockVoList);

        mockMvc.perform(get("/getOrderList.do?page=1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testOrderPlace_withVenueID_shouldReturnVenuePage() throws Exception {
        Venue venue = new Venue();
        venue.setVenueID(1);
        Mockito.when(venueService.findByVenueID(1)).thenReturn(venue);

        mockMvc.perform(get("/order_place.do").param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("venue"))
                .andExpect(view().name("order_place"));
    }

    @Test
    void testOrderPlace_shouldReturnBlankPage() throws Exception {
        mockMvc.perform(get("/order_place"))
                .andExpect(status().isOk())
                .andExpect(view().name("order_place"));
    }

    @Test
    void testGetVenueOrder_shouldReturnJson() throws Exception {
        Venue venue = new Venue();
        venue.setVenueID(1);
        Mockito.when(venueService.findByVenueName("test")).thenReturn(venue);
        List<Order> orders = new ArrayList<>();
        orders.add(new Order());
        Mockito.when(orderService.findDateOrder(anyInt(), any(), any())).thenReturn(orders);


        mockMvc.perform(get("/order/getOrderList.do")
                        .param("venueName", "test")
                        .param("date", "2024-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venue").exists())
                .andExpect(jsonPath("$.orders").isArray());
    }

    @Test
    void testFinishOrder_shouldInvokeService() throws Exception {
        mockMvc.perform(post("/finishOrder.do").param("orderID", "1"))
                .andExpect(status().isOk());

        Mockito.verify(orderService).finishOrder(1);
    }

    @Test
    void testDeleteOrder_shouldReturnTrue() throws Exception {
        mockMvc.perform(post("/delOrder.do").param("orderID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        Mockito.verify(orderService).delOrder(1);
    }

    @Test
    void testEditOrder_shouldReturnPage() throws Exception {
        Order order = new Order();
        order.setVenueID(1);
        Venue venue = new Venue();
        Mockito.when(orderService.findById(1)).thenReturn(order);
        Mockito.when(venueService.findByVenueID(1)).thenReturn(venue);

        mockMvc.perform(get("/modifyOrder.do").param("orderID", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("order", "venue"))
                .andExpect(view().name("order_edit"));
    }

    @Test
    void testAddOrder_shouldRedirect() throws Exception {
        mockMvc.perform(post("/addOrder.do")
                        .session(session)
                        .param("venueName", "testVenue")
                        .param("date", "2025-04-10")
                        .param("startTime", "2025-04-10 10:00")
                        .param("hours", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));

        Mockito.verify(orderService).submit(
                eq("testVenue"),
                any(LocalDateTime.class),
                eq(2),
                eq("test_user")
        );
    }

    @Test
    void testModifyOrder_shouldRedirectAndReturnTrue() throws Exception {
        mockMvc.perform(post("/modifyOrder")
                        .session(session)
                        .param("venueName", "testVenue")
                        .param("date", "2025-04-10")
                        .param("startTime", "2025-04-10 10:00")
                        .param("hours", "2")
                        .param("orderID", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));

        Mockito.verify(orderService).updateOrder(
                eq(123),
                eq("testVenue"),
                any(LocalDateTime.class),
                eq(2),
                eq("test_user")
        );
    }

}
