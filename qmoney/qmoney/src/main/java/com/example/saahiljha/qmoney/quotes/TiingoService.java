package com.example.saahiljha.qmoney.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {
  public RestTemplate restTemplate;
  public static final String TOKEN = "742d992a016d41e5b66a9e6306739eea38a393e1";

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    return new String("https://api.tiingo.com/tiingo/daily/" + symbol
        + "/prices?startDate=" + startDate + "&endDate=" + endDate + "&token="
        + TOKEN);
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
    try {
      String response = restTemplate.getForObject(buildUri(symbol, from, to), String.class);
      ObjectMapper om = new ObjectMapper();
      om.registerModule(new JavaTimeModule());

      Candle[] result = om.readValue(response, TiingoCandle[].class);
      if (result == null) {
        return new ArrayList<>();
      }
      return Arrays.asList(result);

    } catch (Exception e) {
      throw new StockQuoteServiceException("Invalid data");
    }
  }

}
