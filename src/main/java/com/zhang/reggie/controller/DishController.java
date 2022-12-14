package com.zhang.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhang.reggie.common.R;
import com.zhang.reggie.dto.DishDto;
import com.zhang.reggie.entity.Category;
import com.zhang.reggie.entity.Dish;
import com.zhang.reggie.entity.DishFlavor;
import com.zhang.reggie.service.CategoryService;
import com.zhang.reggie.service.DishFlavorService;
import com.zhang.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);
        //新增的时候清理对应的部分的缓存数据
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    //这里需要处理一下 使得页面显示分类的名称
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPageInfo = new Page<>();


        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo, queryWrapper);//传入的是 分页构造器对象 和 条件构造器对象

        //上面查询完之后，page对象里面就有一些数据了，将得到的数据复制到dishDtoPageInfo里面
        // ，除了records，因为里面存放的就是页面需要展示的数据，而这些数据里面是没有categoryName的。
        //需要通过records里面的categoryId去category表里面插到对应的categoryName然后封装到DishDto里面

        BeanUtils.copyProperties(pageInfo, dishDtoPageInfo, "records");
        List<Dish> dishRecords = pageInfo.getRecords();

        List<DishDto> list = dishRecords.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);//将查到的dishRecord里面的数据复制到DishDto里面
            //根据dish表里的categoryId去category表里面查到对应的数据，并且返回封装数据的对象
            Category category = categoryService.getById(item.getCategoryId());
            dishDto.setCategoryName(category.getName());

            return dishDto;//返回的是dishDto

        }).collect(Collectors.toList());
        dishDtoPageInfo.setRecords(list);

        return R.success(dishDtoPageInfo);

    }

    /**
     * 根据菜品id查询菜品信息和对应的口味信息 ---》用来页面数据回显
     *
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public R<DishDto> get(@PathVariable long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);

    }

    /**
     * 更新/修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        //更新/修改的时候清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*"); //所有以dish_开头的key
        //redisTemplate.delete(keys);

        //清理某个分类下的菜品缓存数据  清理一部分
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("菜品信息更新成功");
    }

    /**
     * 根据categoryId获取该种类下的菜品列表
     * @param dish
     * @return
     */
   /* @GetMapping("list")
    public R<List<Dish>> list(Dish dish){
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //构造查询条件
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1(起售)的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //构造排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //根据条件查询与种类对应的菜品信息
        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);

    }*/

    /**
     * 目的：让有口味的菜品能够显示对应的按钮标签
     * @param dish
     * @return
     */
    @GetMapping("list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList =null;
        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        //先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtoList != null){//有则直接返回
            return R.success(dishDtoList);
        }

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //构造查询条件
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1(起售)的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //构造排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //根据条件查询与种类对应的菜品信息
        List<Dish> list = dishService.list(queryWrapper);
        dishDtoList = list.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            //根据id查询分类对象
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null){
                dishDto.setCategoryName(category.getName());
            }

            //将口味属性赋值
            Long dishId = item.getId();//在Dish里面得到dishId去flavor表获得对应dish下面的口味
            //根据条件 （dish_falvor表里的dishId与item里的dishId相对应），得到与之对应的口味信息
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId,dishId);
            //将对应菜品id下的口味信息封装成集合对象
            List<DishFlavor> dishFlavorsList = dishFlavorService.list(wrapper);
            dishDto.setFlavors(dishFlavorsList);
            return dishDto;

        }).collect(Collectors.toList());

        //如果不存在，需要查询数据库，将查询到的数据缓存到redis里
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);//设置过期时间为60分钟
        return R.success(dishDtoList);

    }
}

