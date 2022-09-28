package com.zhang.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhang.reggie.common.CustomException;
import com.zhang.reggie.dto.SetmealDto;
import com.zhang.reggie.entity.Setmeal;
import com.zhang.reggie.entity.SetmealDish;
import com.zhang.reggie.mapper.SetmealMapper;
import com.zhang.reggie.service.SetmealDishService;
import com.zhang.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     *
     * @param setmealDto
     */
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //1、保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);
        //2、保存套餐和菜品的关联关系，操作setmeal_dish，执行insert操作

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //注意，这里的setmealDishes里面是没有setmealId的，只有dishId，所以需要处理一下
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId()); //上面insert操作之后，框架就会给setmealDto对象赋上值
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);

    }

    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //查询套餐状态，确定是否可以删除 1（在售）不可删除   in() 查询某个范围内的数据
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        //如果不能删除，抛出一个业务异常
        if (count > 0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //如果可以删除，先删除套餐中的数据
        this.removeByIds(ids);
        //删除套餐菜品关系表数据
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);//删除这个范围内的数据
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
    }
}
