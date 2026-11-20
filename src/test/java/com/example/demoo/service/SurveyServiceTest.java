
package com.example.demoo.service;

import com.example.demoo.model.Survey;
import com.example.demoo.repository.SurveyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock
    private SurveyRepository surveyRepository;

    @InjectMocks
    private SurveyService surveyService;

    private Survey survey;

    @BeforeEach
    void setUp() {
        survey = new Survey();
        survey.setName("Customer Satisfaction");
        survey.setType("CSAT");
        survey.setDescription("Customer feedback survey");
        survey.setStartDate(LocalDate.of(2026, 11, 1));
        survey.setEndDate(LocalDate.of(2026, 11, 30));
    }

    @Test
    void createSurvey_ValidData_ReturnsSavedSurvey() {
        when(surveyRepository.save(survey)).thenReturn(survey);

        Survey result = surveyService.createSurvey(survey);

        assertNotNull(result);
        assertEquals("Customer Satisfaction", result.getName());
        verify(surveyRepository).save(survey);
    }

    @Test
    void createSurvey_BlankName_ThrowsException() {
        survey.setName(" ");

        assertThrows(IllegalArgumentException.class,
                () -> surveyService.createSurvey(survey));

        verify(surveyRepository, never()).save(any());
    }

    @Test
    void createSurvey_InvalidType_ThrowsException() {
        survey.setType("CES");

        assertThrows(IllegalArgumentException.class,
                () -> surveyService.createSurvey(survey));

        verify(surveyRepository, never()).save(any());
    }

    @Test
    void createSurvey_EndDateBeforeStartDate_ThrowsException() {
        survey.setStartDate(LocalDate.of(2026, 11, 30));
        survey.setEndDate(LocalDate.of(2026, 11, 1));

        assertThrows(IllegalArgumentException.class,
                () -> surveyService.createSurvey(survey));

        verify(surveyRepository, never()).save(any());
    }

    @Test
    void createSurvey_NullSurvey_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> surveyService.createSurvey(null));

        verify(surveyRepository, never()).save(any());
    }

    @Test
    void getSurveyById_ExistingId_ReturnsSurvey() {
        when(surveyRepository.findById(1L))
                .thenReturn(Optional.of(survey));

        Survey result = surveyService.getSurveyById(1L);

        assertNotNull(result);
        assertEquals("Customer Satisfaction", result.getName());
    }

    @Test
    void getSurveyById_NonExistingId_ThrowsException() {
        when(surveyRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> surveyService.getSurveyById(99L));
    }

    @Test
    void getAllSurveys_ReturnsSurveyList() {
        when(surveyRepository.findAll()).thenReturn(List.of(survey));

        List<Survey> result = surveyService.getAllSurveys();

        assertEquals(1, result.size());
        assertEquals("CSAT", result.get(0).getType());
    }

    @Test
    void updateSurvey_ValidData_UpdatesSurvey() {
        Survey updatedSurvey = new Survey();
        updatedSurvey.setName("Updated Survey");
        updatedSurvey.setType("NPS");
        updatedSurvey.setDescription("Updated description");
        updatedSurvey.setStartDate(LocalDate.of(2026, 12, 1));
        updatedSurvey.setEndDate(LocalDate.of(2026, 12, 31));

        when(surveyRepository.findById(1L))
                .thenReturn(Optional.of(survey));
        when(surveyRepository.save(any(Survey.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Survey result = surveyService.updateSurvey(1L, updatedSurvey);

        assertEquals("Updated Survey", result.getName());
        assertEquals("NPS", result.getType());
        verify(surveyRepository).save(survey);
    }

    @Test
    void deleteSurvey_ExistingId_DeletesSurvey() {
        when(surveyRepository.findById(1L))
                .thenReturn(Optional.of(survey));

        surveyService.deleteSurvey(1L);

        verify(surveyRepository).delete(survey);
    }
}
