package com.zhang.reggie.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zhang.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersService extends IService<Orders> {
    void submit(Orders orders);
}
