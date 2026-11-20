package com.example.demoo.service;

import com.example.demoo.model.Survey;
import com.example.demoo.model.SurveyResponse;
import com.example.demoo.repository.SurveyRepository;
import com.example.demoo.repository.SurveyResponseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SurveyResponseService {

    private final SurveyResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;

    public SurveyResponseService(
            SurveyResponseRepository responseRepository,
            SurveyRepository surveyRepository) {
        this.responseRepository = responseRepository;
        this.surveyRepository = surveyRepository;
    }

    // Get all responses
    public List<SurveyResponse> getAllResponses() {
        return responseRepository.findAll();
    }

    // Get responses by survey ID
    public List<SurveyResponse> getResponsesBySurveyId(Long surveyId) {
        if (!surveyRepository.existsById(surveyId)) {
            throw new IllegalArgumentException(
                    "Survey không tồn tại: " + surveyId
            );
        }

        return responseRepository.findBySurveyId(surveyId);
    }

    // Get response by ID
    public SurveyResponse getResponseById(Long id) {
        return responseRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Response không tồn tại: " + id
                        ));
    }

    // Create response
    public SurveyResponse createResponse(
            Long surveyId,
            SurveyResponse response) {

        if (response == null) {
            throw new IllegalArgumentException(
                    "Câu trả lời không được để trống!"
            );
        }

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Survey không tồn tại: " + surveyId
                        ));

        validateScore(survey, response.getScore());

        response.setSurvey(survey);

        return responseRepository.save(response);
    }

    // Delete response
    public void deleteResponse(Long id) {
        SurveyResponse response = getResponseById(id);
        responseRepository.delete(response);
    }

    // Delete all responses belonging to a survey
    public void deleteResponsesBySurveyId(Long surveyId) {
        List<SurveyResponse> responses =
                responseRepository.findBySurveyId(surveyId);

        responseRepository.deleteAll(responses);
    }

    // Validate CSAT and NPS score
    private void validateScore(Survey survey, Integer score) {

        if (score == null) {
            throw new IllegalArgumentException(
                    "Điểm đánh giá không được để trống!"
            );
        }

        if ("CSAT".equalsIgnoreCase(survey.getType())) {
            if (score < 1 || score > 5) {
                throw new IllegalArgumentException(
                        "Điểm CSAT phải từ 1 đến 5!"
                );
            }
        } else if ("NPS".equalsIgnoreCase(survey.getType())) {
            if (score < 0 || score > 10) {
                throw new IllegalArgumentException(
                        "Điểm NPS phải từ 0 đến 10!"
                );
            }
        } else {
            throw new IllegalArgumentException(
                    "Loại khảo sát không hợp lệ!"
            );
        }
    }
}