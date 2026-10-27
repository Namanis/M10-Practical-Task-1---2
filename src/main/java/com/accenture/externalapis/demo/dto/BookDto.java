package com.accenture.externalapis.demo.dto;

import java.math.BigDecimal;

public record BookDto(Long id,
                      String title,
                      String author,
                      BigDecimal price) {
}