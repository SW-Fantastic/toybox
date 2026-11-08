package org.swdc.toybox.core;

public record KeyShortcut(int[] codes, NativeKeyTriggeredListener listener) {

    public boolean isMatched(int[] codes) {
        if (codes.length != this.codes.length) {
            return false;
        }
        for (int idx = 0 ; idx < this.codes.length; idx ++) {
            // 注意，传入的Codes是倒序的，所以需要倒着比较，
            // 0 对应 length - 1， 1 对应 length - 2这种。
            int maxPos = codes.length - 1;
            int revPos = maxPos - idx;
            if (codes[revPos] != this.codes[idx]) {
                return false;
            }
        }
        return true;
    }

}
