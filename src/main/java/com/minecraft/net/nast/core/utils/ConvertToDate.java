/**
 * by Steeein
 */

package com.minecraft.net.nast.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ConvertToDate {
    public static String format(String timestamp) {
        try {
            long time = Long.parseLong(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
            return sdf.format(new Date(time));
        } catch (Exception e) {
            return "Data inválida";
        }
    }
}