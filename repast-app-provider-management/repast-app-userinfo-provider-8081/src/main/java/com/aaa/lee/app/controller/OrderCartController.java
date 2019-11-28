package com.aaa.lee.app.controller;





import com.aaa.lee.app.domain.OrderCart;
import com.aaa.lee.app.service.OrderCartService;
import com.aaa.lee.app.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author leeoneone
 * @date 2019/11/23 0023 15:36
 */
@RestController
public class OrderCartController {
    @Autowired
    private RedisService redisService;
    @Autowired
    private OrderCartService orderCartService;

    @GetMapping("/order/selectcartoperation")
    public List<OrderCart> ordercart(Long id) {
        return orderCartService.ordercart(id, redisService);
    }

    @PostMapping("/order/addcartoperation")
    Boolean addcart(@RequestBody Map<String,Object> data1) {
       ArrayList cart = new ArrayList();
      Map cartMap = new HashMap();
      //b为商品id
     long b = 3L;
     //c为店铺id
     long c = 3L;
     cartMap.put("quantity",0);
     cartMap.put("productId",b);
     cartMap.put("shopId",c);
     cart.add(0,cartMap);
     data1.put("cart",cart);
     Boolean addcart = orderCartService.addcart(data1, redisService);
        return addcart;
    }
    /*清空购物车*/

    @GetMapping("/order/clearcart")
    public  Boolean clearcart(Long shopId){
        return   orderCartService.clearcart(shopId,redisService);
    }
}
