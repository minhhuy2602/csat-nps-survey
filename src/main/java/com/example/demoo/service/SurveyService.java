
package com.example.demoo.service;

import com.example.demoo.model.Survey;
import com.example.demoo.repository.SurveyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SurveyService {

    private final SurveyRepository surveyRepository;

    public SurveyService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    // Get all surveys
    public List<Survey> getAllSurveys() {
        return surveyRepository.findAll();
    }

    // Get survey by ID
    public Survey getSurveyById(Long id) {
        return surveyRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Survey không tồn tại: " + id
                        ));
    }

    // Create survey
    public Survey createSurvey(Survey survey) {
        validateSurvey(survey);
        return surveyRepository.save(survey);
    }

    // Update survey
    public Survey updateSurvey(Long id, Survey updatedSurvey) {
        validateSurvey(updatedSurvey);

        Survey existingSurvey = getSurveyById(id);

        existingSurvey.setName(updatedSurvey.getName());
        existingSurvey.setType(updatedSurvey.getType());
        existingSurvey.setDescription(updatedSurvey.getDescription());
        existingSurvey.setStartDate(updatedSurvey.getStartDate());
        existingSurvey.setEndDate(updatedSurvey.getEndDate());

        return surveyRepository.save(existingSurvey);
    }

    // Delete survey
    public void deleteSurvey(Long id) {
        Survey survey = getSurveyById(id);
        surveyRepository.delete(survey);
    }

    // Validate survey data
    private void validateSurvey(Survey survey) {
        if (survey == null) {
            throw new IllegalArgumentException(
                    "Survey không được để trống!"
            );
        }

        if (survey.getName() == null
                || survey.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Tên khảo sát không được để trống!"
            );
        }

        if (survey.getType() == null
                || !(survey.getType().equalsIgnoreCase("CSAT")
                || survey.getType().equalsIgnoreCase("NPS"))) {
            throw new IllegalArgumentException(
                    "Loại khảo sát phải là CSAT hoặc NPS!"
            );
        }

        if (survey.getStartDate() != null
                && survey.getEndDate() != null
                && survey.getEndDate().isBefore(survey.getStartDate())) {
            throw new IllegalArgumentException(
                    "Ngày kết thúc không được trước ngày bắt đầu!"
            );
        }
    }
}
