package io.github.florianraediker.customsplashes.client.util.splash;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.Calendar;

@OnlyIn(Dist.CLIENT)
public class ReducedCalendar {
    private final int[] fields;

    public ReducedCalendar() {
        fields = new int[Calendar.FIELD_COUNT];
        Arrays.fill(fields, -1);
    }

    public boolean isSet(int field) {
        return fields[field] != -1;
    }

    public void set(int field, int value) {
        fields[field] = value;
    }

    public int get(int field) {
        return fields[field];
    }
}