package com.bringholm.naturalsignshop.constants;

import com.google.common.collect.Maps;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Map;

public class Permissions {

    public static final Permission RELOAD_PERMISSION = new Permission("naturalsignshop.reload");
    public static final Permission CREATE_SIGN_PERMISSION = new Permission("naturalsignshop.create");
    public static final Permission BUY_PERMISSION = new Permission("naturalsignshop.buy", PermissionDefault.TRUE);
    public static final Permission SELL_PERMISSION = new Permission("naturalsignshop.sell", PermissionDefault.TRUE);
    public static final Permission USE_SCROLLER_PERMISSION = new Permission("naturalsignshop.scroller", PermissionDefault.TRUE);
    public static final Permission ALL_PERMISSION = new Permission("naturalsignshop.*", getAllPermissions());

    private static Map<String, Boolean> getAllPermissions() {
        Map<String, Boolean> map = Maps.newHashMap();
        map.put(RELOAD_PERMISSION.getName(), true);
        map.put(CREATE_SIGN_PERMISSION.getName(), true);
        map.put(BUY_PERMISSION.getName(), true);
        map.put(SELL_PERMISSION.getName(), true);
        map.put(USE_SCROLLER_PERMISSION.getName(), true);
        return map;
    }
}
