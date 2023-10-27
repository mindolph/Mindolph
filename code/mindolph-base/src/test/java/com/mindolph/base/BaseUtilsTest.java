package com.mindolph.base;

import org.apache.commons.lang3.CharUtils;

/**
 * @author mindolph
 */
public class BaseUtilsTest {

    public static void main(String[] args) {
        System.out.println(CharUtils.isAsciiPrintable(' '));
        System.out.println(CharUtils.isAsciiPrintable('\t'));
        System.out.println(CharUtils.isAsciiPrintable('\r'));
    }


}
