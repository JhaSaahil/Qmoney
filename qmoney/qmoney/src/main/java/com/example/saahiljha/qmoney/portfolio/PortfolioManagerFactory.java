package com.example.saahiljha.qmoney.dto;

import com.crio.warmup.stock.quotes.StockQuoteServiceFactory;
import com.crio.warmup.stock.quotes.StockQuotesService;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerFactory {

  public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {

    return new PortfolioManagerImpl(restTemplate);
  }

  public static PortfolioManager getPortfolioManager(String provider,
      RestTemplate restTemplate) {
    StockQuoteServiceFactory stockQuotesServiceFactory = StockQuoteServiceFactory.INSTANCE;
    StockQuotesService stockQuotesService = stockQuotesServiceFactory.getService(provider, restTemplate);
    return new PortfolioManagerImpl(stockQuotesService);
  }

}
