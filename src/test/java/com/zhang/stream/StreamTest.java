package com.zhang.stream;

import com.zhang.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamTest {
    /**
     * 测试stream流的中间管道--map
     * map---将已有元素转换成另一个对象类型，一对一逻辑，返回新的stream流
     */
    @Test
    public void test1() {
        List<User> usersList = Arrays.asList(
                new User(1, "jack", 1),
                new User(2, "marry", 1),
                new User(3, "tom", 3)
        );

        //自定义需求，将所有人的工资变成5000
        List<User> collect = usersList.stream().peek(
                (user) -> {
                    user.setSalary(9000);

                }).collect(Collectors.toList());
        System.out.println(collect);
    }
}
