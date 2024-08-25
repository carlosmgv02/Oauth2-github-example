package org.example.oauth2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GitHubService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);

    @Value("${github.api.url}")
    private String githubApiUrl;

    private final RestTemplate restTemplate;

    public GitHubService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Map<String, Object>> getPublicRepositories(String username, @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {
        String url = githubApiUrl + "/users/" + username + "/repos";
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error fetching public repositories for user {}: {}", username, e.getMessage());
            return null;
        }
    }

    public int getTotalCommits(String username, @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {
        String url = githubApiUrl + "/users/" + username + "/repos";
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        int totalCommits = 0;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>[]> response = restTemplate.exchange(url, HttpMethod.GET, entity,
                    (Class<Map<String, Object>[]>) (Class<?>) Map[].class);
            Map<String, Object>[] repositories = response.getBody();

            if (repositories != null) {
                for (Map<String, Object> repo : repositories) {
                    String commitsUrl = (String) repo.get("commits_url");
                    commitsUrl = commitsUrl.replace("{/sha}", "");

                    ResponseEntity<List<Map<String, Object>>> commitsResponse = restTemplate.exchange(commitsUrl,
                            HttpMethod.GET, entity, (Class<List<Map<String, Object>>>) (Class<?>) List.class);
                    List<Map<String, Object>> commits = commitsResponse.getBody();

                    if (commits != null) {
                        totalCommits += commits.size();
                    }
                }
            }
        } catch (HttpClientErrorException e) {
            logger.error("Error fetching commits for user {}: {}", username, e.getMessage());
        }

        return totalCommits;
    }
}