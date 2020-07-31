package io.github.florianraediker.customsplashes.client.util.splash;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class SplashStorage {
    protected final List<SplashList> subSplashLists = new ArrayList<>();
    protected int availableSplashesCount;
    protected List<SplashList> availableSplashSubLists;

    @Nullable
    public String getRandomSplash(Random rand) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        this.calculateAvailableSplashesCount(calendar);

        if (this.availableSplashesCount == 0)
            return null;
        int num = rand.nextInt(this.availableSplashesCount);
        for (SplashList subList : this.availableSplashSubLists) {
            int newNum = num - subList.availableSplashesCount;
            if (newNum < 0)
                return subList.getSplash(num);
            num = newNum;
        }
        throw new IllegalStateException("this should never be reached");
    }

    public int getAvailableSplashesCount() {
        return availableSplashesCount;
    }

    public void addSubList(SplashList subList) {
        this.subSplashLists.add(subList);
    }

    public void removeSubList(SplashList subList) {
        this.subSplashLists.remove(subList);
    }

    public boolean removeSplash(String splash) {
        boolean res = false;
        for (SplashList subSplashList : this.subSplashLists) {
            res = res || subSplashList.removeSplash(splash);
        }
        return res;
    }

    protected boolean calculateAvailableSplashesCount(Calendar currentDate) {
        this.availableSplashesCount = 0;
        List<SplashList> exclusiveLists = new ArrayList<>();
        for (SplashList subSplashList : this.subSplashLists) {
            if (subSplashList.calculateAvailableSplashesCount(currentDate)) {
                exclusiveLists.add(subSplashList);
            }
        }
        boolean hasExclusiveLists;
        if (!exclusiveLists.isEmpty()) {
            this.availableSplashSubLists = exclusiveLists;
            hasExclusiveLists = true;
        } else {
            this.availableSplashSubLists = subSplashLists;
            hasExclusiveLists = false;
        }
        for (SplashStorage splashSubList : this.availableSplashSubLists) {
            this.availableSplashesCount += splashSubList.availableSplashesCount;
        }
        return hasExclusiveLists;
    }

    protected String convertSplash(String splash) {
        return splash;
    }
}
