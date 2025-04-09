package com.demo.controller.admin;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminVenueController.class)
public class AdminVenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VenueService venueService;

    private Venue sampleVenue;

    @BeforeEach
    void setUp() {
        sampleVenue = new Venue();
        sampleVenue.setVenueID(1);
        sampleVenue.setVenueName("Test Venue");
        sampleVenue.setAddress("Test Address");
        sampleVenue.setDescription("Test Description");
        sampleVenue.setPrice(100);
        sampleVenue.setPicture("");
        sampleVenue.setOpen_time("08:00");
        sampleVenue.setClose_time("20:00");
    }

    @Test
    void testVenueManagePage() throws Exception {
        Page<Venue> page = new PageImpl<>(Collections.singletonList(sampleVenue));
        when(venueService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/venue_manage"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("total"))
                .andExpect(view().name("admin/venue_manage"));
    }

    @Test
    void testVenueAddPage() throws Exception {
        mockMvc.perform(get("/venue_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/venue_add"));
    }

    @Test
    void testVenueEditPage() throws Exception {
        when(venueService.findByVenueID(1)).thenReturn(sampleVenue);

        mockMvc.perform(get("/venue_edit").param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("venue"))
                .andExpect(view().name("/admin/venue_edit"));
    }

    @Test
    void testGetVenueList() throws Exception {
        Page<Venue> page = new PageImpl<>(Arrays.asList(sampleVenue));
        when(venueService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/venueList.do?page=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].venueName").value("Test Venue"));
    }

    @Test
    void testAddVenue() throws Exception {
        when(venueService.create(any(Venue.class))).thenReturn(1);

        MockMultipartFile file = new MockMultipartFile("picture", "", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/addVenue.do")
                        .file(file)
                        .param("venueName", "Test Venue")
                        .param("address", "Test Address")
                        .param("description", "Test Description")
                        .param("price", "100")
                        .param("open_time", "08:00")
                        .param("close_time", "20:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_manage"));
    }

    @Test
    void testModifyVenue() throws Exception {
        when(venueService.findByVenueID(1)).thenReturn(sampleVenue);

        MockMultipartFile file = new MockMultipartFile("picture", "", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(file)
                        .param("venueID", "1")
                        .param("venueName", "Updated Venue")
                        .param("address", "Updated Address")
                        .param("description", "Updated Description")
                        .param("price", "200")
                        .param("open_time", "09:00")
                        .param("close_time", "22:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_manage"));
    }

    @Test
    void testDeleteVenue() throws Exception {
        mockMvc.perform(post("/delVenue.do").param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testCheckVenueName() throws Exception {
        when(venueService.countVenueName("Test Venue")).thenReturn(0);

        mockMvc.perform(post("/checkVenueName.do")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("venueName", "Test Venue"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
