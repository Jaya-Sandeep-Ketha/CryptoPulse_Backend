package com.sandy.cryptopulse.CryptoPulse.controller;

import com.sandy.cryptopulse.CryptoPulse.domain.OrderType;
import com.sandy.cryptopulse.CryptoPulse.model.Coin;
import com.sandy.cryptopulse.CryptoPulse.model.User;
import com.sandy.cryptopulse.CryptoPulse.model.Order;
import com.sandy.cryptopulse.CryptoPulse.response.CreateOrderRequest;
import com.sandy.cryptopulse.CryptoPulse.service.CoinService;
import com.sandy.cryptopulse.CryptoPulse.service.OrderService;
import com.sandy.cryptopulse.CryptoPulse.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userSerivce;
    @Autowired
    private CoinService coinService;
//    @Autowired
//    private WalletTransactionService walletTransactionService;

//    @Autowired
//    public OrderController(OrderService orderService, UserService userSerivce) {
//        this.orderService = orderService;
//        this.userSerivce=userSerivce;
//    }

    @PostMapping("/pay")
    public ResponseEntity<Order> payOrderPayment(@RequestHeader("Authorization") String jwt, @RequestBody CreateOrderRequest req) throws Exception {
        User user = userSerivce.findUserProfileByJwt(jwt);
        Coin coin =coinService.findById(req.getCoinId());
        Order order = orderService.processOrder(coin,req.getQuantity(),req.getOrderType(),user);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@RequestHeader("Authorization") String jwtToken, @PathVariable Long orderId) throws Exception {
        User user = userSerivce.findUserProfileByJwt(jwtToken);
        Order order = orderService.getOrderById(orderId);
        if (order.getUser().getId()==(user.getId())) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();        }
    }

    @GetMapping()
    public ResponseEntity<List<Order>> getAllOrdersForUser(
            @RequestHeader("Authorization") String jwtToken,
            @RequestParam(required = false) OrderType order_type,
            @RequestParam(required = false) String asset_symbol
    ) throws Exception {
        if (jwtToken == null) {
            throw new Exception("token missing...");
        }
        Long userId = userSerivce.findUserProfileByJwt(jwtToken).getId();
        List<Order> userOrders = orderService.getAllOrdersOfUser(userId,order_type,asset_symbol);
        return ResponseEntity.ok(userOrders);
    }
}
