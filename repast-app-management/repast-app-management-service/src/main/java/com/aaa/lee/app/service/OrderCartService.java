package com.aaa.lee.app.service;
import com.aaa.lee.app.base.BaseService;
import com.aaa.lee.app.domain.*;
import com.aaa.lee.app.mapper.OrderCartMapper;
import com.aaa.lee.app.mapper.PComMapper;
import com.aaa.lee.app.mapper.ProductMapper;
import com.aaa.lee.app.mapper.SkuMapper;
import com.aaa.lee.app.utils.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.GetMapping;
import tk.mybatis.mapper.common.Mapper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import static com.aaa.lee.app.staticstatus.StaticProperties.REDIS_KEY;
/**
 * @author leeoneone
 * @date 2019/11/23 0023 14:09
 */
@Service
public class OrderCartService extends BaseService<OrderCart> {
    /*注入购物车表*/
    @Autowired
    private OrderCartMapper orderCartMapper;
    /*pms_sku_stock sku的库存表
    * 外键:
	*shop_id(店铺id),product_id(商品id)
	* 通过商品id获取购物车属性：sp1，sp2，sp3，sku_code*/
    @Autowired
    private SkuMapper skuMapper;
    /*注入商品表pms_product*/
    @Autowired
    private ProductMapper productMapper;
    /*注入pms_comment 商品评价表*/
    @Autowired
    private PComMapper pComMapper;


    @Override
    public Mapper<OrderCart> getMapper() {
        return orderCartMapper;
    }

    /*购物车展示
    * */
    public List<OrderCart> ordercart(Long id, RedisService redisService){
        /*从前台获取到商品的id和数量*/
        //店铺id
     /* Long shopId = Long.valueOf(data.get("id"));*/
      //商品数量
     /* Integer quantity = valueOf(data.get("quantity"));*/

        /*从redis中获取登录用户的id*/
        String getMemberInfos = redisService.get(REDIS_KEY);
        Member member = JSONUtil.toObject(getMemberInfos, Member.class);
        /*用户id*/
        Long memberId = member.getId();
        /*根据店铺id和用户id查询购物车信息*/
        List<OrderCart> receiveList = orderCartMapper.selectCart(id, memberId);
        if (receiveList.size()>0){
            return receiveList;
        }else {
            return null;
        }
    }
    /*添加数据到购物车
    * 设置0为未删除
    *    1为删除
    * */
  @Transactional
  public Boolean addcart(Map<String ,Object> data1,RedisService redisService) {
     /*从前台获取商品信息*/
     Object cart =data1.get("cart");
     String carts = JSONUtil.toJsonString(cart);
     List<Map> mapListcart = JSONUtil.toList(carts, Map.class);
     /*获取缓存中的用户id*/
     String Memberinfo = redisService.get(REDIS_KEY);
     Member member = JSONUtil.toObject(Memberinfo, Member.class);
     Long memberId = member.getId();
     String nickname = member.getNickname();
      if (mapListcart.size()>0){
          try {
         for (Map<String, Object> car : mapListcart) {
//          获取商品的数量以及商品id
             Object quantity1 = car.get("quantity");
             String quantity2 = JSONUtil.toJsonString(quantity1);
             int quantity = Integer.parseInt(quantity2);
             Object productId1 = car.get("productId");
             String productId2 = JSONUtil.toJsonString(productId1);
             long productId = Long.parseLong(productId2);
             Object shopId1 = car.get("shopId");
             String shopId2 = JSONUtil.toJsonString(shopId1);
             long shopId = Long.parseLong(shopId2);
             OrderCart voCar = new OrderCart();
             voCar.setProductId(shopId)
                     .setMemberId(memberId);
             List<OrderCart> selectmembercart = orderCartMapper.selectmembercart(voCar);
             //判断前台发送的数量信息
             if (0 != quantity) {
                 //前台发送数量不为0
                 if (0 != selectmembercart.size()) {
                     /*如果有商品则就进行修改数量*/
                     OrderCart voCart1 = new OrderCart();
                     voCart1.setProductId(productId)
                             .setMemberId(memberId)
                             .setQuantity(quantity)
                             .setModifyDate(date());
                     Integer updatecart = orderCartMapper.updatecart(voCart1);
                     if (updatecart > 0) return true;
                 } else {
                     OrderCart CartPro= new OrderCart();
                     /*如果没有商品则就进行新增
                     * 放进去商品id，用户id（缓存），用户昵称（缓存），数量，价格，创建时间*/
                     /*通过主键和店铺，1 查询商品表*/
                     Product product = productMapper.selectProductById(productId);
                     /*通过店铺id和商品id查询库存*/
                     Sku sku = skuMapper.selectskuById(productId);
                     /*通过店铺id和商品id查询评论表获取商品销售属性值ProductAttr*/
                     PCom pComAttr = pComMapper.selectPcomById(productId);
                     System.out.println(pComAttr);
                     /*把获取到的属性放进购物车实体类中，把这些属性添加到购物车表中*/
                     CartPro.setProductId(productId)
                             .setProductSkuId(sku.getId())
                             .setMemberId(memberId)
                             .setShopId(shopId)
                             .setQuantity(quantity)
                             .setPrice(product.getPrice())
                             .setSp1(sku.getSp1())
                             .setSp2(sku.getSp2())
                             .setSp3(sku.getSp3())
                             .setProductPic(product.getPic())
                             .setProductName(product.getName())
                             .setProductSubTitle(product.getSubTitle())
                             .setProductSkuCode(sku.getSkuCode())
                             .setMemberNickname(nickname)
                             .setCreateDate(date())
                             .setDeleteStatus(0)
                             .setProductCategoryId(product.getProductCategoryId())
                             .setProductBrand(product.getBrandName())
                             .setProductSn(product.getProductSn())
                             .setProductAttr(pComAttr.getProductAttribute());
                     Integer insertcart = orderCartMapper.addCartPro(CartPro);
                     System.out.println(insertcart);
                     if (insertcart >0)
                         return true;
                 }
             } else {
                 if (0!= selectmembercart.size()){
                     //如果前台发送数量为0，查询数据有数据，则修改删除的状态码
                     OrderCart upDeletePo = new OrderCart();
                     upDeletePo.setDeleteStatus(1);
                     upDeletePo.setModifyDate(date());
                     upDeletePo.setProductId(productId);
                     upDeletePo.setMemberId(memberId);
                     upDeletePo.setQuantity(quantity);
                     Integer updeletestatus = orderCartMapper.updeletestatus(upDeletePo);
                     if (updeletestatus> 0) return true;
             }else {
                 return false;
             }
             }
         }
          }catch (Exception e){
              e.printStackTrace();
              TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();}
         }
     return false;
     }
   /*清空购物车
     * */
    @GetMapping("/order/clearcart")
    public Boolean clearcart(Long shopId,RedisService redisService){
        /*获取缓存中的用户id*/
        String Memberinfo = redisService.get(REDIS_KEY);
        Member member = JSONUtil.toObject(Memberinfo, Member.class);
        Long memberId = member.getId();
        System.out.println(memberId);
        System.out.println(shopId);
        //通过店铺id和会员id修改删除状态码，来实现提交订单的时候，购物车不显示数据
        //在订单完成后如果想要“再来一单”可以通过修改状态码来实现再次购买
        OrderCart cartclear = new OrderCart();
        cartclear.setShopId(shopId);
        cartclear.setMemberId(memberId);
        cartclear.setDeleteStatus(1);
        cartclear.setModifyDate(date());
        Integer integer = orderCartMapper.clearCart(cartclear);
        System.out.println(integer);
        if (integer>0){return true;}
        return false;
    }

    /*日期封装
    * */
    public Date date(){
        Date date1 = new Date();
        String formatDate = null;
        DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //HH表示24小时制；
        formatDate = dFormat.format(date1);
        SimpleDateFormat lsdStrFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date strD = null ;
        try {
            strD = lsdStrFormat.parse(formatDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strD;

    }
 }





