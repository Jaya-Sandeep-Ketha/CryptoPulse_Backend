package com.sandy.service;

import com.sandy.model.CoinDTO;
import com.sandy.response.ApiResponse;

public interface ChatBotService {
    ApiResponse getCoinDetails(String coinName);

    CoinDTO getCoinByName(String coinName);

    String simpleChat(String prompt);

//    String processPromptAndGenerateResponse(String prompt);
}
