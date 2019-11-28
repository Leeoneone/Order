package com.aaa.lee.app.mapper;

import com.aaa.lee.app.domain.OrderCart;

import com.aaa.lee.app.domain.Product;
import com.aaa.lee.app.domain.Sku;
import org.apache.ibatis.annotations.Param;

import tk.mybatis.mapper.common.Mapper;
import java.util.List;

public interface OrderCartMapper extends Mapper<OrderCart> {
 List<OrderCart> selectCart(@Param("id") Long id,@Param("memberId") Long memberId);
 /*查询*/
 List<OrderCart> selectmembercart(OrderCart voCar);
/* *//*删除*//*
 Integer deletecart(@Param("productId") Long productId);*/
 /*修改*/
 Integer updatecart(OrderCart voCart2);
 /*修改删除状态码*/
 Integer updeletestatus(OrderCart upDeletePo);
 /*新增*/
Integer  addCartPro(OrderCart CartPro);
/*通过用户id和店铺id清除购物车*/
 Integer clearCart(OrderCart cartclear);
}