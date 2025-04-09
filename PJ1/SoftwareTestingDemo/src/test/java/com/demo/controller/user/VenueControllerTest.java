package com.demo.controller.user;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VenueController.class)
public class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VenueService venueService;

    private Venue mockVenue;

    @BeforeEach
    void setUp() {
        mockVenue = new Venue();
        mockVenue.setVenueID(1);
        mockVenue.setVenueName("Test Venue");
    }

    @Test
    void testToGymPage_shouldReturnVenueDetailPage() throws Exception {
        when(venueService.findByVenueID(1)).thenReturn(mockVenue);

        mockMvc.perform(get("/venue").param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("venue"))
                .andExpect(model().attribute("venue", notNullValue()));
    }

    @Test
    void testVenueListApi_shouldReturnPagedVenueJson() throws Exception {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("venueID").ascending());
        List<Venue> venueList = Collections.singletonList(mockVenue);
        Page<Venue> venuePage = new PageImpl<>(venueList, pageable, 1);

        when(venueService.findAll(any(Pageable.class))).thenReturn(venuePage);

        mockMvc.perform(get("/venuelist/getVenueList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].venueID").value(1));
    }

    @Test
    void testVenueListView_shouldReturnVenueListPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("venueID").ascending());
        List<Venue> venueList = Collections.singletonList(mockVenue);
        Page<Venue> venuePage = new PageImpl<>(venueList, pageable, 1);

        when(venueService.findAll(any(Pageable.class))).thenReturn(venuePage);

        mockMvc.perform(get("/venue_list"))
                .andExpect(status().isOk())
                .andExpect(view().name("venue_list"))
                .andExpect(model().attributeExists("venue_list"))
                .andExpect(model().attributeExists("total"));
    }
}
