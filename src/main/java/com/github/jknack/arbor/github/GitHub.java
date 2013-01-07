/**
 * This copy of Woodstox XML processor is licensed under the
 * Apache (Software) License, version 2.0 ("the License").
 * See the License for details about distribution rights, and the
 * specific rights regarding derivate works.
 *
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing Woodstox, in file "ASL2.0", under the same directory
 * as this file.
 */
package com.github.jknack.arbor.github;

import static org.apache.http.client.fluent.Request.Get;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GitHub {

  private static final String SEARCH_PATH = "/legacy/repos/search/";

  private static final String TAGS = "/repos/%s/%s/tags";

  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * The logging system.
   */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private URI uri;

  private Map<String, GitHubRepository> repoCache = new HashMap<String, GitHubRepository>();

  public GitHub(final URI uri) {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    this.uri = uri;
  }

  public GitHub() {
    this(URI.create("https://api.github.com/"));
  }

  public GitHubRepository createRepository(final URI uri) throws IOException {
    String[] token = StringUtils.split(uri.getPath(), "/");
    String owner = token[0];
    String repo = token[1];
    String suffix = ".git";
    if (repo.endsWith(suffix)) {
      repo = repo.substring(0, repo.length() - suffix.length());
    }
    GitHubRepository repository = new GitHubRepository(owner, repo);
    repository.setTags(tags(owner, repo));
    return repository;
  }

  public GitHubRepository findRepository(final String term) throws IOException {
    try {
      GitHubRepository repository = repoCache.get(term);
      if (repository == null) {
        URI searchURI = new URIBuilder(uri).setPath(SEARCH_PATH + term)
            .addParameter("language", "JavaScript").build();
        logger.debug("GET {}", searchURI);

        String json = Get(searchURI)
            .execute().handleResponse(responseHandler());
        JsonNode node = objectMapper.readTree(json).get("repositories");
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(LinkedList.class,
            GitHubRepository.class);
        LinkedList<GitHubRepository> repositories = objectMapper.readValue(node.traverse(), type);
        if (repositories.size() == 0) {
          return null;
        }
        repository = repositories.getFirst();
        repository.setTags(tags(repository.getOwner(), repository.getName()));
        repoCache.put(term, repository);
      }
      return repository;
    } catch (URISyntaxException ex) {
      throw new IOException("Invalid URI", ex);
    }
  }

  private List<GitHubTag> tags(final String user, final String repository) throws IOException {
    try {
      URI tagsURI = new URIBuilder(uri).setPath(String.format(TAGS, user, repository)).build();
      logger.debug("GET {}", tagsURI);

      String json = Get(tagsURI)
          .execute().handleResponse(responseHandler());
      JavaType tagType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class,
          GitHubTag.class);
      List<GitHubTag> tags = objectMapper.readValue(json, tagType);
      Collections.sort(tags, GitHubTag.DESC);
      return tags;
    } catch (URISyntaxException ex) {
      throw new IOException("Invalid URI", ex);
    }
  }

  private ResponseHandler<String> responseHandler() {
    return new ResponseHandler<String>() {
      @Override
      public String handleResponse(final HttpResponse response) throws ClientProtocolException,
          IOException {
        String json = EntityUtils.toString(response.getEntity());
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() >= 300) {
          JsonNode node = objectMapper.readTree(json).get("message");
          String message = statusLine.getReasonPhrase() + "(" + statusLine.getStatusCode() + ")"
              + ": " + node.asText();
          throw new HttpResponseException(statusLine.getStatusCode(), message);
        }
        return json;
      }
    };
  }
}
