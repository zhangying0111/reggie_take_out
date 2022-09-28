package com.zhang.test;

import org.junit.jupiter.api.Test;

public class uploadFileTest {
    @Test
    public void test1() {
        String fileName = "abababa.jpg";
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(suffix);
    }

}
