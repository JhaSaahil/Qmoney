
package com.example.saahiljha.qmoney.dto;

import org.springframework.web.client.RestTemplate;

public enum StockQuoteServiceFactory {

  INSTANCE;

  public StockQuotesService getService(String provider, RestTemplate restTemplate) {
    if ("tiingo".equalsIgnoreCase(provider)) {
      return new TiingoService(restTemplate);
    }
    return new AlphavantageService(restTemplate);
  }
}
