package com;

import com.core.service.MenuService;

public class FinanceControl {

    public static void main(String[] args) {
        MenuService menuService = new MenuService();
        menuService.start();
    }
}
