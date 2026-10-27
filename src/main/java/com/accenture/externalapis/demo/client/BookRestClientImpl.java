package com.accenture.externalapis.demo.client;

import com.accenture.externalapis.demo.config.ExternalServiceProperties;
import com.accenture.externalapis.demo.dto.BookApiResponse;
import com.accenture.externalapis.demo.dto.BookDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.util.ArrayList;
import java.util.List;


@Component
public class BookRestClientImpl implements BookRestClient {

    private final RestClient restClient;

    public BookRestClientImpl(RestClient.Builder builder, ExternalServiceProperties properties) {
        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.token())
                .build();
    }

    public BookDto getBook(Long id) {
        try {
            BookApiResponse response = restClient.get()
                    .uri("/books/{id}", id)
                    .retrieve()
                    .body(BookApiResponse.class);
            if (response == null) {
                throw new ClientException("Empty response for book " + id);
            }
            return toDto(response);
        } catch (RestClientException e) {
            throw toClientException(e);
        }
    }


    private BookDto toDto(BookApiResponse response) {
        try {
            return new BookDto(
                    response.id(),
                    response.title(),
                    response.author(),
                    response.price()
            );
        } catch (RestClientException e) {
            throw toClientException(e);
        }
    }


    public List<BookDto> getAllBooks() {
        try {
            BookApiResponse[] responses = restClient.get()
                    .uri("/books")
                    .retrieve()
                    .body(BookApiResponse[].class);

            List<BookDto> books = new ArrayList<>();
            if (responses == null) {
                throw new ClientException("Empty response for book list");
            }
            for (BookApiResponse response : responses) {
                books.add(toDto(response));
            }
            return books;
        } catch (RestClientException e) {
            throw toClientException(e);
        }


    }

    private ClientException toClientException(RestClientException e) {
        if (e instanceof HttpClientErrorException exception) {
            return new ClientException("Request rejected: " + exception.getStatusCode(),
                    exception.getStatusCode().value(), e);
        }
        if (e instanceof HttpServerErrorException exception) {
            return new ClientException("External service error: " + exception.getStatusCode(),
                    exception.getStatusCode().value(), e);
        }
        if (e instanceof ResourceAccessException) {
            return new ClientException("External service unreachable: " + e.getMessage(), null, e);
        }
        return new ClientException("Invalid response from external service: " + e.getMessage(), null, e);
    }
}
