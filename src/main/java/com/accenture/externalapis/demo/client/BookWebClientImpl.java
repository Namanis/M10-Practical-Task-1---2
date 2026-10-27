package com.accenture.externalapis.demo.client;

import com.accenture.externalapis.demo.config.ExternalServiceProperties;
import com.accenture.externalapis.demo.dto.BookDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class BookWebClientImpl implements BookWebClient{

    private WebClient webClient;

    public BookWebClientImpl(WebClient.Builder builder, ExternalServiceProperties properties) {

    }

    @Override
    public Mono<BookDto> getBookAsync(Long id) {
        throw new UnsupportedOperationException("Not implemented yet - part 2");
    }

    @Override
    public Flux<BookDto> getAllBooksAsync() {
        throw new UnsupportedOperationException("Not implemented yet - part 2");
    }

    @Override
    public Mono<List<BookDto>> getBooksInParallel(Long id1, Long id2) {
        throw new UnsupportedOperationException("Not implemented yet - part 2");
    }

}
