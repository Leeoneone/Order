package com.aaa.lee.app.mapper;

import com.aaa.lee.app.domain.Product;
import com.aaa.lee.app.domain.Sku;
import tk.mybatis.mapper.common.Mapper;

public interface SkuMapper extends Mapper<Sku> {
    Sku selectskuById(Long productId);
}