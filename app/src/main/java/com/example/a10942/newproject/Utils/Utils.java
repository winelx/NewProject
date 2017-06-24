package com.example.a10942.newproject.Utils;


import java.util.regex.Pattern;

/**
 * Created by 10942 on 2017/6/22 0022.
 */

public class Utils {

    /*
      * 判断是否为整数
      * @param str 传入的字符串
      * @return 是整数返回true,否则返回false
    */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

}
