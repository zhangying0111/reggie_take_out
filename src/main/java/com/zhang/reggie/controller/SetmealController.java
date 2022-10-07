package com.zhang.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhang.reggie.common.R;
import com.zhang.reggie.dto.DishDto;
import com.zhang.reggie.dto.SetmealDto;
import com.zhang.reggie.entity.Category;
import com.zhang.reggie.entity.Setmeal;
import com.zhang.reggie.entity.SetmealDish;
import com.zhang.reggie.service.CategoryService;
import com.zhang.reggie.service.SetmealDishService;
import com.zhang.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;
    /**
     * 保存套餐信息
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true) //表示删除这个分类下的所有数据
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息 : {}",setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("保存套餐信息成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //构造分页器对象
        Page<Setmeal> pageInfo = new Page<>();

        Page<SetmealDto> setmealDtoPageInfo = new Page<>();

        //构造条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null,Setmeal::getName,name);
        //构造排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);

        /**
         * 问题：页面显示不出setmeal的分类name
         * 原因：setmeal里面只有categoryId，而没有categoryName，也没有categoryName这一属性。
         * 解决方法：正常查询，分页，然后处理。
         * 处理办法：利用setmealDto对象，用setmeal里面的categoryId去category表里面查询对应的名称赋值到setmealDto里面
         */
        //将分页的一些数据复制过去，但是具体页面的records要进行具体处理
        BeanUtils.copyProperties(pageInfo,setmealDtoPageInfo,"records");
        List<Setmeal> setmealRecords = pageInfo.getRecords();
        //将setmealRecords里面的值赋值到setmealDto里面
        List<SetmealDto> SetmealDtoRecords = setmealRecords.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);//将页面数据放到setmealDto里面
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null){
                setmealDto.setCategoryName(category.getName());
            }

            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPageInfo.setRecords(SetmealDtoRecords);
        return R.success(setmealDtoPageInfo);
    }

    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> remove(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功");
    }

    /**
     * 根据条件查询套餐数据
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    //注意这理Setmeal setmeal 不需要jia@RequestBody 因为前端传过来的参数不是json数据，而是键值对
    public R<List<Setmeal>> list( Setmeal setmeal){
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //构造查询条件
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        //构造排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //查询 ->根据条件把该categoryId下对应的套餐封装成对象放进集合里面返回，前端页面接收的返回信息就是该categoryId下的集合，里面是一个一哥套餐的具体信息
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);

    }
}
