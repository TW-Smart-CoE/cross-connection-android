/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.utils;

import java.nio.ByteOrder;

class ByteUtils {
    public static void putShort(byte[] bytes, int index, short value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            bytes[index] = (byte) (value >> 8);
            bytes[index + 1] = (byte) (value);
        } else {
            bytes[index] = (byte) (value);
            bytes[index + 1] = (byte) (value >> 8);
        }
    }

    public static short getShort(byte[] bytes, int index) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            return (short) (((bytes[index] << 8) | bytes[index + 1] & 0xff));
        } else {
            return (short) (((bytes[index + 1] << 8) | bytes[index] & 0xff));
        }
    }

    public static void putInt(byte[] bytes, int index, int value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            bytes[index] = (byte) (value >> 24);
            bytes[index + 1] = (byte) (value >> 16);
            bytes[index + 2] = (byte) (value >> 8);
            bytes[index + 3] = (byte) (value);
        } else {
            bytes[index] = (byte) (value);
            bytes[index + 1] = (byte) (value >> 8);
            bytes[index + 2] = (byte) (value >> 16);
            bytes[index + 3] = (byte) (value >> 24);
        }
    }

    public static int getInt(byte[] bytes, int index) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            return (((bytes[index] & 0xff) << 24)
                    | ((bytes[index + 1] & 0xff) << 16)
                    | ((bytes[index + 2] & 0xff) << 8)
                    | ((bytes[index + 3] & 0xff)));
        } else {
            return (((bytes[index] & 0xff))
                    | ((bytes[index + 1] & 0xff) << 8)
                    | ((bytes[index + 2] & 0xff) << 16)
                    | ((bytes[index + 3] & 0xff)) << 24);
        }
    }
}
