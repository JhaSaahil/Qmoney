package com.example.saahiljha.qmoney.portfolio;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {
  public RestTemplate restTemplate;
  public static final String TOKEN = "742d992a016d41e5b66a9e6306739eea38a393e1";
  public StockQuotesService stockQuotesService;

  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }

  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
    List<Candle> result = stockQuotesService.getStockQuote(symbol, from, to);
    if (result == null) {
      return new ArrayList<>();
    }
    return result;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    return new String("https://api.tiingo.com/tiingo/daily/" + symbol
        + "/prices?startDate=" + startDate + "&endDate=" + endDate + "&token="
        + TOKEN);
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    double totalReturn = (sellPrice - buyPrice) / buyPrice;

    double totalYears = (double) (ChronoUnit.DAYS.between(trade.getPurchaseDate(),
        endDate)) / 365.24;

    double annualizedReturns = Math.pow((1 + totalReturn), (1 / totalYears)) - 1;

    return new AnnualizedReturn(trade.getSymbol(), annualizedReturns, totalReturn);
  }

  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trades,
      LocalDate endDate) throws StockQuoteServiceException, JsonProcessingException {

    LocalDate startDate = trades.getPurchaseDate();

    if (startDate.compareTo(endDate) >= 0) {
      throw new RuntimeException();
    }

    List<Candle> tiingoCandles = getStockQuote(trades.getSymbol(), startDate, endDate);

    Double openPrice = tiingoCandles.get(0).getOpen();
    Double closePrice = tiingoCandles.get(tiingoCandles.size() - 1).getClose();
    return calculateAnnualizedReturns(endDate, trades, openPrice, closePrice);
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> trades,
      LocalDate endDate) throws StockQuoteServiceException, JsonProcessingException {

    System.out.println("EndDate : " + endDate);
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();

    for (int i = 0; i < trades.size(); i++) {
      LocalDate startDate = trades.get(i).getPurchaseDate();

      if (startDate.compareTo(endDate) >= 0) {
        throw new RuntimeException();
      }

      List<Candle> tiingoCandles = getStockQuote(trades.get(i).getSymbol(), startDate, endDate);

      if (tiingoCandles != null) {
        Double openPrice = tiingoCandles.get(0).getOpen();
        Double closePrice = tiingoCandles.get(tiingoCandles.size() - 1).getClose();
        annualizedReturns.add(calculateAnnualizedReturns(endDate, trades.get(i),
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

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturnParallel(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate, int numThreads) throws InterruptedException, StockQuoteServiceException {
    // TODO Auto-generated method stub
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
    List<Future<AnnualizedReturn>> reponses = new ArrayList<>();

    for (PortfolioTrade portfolioTrade : portfolioTrades) {
      Callable<AnnualizedReturn> callableTask = () -> {
        return getAnnualizedReturn(portfolioTrade, endDate);
      };
      Future<AnnualizedReturn> reponse = executorService.submit(callableTask);
      reponses.add(reponse);
    }

    for (int i = 0; i < portfolioTrades.size(); i++) {
      Future<AnnualizedReturn> reponse = reponses.get(i);

      try {
        AnnualizedReturn annualizedReturn = reponse.get();
        annualizedReturns.add(annualizedReturn);
      } catch (ExecutionException e) {
        throw new StockQuoteServiceException("Error when calling the API", e);
      }
    }
    Collections.sort(annualizedReturns);
    return annualizedReturns;
  }
}
