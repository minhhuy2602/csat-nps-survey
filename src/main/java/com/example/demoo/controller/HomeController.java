
package com.example.demoo.controller;

import com.example.demoo.model.Survey;
import com.example.demoo.model.SurveyResponse;
import com.example.demoo.repository.SurveyRepository;
import com.example.demoo.repository.SurveyResponseRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository responseRepository;

    public HomeController(
            SurveyRepository surveyRepository,
            SurveyResponseRepository responseRepository) {
        this.surveyRepository = surveyRepository;
        this.responseRepository = responseRepository;
    }

    // ==================== HOME ====================

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("surveys", surveyRepository.findAll());
        return "index";
    }

    // ==================== RESULTS ====================

    @GetMapping("/results")
    public String results(Model model) {

        List<Survey> surveys = surveyRepository.findAll();

        long totalSurveys = surveyRepository.count();
        long totalResponses = responseRepository.count();

        Map<Long, Long> responseCounts = new HashMap<>();
        Map<Long, Double> averageScores = new HashMap<>();
        Map<Long, Double> csatScores = new HashMap<>();
        Map<Long, Double> npsScores = new HashMap<>();

        for (Survey survey : surveys) {

            List<SurveyResponse> responses =
                    responseRepository.findBySurveyId(survey.getId());

            long count = responses.size();

            responseCounts.put(survey.getId(), count);

            if (count == 0) {
                averageScores.put(survey.getId(), 0.0);
                csatScores.put(survey.getId(), 0.0);
                npsScores.put(survey.getId(), 0.0);
                continue;
            }

            // Calculate average score (kept for compatibility)
            double average = responses.stream()
                    .mapToInt(SurveyResponse::getScore)
                    .average()
                    .orElse(0.0);

            averageScores.put(survey.getId(), average);

            // Calculate CSAT score
            // CSAT = (Number of satisfied responses / Total responses) * 100
            if ("CSAT".equalsIgnoreCase(survey.getType())) {

                long satisfiedResponses = responses.stream()
                        .filter(r -> r.getScore() >= 4 && r.getScore() <= 5)
                        .count();

                double csat =
                        ((double) satisfiedResponses / count) * 100;

                csatScores.put(survey.getId(), csat);

            } else {
                csatScores.put(survey.getId(), 0.0);
            }

            // Calculate NPS score
            // NPS = % Promoters - % Detractors
            if ("NPS".equalsIgnoreCase(survey.getType())) {

                long promoters = responses.stream()
                        .filter(r -> r.getScore() >= 9)
                        .count();

                long detractors = responses.stream()
                        .filter(r -> r.getScore() <= 6)
                        .count();

                double nps =
                        ((double) (promoters - detractors) / count) * 100;

                npsScores.put(survey.getId(), nps);

            } else {
                npsScores.put(survey.getId(), 0.0);
            }
        }

        model.addAttribute("surveys", surveys);
        model.addAttribute("totalSurveys", totalSurveys);
        model.addAttribute("totalResponses", totalResponses);
        model.addAttribute("responseCounts", responseCounts);
        model.addAttribute("averageScores", averageScores);
        model.addAttribute("csatScores", csatScores);
        model.addAttribute("npsScores", npsScores);

        return "results";
    }

    // ==================== CREATE SURVEY ====================

    @GetMapping("/surveys/new")
    public String showCreateForm(Model model) {
        model.addAttribute("survey", new Survey());
        return "survey-form";
    }

    @PostMapping("/surveys/create")
    public String createSurvey(
            @ModelAttribute Survey survey,
            RedirectAttributes redirectAttributes) {

        surveyRepository.save(survey);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Tạo khảo sát thành công!"
        );

        return "redirect:/surveys";
    }

    // ==================== SURVEY LIST ====================

    @GetMapping("/surveys")
    public String listSurveys(Model model) {
        model.addAttribute("surveys", surveyRepository.findAll());
        return "survey-list";
    }

    // ==================== SURVEY DETAIL ====================

    @GetMapping("/surveys/{id}")
    public String viewSurvey(
            @PathVariable Long id,
            Model model) {

        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Survey không tồn tại: " + id
                        ));

        List<SurveyResponse> responses =
                responseRepository.findBySurveyId(id);

        model.addAttribute("survey", survey);
        model.addAttribute("responses", responses);

        return "survey-detail";
    }

    // ==================== EDIT SURVEY ====================

    @GetMapping("/surveys/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Survey không tồn tại: " + id
                        ));

        model.addAttribute("survey", survey);

        return "survey-edit";
    }

    @PostMapping("/surveys/update/{id}")
    public String updateSurvey(
            @PathVariable Long id,
            @ModelAttribute Survey survey,
            RedirectAttributes redirectAttributes) {

        Survey existingSurvey = surveyRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Survey không tồn tại: " + id
                        ));

        existingSurvey.setName(survey.getName());
        existingSurvey.setType(survey.getType());
        existingSurvey.setDescription(survey.getDescription());
        existingSurvey.setStartDate(survey.getStartDate());
        existingSurvey.setEndDate(survey.getEndDate());

        surveyRepository.save(existingSurvey);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Cập nhật khảo sát thành công!"
        );

        return "redirect:/surveys";
    }

    // ==================== DELETE SURVEY ====================

    @PostMapping("/surveys/delete/{id}")
    public String deleteSurvey(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Survey không tồn tại: " + id
                        ));

        List<SurveyResponse> responses =
                responseRepository.findBySurveyId(id);

        responseRepository.deleteAll(responses);
        surveyRepository.delete(survey);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Xóa khảo sát thành công!"
        );

        return "redirect:/surveys";
    }

    // ==================== CUSTOMER RESPONSE FORM ====================

    @GetMapping("/surveys/{id}/respond")
    public String showResponseForm(
            @PathVariable Long id,
            Model model) {

        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Survey không tồn tại: " + id
                        ));

        model.addAttribute("survey", survey);
        model.addAttribute("response", new SurveyResponse());

        return "response-form";
    }

    // ==================== SUBMIT RESPONSE ====================

    @PostMapping("/surveys/{id}/respond")
    public String submitResponse(
            @PathVariable Long id,
            @ModelAttribute SurveyResponse response,
            RedirectAttributes redirectAttributes) {

        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Survey không tồn tại: " + id
                        ));

        Integer score = response.getScore();

        // Check empty score
        if (score == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Vui lòng chọn điểm đánh giá!"
            );

            return "redirect:/surveys/" + id + "/respond";
        }

        // Validate CSAT score: 1-5
        if ("CSAT".equalsIgnoreCase(survey.getType())) {

            if (score < 1 || score > 5) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Điểm CSAT phải nằm trong khoảng từ 1 đến 5!"
                );

                return "redirect:/surveys/" + id + "/respond";
            }
        }

        // Validate NPS score: 0-10
        if ("NPS".equalsIgnoreCase(survey.getType())) {

            if (score < 0 || score > 10) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Điểm NPS phải nằm trong khoảng từ 0 đến 10!"
                );

                return "redirect:/surveys/" + id + "/respond";
            }
        }

        // Create a new response
        SurveyResponse newResponse = new SurveyResponse();

        newResponse.setSurvey(survey);
        newResponse.setCustomerName(response.getCustomerName());
        newResponse.setScore(score);
        newResponse.setComment(response.getComment());

        responseRepository.save(newResponse);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Gửi phản hồi thành công!"
        );

        return "redirect:/surveys/" + id;
    }
}
