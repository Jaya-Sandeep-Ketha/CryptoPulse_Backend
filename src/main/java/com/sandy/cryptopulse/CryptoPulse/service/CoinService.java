package com.sandy.cryptopulse.CryptoPulse.service;

import com.sandy.cryptopulse.CryptoPulse.model.Coin;

import java.util.List;

@Se
public interface CoinService {
    List<Coin> getCoinList(int page);
    String getMarketChart(String coinId, int days);
    String getCoinDetails(String coinId);
    Coin findById(String coinId) throws Exception;
    String searchCoin(String keyWord);
    String getTop50CoinsByMarketCapRank();
    String getTrendingCoins();
}
