package com.zhang.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhang.reggie.dto.DishDto;
import com.zhang.reggie.entity.Dish;
import com.zhang.reggie.entity.DishFlavor;
import com.zhang.reggie.mapper.DishMapper;
import com.zhang.reggie.service.DishFlavorService;
import com.zhang.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    public DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存相对应的口味
     *
     * @param dishDto
     */
    @Transactional //涉及多张表操作，添加事务控制  保证数据一致性
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品基本信息到dish表
        this.save(dishDto);

        //获取dishDto中的dishId
        Long dishId = dishDto.getId();//上面操作完就已经获得id，此时获取的是继承自dish中的id，也就是dishId
        //将dishId放到dishFlavor中
        List<DishFlavor> flavors = dishDto.getFlavors();
        //利用stream流将获取的dishId放到集合中
        flavors = flavors.stream().map((item)
                -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味数据到菜品口味表
        dishFlavorService.saveBatch(flavors); //saveBath批量保存，保存的是集合
    }

    /**
     * 根据id查询菜品新和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(long id) {
        DishDto dishDto = new DishDto();
        //1、查询菜品基本信息
        Dish dish = this.getById(id);

        BeanUtils.copyProperties(dish,dishDto);
        //2、查询菜品口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        //单独设置flavor
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    /**
     * 更新/修改菜品信息 同时更新口味信息
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //1、在dish表中修改菜品信息
        this.updateById(dishDto);
        //2、在dish_flavor表中修改口味信息
        //1）清理当前菜品口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());//DishDto继承Dish，这里的dishDto.getId()拿到的就是dishId
        dishFlavorService.remove(queryWrapper);

        //2) 添加当前提交过来的菜品口味 和 save一个操作
        /**
         * 这里注意，进行save口味操作的时候。 封装过来的数据只有 name 和 value (eg: 辣度 ： 中辣，麻辣...)
         * 但是并不知道这是哪一个dish对应的数据，也就是缺少了的该口味对应的dish，在dish_flavor表中所缺少的就是该口味的dish_id
         *
         * 所以，需要将dish_id从dishDto中取出来放进dish_flavor中
         */
        List<DishFlavor> flavor = dishDto.getFlavors();
        //此时的flavor中只有name和value，没有dish_id
        flavor.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavor);
    }
}

