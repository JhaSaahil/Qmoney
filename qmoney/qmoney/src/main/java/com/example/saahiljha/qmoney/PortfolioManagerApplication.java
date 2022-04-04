
package com.example.saahiljha.qmoney;

import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication {

  public static final String TOKEN = "742d992a016d41e5b66a9e6306739eea38a393e1";

  public static String getToken() {
    return TOKEN;
  }

  public static String readFileAsString(String file) throws URISyntaxException, IOException {
    return new String(Files.readAllBytes(resolveFileFromResources(file).toPath()));
  }

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    String file = args[0];
    String contents = readFileAsString(file);
    ObjectMapper om = getObjectMapper();
    PortfolioTrade[] portfolioTrades = om.readValue(contents, PortfolioTrade[].class);

    List<String> symbol = new ArrayList<>();
    for (int i = 0; i < portfolioTrades.length; i++) {
      symbol.add(portfolioTrades[i].getSymbol());
    }

    return symbol;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename)
        .toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/saahilkumarjha-ME_QMONEY_V2/"
        + "qmoney/bin/test/assessments/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5b8dfcc1";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "19";

    return Arrays.asList(new String[] { valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace });
  }

  public static List<String> mainReadQuotes(String[] args)
      throws IOException, URISyntaxException, NullPointerException {
    String file = args[0];
    List<PortfolioTrade> trades = readTradesFromJson(file);

    ArrayList<TotalReturnsDto> totalReturnsDtos = new ArrayList<TotalReturnsDto>();

    for (int i = 0; i < trades.size(); i++) {
      String url = prepareUrl(trades.get(i), LocalDate.parse(args[1]), TOKEN);
      TiingoCandle[] tiingoCandle = new RestTemplate().getForObject(url, TiingoCandle[].class);
      totalReturnsDtos
          .add(new TotalReturnsDto(trades.get(i).getSymbol(),
              tiingoCandle[tiingoCandle.length - 1].getClose()));
    }

    Collections.sort(totalReturnsDtos);

    List<String> symbol = new ArrayList<>();
    for (int i = 0; i < totalReturnsDtos.size(); i++) {
      symbol.add(totalReturnsDtos.get(i).getSymbol());
    }

    return symbol;
  }

  public static List<PortfolioTrade> readTradesFromJson(String filename)
      throws IOException, URISyntaxException {
    String contents = readFileAsString(filename);
    ObjectMapper om = getObjectMapper();
    List<PortfolioTrade> portfolioTrades = om.readValue(contents,
        new TypeReference<List<PortfolioTrade>>() {
        });
    return portfolioTrades;
  }

  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    return new String("https://api.tiingo.com/tiingo/daily/" + trade.getSymbol()
        + "/prices?startDate=" + trade.getPurchaseDate() + "&endDate=" + endDate + "&token="
        + token);
  }

  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size() - 1).getClose();
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = prepareUrl(trade, endDate, token);
    TiingoCandle[] candles = new RestTemplate().getForObject(url, TiingoCandle[].class);
    List<Candle> candle = new ArrayList<>();

    for (int i = 0; i < candles.length; i++) {
      candle.add(candles[i]);
    }

    return candle;
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException, NullPointerException {

    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();

    for (int i = 0; i < trades.size(); i++) {
      String url = prepareUrl(trades.get(i), LocalDate.parse(args[1]), TOKEN);

      LocalDate startDate = trades.get(i).getPurchaseDate();

      if (startDate.compareTo(LocalDate.parse(args[1])) >= 0) {
        throw new RuntimeException();
      }

      TiingoCandle[] tiingoCandles = new RestTemplate().getForObject(url, TiingoCandle[].class);
      if (tiingoCandles != null) {
        Double openPrice = tiingoCandles[0].getOpen();
        Double closePrice = tiingoCandles[tiingoCandles.length - 1].getClose();
        annualizedReturns.add(calculateAnnualizedReturns(LocalDate.parse(args[1]), trades.get(i),
            openPrice, closePrice));
      } else {
        annualizedReturns.add(new AnnualizedReturn(trades.get(i).getSymbol(),
            Double.NaN, Double.NaN));
      }
    }

    Collections.sort(annualizedReturns);

    for (int i = 0; i < annualizedReturns.size(); i++) {
      System.out.println("Annualized Return : " + annualizedReturns.get(i).getAnnualizedReturn());
    }

    return annualizedReturns;
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    double totalReturn = (sellPrice - buyPrice) / buyPrice;

    double totalYears = (double) (ChronoUnit.DAYS.between(trade.getPurchaseDate(),
        endDate)) / 365.24;

    double annualizedReturns = Math.pow((1 + totalReturn), (1 / totalYears)) - 1;

    return new AnnualizedReturn(trade.getSymbol(), annualizedReturns, totalReturn);
  }

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
    LocalDate endDate = LocalDate.parse(args[1]);
    List<PortfolioTrade> portfolioTrades = readTradesFromJson(args[0]);
    PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(new RestTemplate());
    return portfolioManager.calculateAnnualizedReturn(portfolioTrades, endDate);
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}
