package com.zhang.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhang.reggie.common.BaseContext;
import com.zhang.reggie.common.CustomException;
import com.zhang.reggie.entity.Orders;
import com.zhang.reggie.entity.ShoppingCart;
import com.zhang.reggie.mapper.OrdersMapper;
import com.zhang.reggie.service.OrderDetailService;
import com.zhang.reggie.service.OrdersService;
import com.zhang.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;
    /**
     * 用户下单
     * @param orders
     */
    @Override
    @Transactional //操作多个表 需要进行事务控制
    public void submit(Orders orders) {
        //获得当前用户id
        long userId = BaseContext.getCurrentId();
        //查询当前用户购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);//用户购物车数据
        if (shoppingCartList == null || shoppingCartList.size() == 0){
            throw  new CustomException("购物车为空，不能下单");
        }
        //向订单表插入数据，一条数据

        //向订单明细表插入数据，多条数据

        //清空购物车
    }
}
