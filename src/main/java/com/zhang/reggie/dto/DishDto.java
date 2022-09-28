package com.zhang.reggie.dto;

import com.zhang.reggie.entity.Dish;
import com.zhang.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

//DTO Data Transfer Object
@Data
public class DishDto extends Dish {//继承了Dish 继承了Dish里面的熟悉 然后扩展了其他的属性

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
