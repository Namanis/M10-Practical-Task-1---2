package com.accenture.externalapis.demo.client;

import com.accenture.externalapis.demo.config.ExternalServiceProperties;
import com.accenture.externalapis.demo.dto.BookApiResponse;
import com.accenture.externalapis.demo.dto.BookDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
public class BookWebClientImpl implements BookWebClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);
    private final WebClient webClient;

    public BookWebClientImpl(WebClient.Builder builder, ExternalServiceProperties properties) {
        this.webClient = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.token())
                .build();
    }

    @Override
    public Mono<BookDto> getBookAsync(Long id) {
        return webClient.get()
                .uri("/books/{id}", id)
                .retrieve()
                .bodyToMono(BookApiResponse.class)
                .switchIfEmpty(Mono.error(new ClientException("Empty response for book " + id)))
                .map(this::toDto)
                .timeout(TIMEOUT)
                .retry(3)
                .onErrorMap(e -> !(e instanceof ClientException), this::toClientException);
    }

    @Override
    public Flux<BookDto> getAllBooksAsync() {
        return webClient.get()
                .uri("/books")
                .retrieve()
                .bodyToFlux(BookApiResponse.class)
                .map(this::toDto)
                .timeout(TIMEOUT)
                .onErrorMap(e -> !(e instanceof ClientException), this::toClientException);
    }

    @Override
    public Mono<List<BookDto>> getBooksInParallel(Long id1, Long id2) {
        Mono<BookDto> book1 = getBookAsync(id1);
        Mono<BookDto> book2 = getBookAsync(id2);
        return Mono.zip(book1, book2)
                .map(books -> List.of(
                        books.getT1(),
                        books.getT2()));
    }

    private BookDto toDto(BookApiResponse response) {
        return new BookDto(
                response.id(),
                response.title(),
                response.author(),
                response.price()
        );
    }

    private ClientException toClientException(Throwable e) {
        if (e instanceof WebClientResponseException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                return new ClientException("Request rejected: " + ex.getStatusCode(),
                        ex.getStatusCode().value(), e);
            }
            return new ClientException("External service error: " + ex.getStatusCode(),
                    ex.getStatusCode().value(), e);
        }
        if (e instanceof WebClientRequestException) {
            return new ClientException("External service unreachable: " + e.getMessage(), e);
        }
        if (e instanceof TimeoutException) {
            return new ClientException("External service did not respond within " + TIMEOUT.toSeconds() + "s", e);
        }
        return new ClientException("Invalid response from external service: " + e.getMessage(), e);
    }
}
