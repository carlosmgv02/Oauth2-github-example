package org.example.oauth2.controller;

import org.example.oauth2.service.GitHubService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    private final GitHubService gitHubService;

    public UserController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal OAuth2User principal,
                          @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {
        if (principal != null) {
            String username = principal.getAttribute("login");
            model.addAttribute("login", username);
            model.addAttribute("name", principal.getAttribute("name"));
            model.addAttribute("avatar_url", principal.getAttribute("avatar_url"));
            model.addAttribute("html_url", principal.getAttribute("html_url"));
            model.addAttribute("bio", principal.getAttribute("bio"));
            model.addAttribute("company", principal.getAttribute("company"));
            model.addAttribute("blog", principal.getAttribute("blog"));
            model.addAttribute("location", principal.getAttribute("location"));
            model.addAttribute("email", principal.getAttribute("email"));
            model.addAttribute("public_repos", principal.getAttribute("public_repos"));
            model.addAttribute("followers", principal.getAttribute("followers"));
            model.addAttribute("following", principal.getAttribute("following"));

            // Fetch additional data
            List<Map<String, Object>> repositories = gitHubService.getPublicRepositories(username, authorizedClient);
            int totalCommits = gitHubService.getTotalCommits(username, authorizedClient);

            model.addAttribute("repositories", repositories);
            model.addAttribute("totalCommits", totalCommits);
        }
        return "profile";
    }

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            String name = principal.getAttribute("name");
            model.addAttribute("name", name);
        }
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}