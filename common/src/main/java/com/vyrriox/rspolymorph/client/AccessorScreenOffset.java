package com.vyrriox.rspolymorph.client;

/**
 * Duck-type interface implemented (via mixin) by {@code AbstractContainerScreen} to expose its
 * protected {@code leftPos}/{@code topPos} so the popup can be anchored in screen space without
 * subclassing. See {@code MixinAbstractContainerScreenOffset}.
 *
 * Author: vyrriox
 */
public interface AccessorScreenOffset {
    int rspolymorph$leftPos();
    int rspolymorph$topPos();
}
