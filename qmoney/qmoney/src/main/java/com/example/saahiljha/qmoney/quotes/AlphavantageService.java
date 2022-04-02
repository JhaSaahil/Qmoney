
package com.example.saahiljha.qmoney.dto;

import com.crio.warmup.stock.dto.AlphavantageCandle;
import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class AlphavantageService implements StockQuotesService {

  public RestTemplate restTemplate;
  public static final String TOKEN = "OZTV4DZ8OL37EDXC";

  protected AlphavantageService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected String buildUri(String symbol) {
    return new String("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + symbol
        + "&apikey=" + TOKEN);
  }

  private Comparator<Candle> getComparator() {
    return Comparator.comparing(Candle::getDate);
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
    try {
      String response = restTemplate.getForObject(buildUri(symbol), String.class);
      System.out.println(response);
      ObjectMapper om = new ObjectMapper();
      om.registerModule(new JavaTimeModule());

      AlphavantageDailyResponse result = om.readValue(response, AlphavantageDailyResponse.class);

      List<Candle> Candle = new ArrayList<>();

      for (Entry<LocalDate, AlphavantageCandle> entry : result.getCandles().entrySet()) {
        entry.getValue().setDate(entry.getKey());
        if ((entry.getKey().isEqual(from) || entry.getKey().isEqual(to))
            || (entry.getKey().isAfter(from) && entry.getKey().isBefore(to))) {
          Candle.add(entry.getValue());
        }
      }

      Collections.sort(Candle, getComparator());

      return Candle;

    } catch (Exception e) {
      throw new StockQuoteServiceException("Too Many Requests");
    }
  }

}
