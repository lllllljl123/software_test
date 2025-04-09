package com.demo.controller.admin;

import com.demo.entity.Order;
import com.demo.entity.vo.OrderVo;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminOrderController.class)
public class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderVoService orderVoService;

    @Test
    @DisplayName("GET /reservation_manage 应返回正确视图和模型")
    void testReservationManage() throws Exception {
        List<Order> mockOrders = java.util.Arrays.asList(new Order());
        List<OrderVo> mockOrderVos = java.util.Arrays.asList(new OrderVo());

        Page<Order> mockPage = new PageImpl<>(mockOrders, PageRequest.of(0, 10), 1);

        when(orderService.findAuditOrder()).thenReturn(mockOrders);
        when(orderVoService.returnVo(mockOrders)).thenReturn(mockOrderVos);
        when(orderService.findNoAuditOrder(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/reservation_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservation_manage"))
                .andExpect(model().attributeExists("order_list"))
                .andExpect(model().attributeExists("total"));

        verify(orderService).findAuditOrder();
        verify(orderService).findNoAuditOrder(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /admin/getOrderList.do?page=1 返回未审核订单列表 JSON")
    void testGetNoAuditOrder() throws Exception {
        List<Order> mockOrders = Arrays.asList(new Order());
        List<OrderVo> mockVos = Arrays.asList(new OrderVo());

        when(orderService.findNoAuditOrder(any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockOrders));
        when(orderVoService.returnVo(mockOrders)).thenReturn(mockVos);

        mockMvc.perform(get("/admin/getOrderList.do?page=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(mockVos.size()));

        verify(orderService).findNoAuditOrder(any(Pageable.class));
        verify(orderVoService).returnVo(mockOrders);
    }


    @Test
    @DisplayName("POST /passOrder.do 应调用确认订单")
    void testConfirmOrder() throws Exception {
        mockMvc.perform(post("/passOrder.do").param("orderID", "123"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderService).confirmOrder(123);
    }

    @Test
    @DisplayName("POST /rejectOrder.do 应调用拒绝订单")
    void testRejectOrder() throws Exception {
        mockMvc.perform(post("/rejectOrder.do").param("orderID", "456"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderService).rejectOrder(456);
    }

    @Test
    @DisplayName("GET /admin/getOrderList.do?page=0 应返回错误")
    void testGetNoAuditOrderList_invalidPage() throws Exception {
        mockMvc.perform(get("/admin/getOrderList.do?page=0"))
                .andExpect(status().is5xxServerError()); // 或 isBadRequest()，取决于控制器是否处理异常
    }
}
