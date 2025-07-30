/*******************************************************************************
 * Copyright 2016 stfalcon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.stfalcon.chatkit.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for formatting dates in messages
 */
public class DateFormatter {

    public interface Formatter {
        String format(Date date);
    }

    public enum Template {
        TIME("HH:mm"),
        STRING_DAY_MONTH_YEAR("d MMM yyyy"),
        STRING_DAY_MONTH("d MMM");

        private String template;

        Template(String template) {
            this.template = template;
        }

        public String getTemplate() {
            return template;
        }
        
        public String get() {
            return template;
        }
    }

    public static String format(Date date, Template template) {
        SimpleDateFormat formatter = new SimpleDateFormat(template.getTemplate(), Locale.getDefault());
        return formatter.format(date);
    }

    public static String format(Date date, String template) {
        SimpleDateFormat formatter = new SimpleDateFormat(template, Locale.getDefault());
        return formatter.format(date);
    }
    
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    
    public static boolean isToday(Date date) {
        return isSameDay(date, new Date());
    }
    
    public static boolean isYesterday(Date date) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        return isSameDay(date, yesterday.getTime());
    }
    
    public static boolean isCurrentYear(Date date) {
        if (date == null) {
            return false;
        }
        
        Calendar dateCalendar = Calendar.getInstance();
        Calendar currentCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);
        
        return dateCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR);
    }
}