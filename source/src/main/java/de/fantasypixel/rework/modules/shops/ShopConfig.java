package de.fantasypixel.rework.modules.shops;

import de.fantasypixel.rework.framework.config.ConfigProvider;
import lombok.Getter;

@Getter
@ConfigProvider(path = "config/shop")
public class ShopConfig {

    private String shopDiscountNameFormat;

}
