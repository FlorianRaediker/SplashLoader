package io.github.florianraediker.customsplashes.client.util.splash;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.util.*;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class SplashList extends SplashStorage {
    private static int month2number(String month) {
        switch (month.toLowerCase()) {
            case "january":
            case "jan":
                return Calendar.JANUARY;
            case "february":
            case "feb":
                return Calendar.FEBRUARY;
            case "march":
            case "mar":
                return Calendar.MARCH;
            case "april":
            case "apr":
                return Calendar.APRIL;
            case "may":
                return Calendar.MAY;
            case "june":
            case "jun":
                return Calendar.JUNE;
            case "july":
            case "jul":
                return Calendar.JULY;
            case "august":
            case "aug":
                return Calendar.AUGUST;
            case "september":
            case "sep":
                return Calendar.SEPTEMBER;
            case "october":
            case "oct":
                return Calendar.OCTOBER;
            case "november":
            case "nov":
                return Calendar.NOVEMBER;
            case "december":
            case "dec":
                return Calendar.DECEMBER;
            default:
                return -1;
        }
    }
    private static ReducedCalendar parseDate(JsonObject object) {
        ReducedCalendar calendar = new ReducedCalendar();
        if (object.has("era"))
            calendar.set(Calendar.ERA, JSONUtils.getInt(object, "era"));
        if (object.has("year"))
            calendar.set(Calendar.YEAR, JSONUtils.getInt(object, "year"));
        if (object.has("month")) {
            JsonElement month = object.get("month");
            int m;
            if (month.isJsonPrimitive()) {
                m = month2number(JSONUtils.getString(month, "month"));
                if (m == -1)
                    throw new JsonSyntaxException("Unknown month '" + month + "'");
            } else {
                m = JSONUtils.getInt(month, "month");
            }
            calendar.set(Calendar.MONTH, m);
        }
        if (object.has("week_of_year"))
            calendar.set(Calendar.WEEK_OF_YEAR, JSONUtils.getInt(object, "week_of_year"));
        if (object.has("week_of_month"))
            calendar.set(Calendar.WEEK_OF_MONTH, JSONUtils.getInt(object, "week_of_month"));
        if (object.has("date"))
            calendar.set(Calendar.DATE, JSONUtils.getInt(object, "date"));
        if (object.has("day_of_month"))
            calendar.set(Calendar.DAY_OF_MONTH, JSONUtils.getInt(object, "day_of_month"));
        if (object.has("day_of_year"))
            calendar.set(Calendar.DAY_OF_YEAR, JSONUtils.getInt(object, "day_of_year"));
        if (object.has("day_of_week"))
            calendar.set(Calendar.DAY_OF_WEEK, JSONUtils.getInt(object, "day_of_week"));
        if (object.has("day_of_week_in_month"))
            calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, JSONUtils.getInt(object, "day_of_week_in_month"));
        if (object.has("am_pm"))
            calendar.set(Calendar.AM_PM, JSONUtils.getInt(object, "am_pm"));
        if (object.has("hour"))
            calendar.set(Calendar.HOUR, JSONUtils.getInt(object, "hour"));
        if (object.has("hour_of_day"))
            calendar.set(Calendar.HOUR_OF_DAY, JSONUtils.getInt(object, "HOUR_OF_DAY"));
        if (object.has("minute"))
            calendar.set(Calendar.MINUTE, JSONUtils.getInt(object, "minute"));
        if (object.has("second"))
            calendar.set(Calendar.SECOND, JSONUtils.getInt(object, "SECOND"));
        if (object.has("millisecond"))
            calendar.set(Calendar.MILLISECOND, JSONUtils.getInt(object, "millisecond"));
        if (object.has("zone_offset"))
            calendar.set(Calendar.ZONE_OFFSET, JSONUtils.getInt(object, "zone_offset"));
        if (object.has("dst_offset"))
            calendar.set(Calendar.DST_OFFSET, JSONUtils.getInt(object, "dst_offset"));
        return calendar;
    }


    public static SplashList fromJSON(String modId, JsonObject object) throws JsonSyntaxException {
        return fromJSON(modId, object, false, false);
    }
    private static SplashList fromJSON(String modId, JsonObject object, boolean doTranslateIn, boolean isTimeExclusiveIn) {
        boolean doTranslate = JSONUtils.getBoolean(object, "translated", doTranslateIn);
        boolean isTimeExclusive = JSONUtils.getBoolean(object, "is_exclusive", isTimeExclusiveIn);
        ReducedCalendar requiredTime = null;
        ReducedCalendar requiredTimeFrom = null;
        ReducedCalendar requiredTimeTo = null;
        if (object.has("date"))
            requiredTime = parseDate(JSONUtils.getJsonObject(object, "date"));
        if (object.has("date_from") || object.has("date_to")) {
            if (requiredTime != null)
                throw new JsonSyntaxException("Expected only 'date' or 'date_from'/'date_to' keys, not both");
            requiredTimeFrom = parseDate(JSONUtils.getJsonObject(object, "date_from"));
            requiredTimeTo = parseDate(JSONUtils.getJsonObject(object, "date_to"));
        }

        List<String> splashes = new ArrayList<>();
        List<SplashList> subSplashLists = new ArrayList<>();
        if (object.has("splashes")) {
            JSONUtils.getJsonArray(object, "splashes").forEach(e -> {
                if (e.isJsonObject()) {
                    subSplashLists.add(SplashList.fromJSON(modId, e.getAsJsonObject(), doTranslate, isTimeExclusive));
                } else {
                    String splash = JSONUtils.getString(e, "splash");
                    if (splash.hashCode() != 125780783)
                        splashes.add(e.getAsString());
                }
            });
        } else if (object.has("splash")) {
            splashes.add(JSONUtils.getString(object, "splash"));
        }
        return new SplashList(modId, doTranslate, requiredTime, requiredTimeFrom, requiredTimeTo, isTimeExclusive, splashes, subSplashLists);
    }

    public static SplashList fromReader(String modId, BufferedReader reader) {
        List<String> splashes = reader.lines().map(String::trim).filter((p_215277_0_) -> p_215277_0_.hashCode() != 125780783).collect(Collectors.toList());
        return new SplashList(modId, false, null, null, null, false, splashes, null);
    }

    protected final String modId;
    protected final boolean doTranslate;
    @Nullable
    protected final ReducedCalendar requiredTime;
    @Nullable
    protected final ReducedCalendar requiredTimeFrom;
    @Nullable
    protected final ReducedCalendar requiredTimeTo;
    protected final boolean isTimeExclusive;
    protected final List<String> splashes;
    protected boolean excludeSplashes;

    protected SplashList(String modId, boolean doTranslate,
                         @Nullable ReducedCalendar requiredTime, @Nullable ReducedCalendar requiredTimeFrom, @Nullable ReducedCalendar requiredTimeTo, boolean isTimeExclusive,
                         List<String> splashes, @Nullable Collection<SplashList> subSplashLists) {
        this.modId = modId;
        this.doTranslate = doTranslate;
        this.requiredTime = requiredTime;
        this.requiredTimeFrom = requiredTimeFrom;
        this.requiredTimeTo = requiredTimeTo;
        this.isTimeExclusive = isTimeExclusive;
        this.splashes = splashes;
        if (subSplashLists != null)
            this.subSplashLists.addAll(subSplashLists);
    }

    public SplashList(String modId, boolean doTranslate,
                      @Nullable ReducedCalendar requiredTime, boolean isTimeExclusive,
                      List<String> splashes) {
        this(modId, doTranslate, requiredTime, null, null, isTimeExclusive, splashes, null);
    }

    @SuppressWarnings("unused")
    public SplashList(String modId, boolean doTranslate,
                      @Nullable ReducedCalendar requiredTimeFrom, @Nullable ReducedCalendar requiredTimeTo, boolean isTimeExclusive,
                      List<String> splashes) {
        this(modId, doTranslate, requiredTimeFrom, requiredTimeTo, null, isTimeExclusive, splashes, null);
    }

    public SplashList(String modId) {
        this(modId, false, null, null, null, false, Collections.emptyList(), null);
    }

    public List<String> getSplashes() {
        return splashes;
    }

    public void addSplashes(Collection<String> splashes) {
        this.splashes.addAll(splashes);
    }

    @Override
    public boolean removeSplash(String splash) {
        return this.splashes.removeIf(splash::equals) || super.removeSplash(splash);
    }

    @Override
    protected boolean calculateAvailableSplashesCount(Calendar currentDate) {
        boolean res = false;
        if (this.requiredTime != null) {
            // check whether time is correct
            boolean isCorrect = true;
            for (int i = 0; i < Calendar.FIELD_COUNT; i++) {
                //noinspection MagicConstant
                if (this.requiredTime.isSet(i) && this.requiredTime.get(i) != currentDate.get(i)) {
                    isCorrect = false;
                    break;
                }
            }
            res = this.isTimeExclusive;
            if (!isCorrect) {
                this.availableSplashesCount = 0;
                return false;
            }
        }
        boolean hasExclusiveSubLists = super.calculateAvailableSplashesCount(currentDate);
        if (!res && hasExclusiveSubLists) {
            this.excludeSplashes = true;
        } else {
            this.excludeSplashes = false;
            this.availableSplashesCount += this.splashes.size();
        }
        res = res || hasExclusiveSubLists;
        return res;
    }

    @Override
    protected String convertSplash(String splash) {
        if (doTranslate) {
            String key = "splash." + this.modId + "." + splash;
            if (I18n.hasKey(key)) {
                return I18n.format(key);
            }
        }
        return super.convertSplash(splash);
    }

    protected String getSplash(int num) {
        int newNum;
        if (!excludeSplashes) {
            newNum = num - this.splashes.size();
            if (newNum < 0)
                return this.splashes.get(num);
            num = newNum;
        }
        for (SplashList subList : this.availableSplashSubLists) {
            newNum = num - subList.availableSplashesCount;
            if (newNum < 0)
                return subList.getSplash(num);
            num = newNum;
        }
        throw new IllegalStateException("this should never be reached");
    }
}
