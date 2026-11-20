
package com.example.demoo.controller;

import com.example.demoo.model.Survey;
import com.example.demoo.model.SurveyResponse;
import com.example.demoo.service.SurveyService;
import com.example.demoo.service.SurveyResponseService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final SurveyService surveyService;
    private final SurveyResponseService responseService;

    public HomeController(
            SurveyService surveyService,
            SurveyResponseService responseService) {
        this.surveyService = surveyService;
        this.responseService = responseService;
    }

    // HOME
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("surveys", surveyService.getAllSurveys());
        return "index";
    }

    // RESULTS
    @GetMapping("/results")
    public String results(Model model) {

        List<Survey> surveys = surveyService.getAllSurveys();
        long totalSurveys = surveys.size();
        long totalResponses = responseService.getAllResponses().size();

        Map<Long, Long> responseCounts = new HashMap<>();
        Map<Long, Double> averageScores = new HashMap<>();
        Map<Long, Double> csatScores = new HashMap<>();
        Map<Long, Double> npsScores = new HashMap<>();

        for (Survey survey : surveys) {

            List<SurveyResponse> responses =
                    responseService.getResponsesBySurveyId(survey.getId());

            long count = responses.size();
            responseCounts.put(survey.getId(), count);

            if (count == 0) {
                averageScores.put(survey.getId(), 0.0);
                csatScores.put(survey.getId(), 0.0);
                npsScores.put(survey.getId(), 0.0);
                continue;
            }

            double average = responses.stream()
                    .mapToInt(SurveyResponse::getScore)
                    .average()
                    .orElse(0.0);

            averageScores.put(survey.getId(), average);

            if ("CSAT".equalsIgnoreCase(survey.getType())) {
                long satisfied = responses.stream()
                        .filter(r -> r.getScore() >= 4 && r.getScore() <= 5)
                        .count();

                csatScores.put(
                        survey.getId(),
                        ((double) satisfied / count) * 100
                );
            } else {
                csatScores.put(survey.getId(), 0.0);
            }

            if ("NPS".equalsIgnoreCase(survey.getType())) {
                long promoters = responses.stream()
                        .filter(r -> r.getScore() >= 9)
                        .count();

                long detractors = responses.stream()
                        .filter(r -> r.getScore() <= 6)
                        .count();

                npsScores.put(
                        survey.getId(),
                        ((double) (promoters - detractors) / count) * 100
                );
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

    // CREATE SURVEY
    @GetMapping("/surveys/new")
    public String showCreateForm(Model model) {
        model.addAttribute("survey", new Survey());
        return "survey-form";
    }

    @PostMapping("/surveys/create")
    public String createSurvey(
            @ModelAttribute Survey survey,
            RedirectAttributes redirectAttributes) {

        try {
            surveyService.createSurvey(survey);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Tạo khảo sát thành công!"
            );
            return "redirect:/surveys";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage()
            );
            return "redirect:/surveys/new";
        }
    }

    // SURVEY LIST
    @GetMapping("/surveys")
    public String listSurveys(Model model) {
        model.addAttribute("surveys", surveyService.getAllSurveys());
        return "survey-list";
    }

    // SURVEY DETAIL
    @GetMapping("/surveys/{id}")
    public String viewSurvey(
            @PathVariable Long id,
            Model model) {

        Survey survey = surveyService.getSurveyById(id);
        List<SurveyResponse> responses =
                responseService.getResponsesBySurveyId(id);

        model.addAttribute("survey", survey);
        model.addAttribute("responses", responses);

        return "survey-detail";
    }

    // EDIT SURVEY
    @GetMapping("/surveys/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        model.addAttribute("survey", surveyService.getSurveyById(id));
        return "survey-edit";
    }

    @PostMapping("/surveys/update/{id}")
    public String updateSurvey(
            @PathVariable Long id,
            @ModelAttribute Survey survey,
            RedirectAttributes redirectAttributes) {

        try {
            surveyService.updateSurvey(id, survey);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Cập nhật khảo sát thành công!"
            );
            return "redirect:/surveys";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage()
            );
            return "redirect:/surveys/edit/" + id;
        }
    }

    // DELETE SURVEY
    @PostMapping("/surveys/delete/{id}")
    public String deleteSurvey(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            // Delete responses before deleting the survey
            responseService.deleteResponsesBySurveyId(id);
            surveyService.deleteSurvey(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Xóa khảo sát thành công!"
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage()
            );
        }

        return "redirect:/surveys";
    }

    // CUSTOMER RESPONSE FORM
    @GetMapping("/surveys/{id}/respond")
    public String showResponseForm(
            @PathVariable Long id,
            Model model) {

        model.addAttribute("survey", surveyService.getSurveyById(id));
        model.addAttribute("response", new SurveyResponse());

        return "response-form";
    }

    // SUBMIT RESPONSE
    @PostMapping("/surveys/{id}/respond")
    public String submitResponse(
            @PathVariable Long id,
            @ModelAttribute SurveyResponse response,
            RedirectAttributes redirectAttributes) {

        try {
            responseService.createResponse(id, response);

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Gửi phản hồi thành công!"
            );

            return "redirect:/surveys/" + id;

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage()
            );

            return "redirect:/surveys/" + id + "/respond";
        }
    }
}
