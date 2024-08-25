package org.example.oauth2.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class GitHubService {


    private final String githubApiUrl;
    private final RestTemplate restTemplate;

    public GitHubService(@Value("${github.api.url}") String githubApiUrl, RestTemplate restTemplate) {
        this.githubApiUrl = githubApiUrl;
        this.restTemplate = restTemplate;
    }

    public List<Map<String, Object>> getPublicRepositories(String username, @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {
        String url = UriComponentsBuilder.fromHttpUrl(githubApiUrl)
                .pathSegment("users", username, "repos")
                .toUriString();

        HttpEntity<Void> entity = createHttpEntity(authorizedClient);

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.GET, entity,
                    getGenericListType());
            return Optional.ofNullable(response.getBody()).orElse(Collections.emptyList());
        } catch (HttpClientErrorException e) {
            log.error("Error fetching public repositories for user {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    public int getTotalCommits(String username, @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {
        String url = UriComponentsBuilder.fromHttpUrl(githubApiUrl)
                .pathSegment("users", username, "repos")
                .toUriString();

        HttpEntity<Void> entity = createHttpEntity(authorizedClient);

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.GET, entity,
                    getGenericListType());

            List<Map<String, Object>> repositories = Optional.ofNullable(response.getBody()).orElse(Collections.emptyList());
            return calculateTotalCommits(repositories, entity);
        } catch (HttpClientErrorException e) {
            log.error("Error fetching commits for user {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    private HttpEntity<Void> createHttpEntity(OAuth2AuthorizedClient authorizedClient) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
        return new HttpEntity<>(headers);
    }

    private int calculateTotalCommits(List<Map<String, Object>> repositories, HttpEntity<Void> entity) {
        int totalCommits = 0;

        for (Map<String, Object> repo : repositories) {
            String commitsUrl = ((String) repo.get("commits_url")).replace("{/sha}", "");

            try {
                ResponseEntity<List<Map<String, Object>>> commitsResponse = restTemplate.exchange(
                        commitsUrl, HttpMethod.GET, entity, getGenericListType());

                List<Map<String, Object>> commits = Optional.ofNullable(commitsResponse.getBody()).orElse(Collections.emptyList());
                totalCommits += commits.size();
            } catch (HttpClientErrorException e) {
                log.error("Error fetching commits from URL {}: {}", commitsUrl, e.getMessage(), e);
                throw e;
            }
        }

        return totalCommits;
    }

    // This method returns the generic type information needed for the exchange method.
    private static ParameterizedTypeReference<List<Map<String, Object>>> getGenericListType() {
        return new ParameterizedTypeReference<>() {};
    }
}