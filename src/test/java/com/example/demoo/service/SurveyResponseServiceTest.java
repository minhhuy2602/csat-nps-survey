
package com.example.demoo.service;

import com.example.demoo.model.Survey;
import com.example.demoo.model.SurveyResponse;
import com.example.demoo.repository.SurveyRepository;
import com.example.demoo.repository.SurveyResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyResponseServiceTest {

    @Mock
    private SurveyResponseRepository responseRepository;

    @Mock
    private SurveyRepository surveyRepository;

    @InjectMocks
    private SurveyResponseService responseService;

    private Survey csatSurvey;
    private Survey npsSurvey;

    @BeforeEach
    void setUp() {
        csatSurvey = new Survey();
        csatSurvey.setName("CSAT Test");
        csatSurvey.setType("CSAT");

        npsSurvey = new Survey();
        npsSurvey.setName("NPS Test");
        npsSurvey.setType("NPS");
    }

    private SurveyResponse createResponse(Integer score) {
        SurveyResponse response = new SurveyResponse();
        response.setCustomerName("Test Customer");
        response.setScore(score);
        response.setComment("Test feedback");
        return response;
    }

    @Test
    void createResponse_CSATMinimumScore_ReturnsSavedResponse() {
        SurveyResponse response = createResponse(1);

        when(surveyRepository.findById(1L))
                .thenReturn(Optional.of(csatSurvey));
        when(responseRepository.save(response))
                .thenReturn(response);

        SurveyResponse result = responseService.createResponse(1L, response);

        assertNotNull(result);
        assertEquals(1, result.getScore());
        assertEquals(csatSurvey, result.getSurvey());
        verify(responseRepository).save(response);
    }

    @Test
    void createResponse_CSATMaximumScore_ReturnsSavedResponse() {
        SurveyResponse response = createResponse(5);

        when(surveyRepository.findById(1L))
                .thenReturn(Optional.of(csatSurvey));
        when(responseRepository.save(response))
                .thenReturn(response);

        SurveyResponse result = responseService.createResponse(1L, response);

        assertEquals(5, result.getScore());
        verify(responseRepository).save(response);
    }

    @Test
    void createResponse_CSATScoreBelowMinimum_ThrowsException() {
        SurveyResponse response = createResponse(0);

        when(surveyRepository.findById(1L))
                .thenReturn(Optional.of(csatSurvey));

        assertThrows(IllegalArgumentException.class,
                () -> responseService.createResponse(1L, response));

        verify(responseRepository, never()).save(any());
    }

    @Test
    void createResponse_CSATScoreAboveMaximum_ThrowsException() {
        SurveyResponse response = createResponse(6);

        when(surveyRepository.findById(1L))
                .thenReturn(Optional.of(csatSurvey));

        assertThrows(IllegalArgumentException.class,
                () -> responseService.createResponse(1L, response));

        verify(responseRepository, never()).save(any());
    }

    @Test
    void createResponse_NPSMinimumScore_ReturnsSavedResponse() {
        SurveyResponse response = createResponse(0);

        when(surveyRepository.findById(2L))
                .thenReturn(Optional.of(npsSurvey));
        when(responseRepository.save(response))
                .thenReturn(response);

        SurveyResponse result = responseService.createResponse(2L, response);

        assertEquals(0, result.getScore());
        assertEquals(npsSurvey, result.getSurvey());
        verify(responseRepository).save(response);
    }

    @Test
    void createResponse_NPSMaximumScore_ReturnsSavedResponse() {
        SurveyResponse response = createResponse(10);

        when(surveyRepository.findById(2L))
                .thenReturn(Optional.of(npsSurvey));
        when(responseRepository.save(response))
                .thenReturn(response);

        SurveyResponse result = responseService.createResponse(2L, response);

        assertEquals(10, result.getScore());
        verify(responseRepository).save(response);
    }

    @Test
    void createResponse_NPSScoreBelowMinimum_ThrowsException() {
        SurveyResponse response = createResponse(-1);

        when(surveyRepository.findById(2L))
                .thenReturn(Optional.of(npsSurvey));

        assertThrows(IllegalArgumentException.class,
                () -> responseService.createResponse(2L, response));

        verify(responseRepository, never()).save(any());
    }

    @Test
    void createResponse_NPSScoreAboveMaximum_ThrowsException() {
        SurveyResponse response = createResponse(11);

        when(surveyRepository.findById(2L))
                .thenReturn(Optional.of(npsSurvey));

        assertThrows(IllegalArgumentException.class,
                () -> responseService.createResponse(2L, response));

        verify(responseRepository, never()).save(any());
    }

    @Test
    void createResponse_NullScore_ThrowsException() {
        SurveyResponse response = createResponse(null);

        when(surveyRepository.findById(1L))
                .thenReturn(Optional.of(csatSurvey));

        assertThrows(IllegalArgumentException.class,
                () -> responseService.createResponse(1L, response));

        verify(responseRepository, never()).save(any());
    }

    @Test
    void createResponse_NonExistingSurvey_ThrowsException() {
        SurveyResponse response = createResponse(5);

        when(surveyRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> responseService.createResponse(99L, response));

        verify(responseRepository, never()).save(any());
    }

    @Test
    void createResponse_NullResponse_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> responseService.createResponse(1L, null));

        verify(surveyRepository, never()).findById(any());
        verify(responseRepository, never()).save(any());
    }
}
