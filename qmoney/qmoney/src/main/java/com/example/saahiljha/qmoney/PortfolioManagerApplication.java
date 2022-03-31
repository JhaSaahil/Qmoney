package com.example.saahiljha.qmoney;

import com.example.saahiljha.qmoney.dto.*;
import com.example.saahiljha.qmoney.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.logging.log4j.ThreadContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication {

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
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/saahilkumarjha-ME_QMONEY_V2/qmoney/bin/test/assessments/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5b8dfcc1";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "19";

    return Arrays.asList(new String[] { valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
        functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace });
  }

  public static List<String> mainReadQuotes(String[] args)
      throws IOException, URISyntaxException, NullPointerException {
    String file = args[0];
    List<PortfolioTrade> trades = readTradesFromJson(file);

    String date = args[1];
    LocalDate endDate = LocalDate.parse(date);

    String token = "742d992a016d41e5b66a9e6306739eea38a393e1";

    ArrayList<TotalReturnsDto> totalReturnsDtos = new ArrayList<TotalReturnsDto>();

    for (int i = 0; i < trades.size(); i++) {
      String url = prepareUrl(trades.get(i), endDate, token);
      TiingoCandle[] tiingoCandle = new RestTemplate().getForObject(url, TiingoCandle[].class);
      totalReturnsDtos
          .add(new TotalReturnsDto(trades.get(i).getSymbol(), tiingoCandle[tiingoCandle.length - 1].getClose()));
    }

    Collections.sort(totalReturnsDtos);

    List<String> symbol = new ArrayList<>();
    for (int i = 0; i < totalReturnsDtos.size(); i++) {
      symbol.add(totalReturnsDtos.get(i).getSymbol());
    }

    return symbol;
  }

  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    String contents = readFileAsString(filename);
    ObjectMapper om = getObjectMapper();
    List<PortfolioTrade> portfolioTrades = om.readValue(contents, new TypeReference<List<PortfolioTrade>>() {
    });
    return portfolioTrades;
  }

  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    return new String("https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="
        + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token);
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadQuotes(args));
  }
}
