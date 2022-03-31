package com.example.saahiljha.qmoney;

import java.util.Comparator;

import com.example.saahiljha.qmoney.dto.TotalReturnsDto;

public class ClosePriceComparator implements Comparator<TotalReturnsDto> {
  @Override
  public int compare(TotalReturnsDto symbol1, TotalReturnsDto symbol2) {
    if (symbol1.getClosingPrice() == symbol2.getClosingPrice())
      return 0;
    else if (symbol1.getClosingPrice() < symbol1.getClosingPrice())
      return 1;
    else
      return -1;
  }
}