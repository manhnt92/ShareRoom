package com.manhnt.navigationdrawer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class Blur {

    public static Bitmap apply(Context context, Bitmap sentBitmap) {
        return fastBlur(context, sentBitmap, 12, false);
    }

	@SuppressLint("NewApi")
	public static Bitmap fastBlur(Context context, Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
        if (VERSION.SDK_INT > 16) {
            Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
            final RenderScript rs = RenderScript.create(context);
            final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(radius /* e.g. 3.f */);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmap);
            return bitmap;
        }
        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }
        if (radius < 1) {
            return (null);
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);
        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;
        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rSum, gSum, bSum, x, y, i, p, yp, yi, yw;
        int vMin[] = new int[Math.max(w, h)];
        int divSum = (div + 1) >> 1;
        divSum *= divSum;
        int dv[] = new int[256 * divSum];
        for (i = 0; i < 256 * divSum; i++) {
            dv[i] = (i / divSum);
        }
        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackPointer;
        int stackStart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int rOutSum, gOutSum, bOutSum;
        int rInSum, gInSum, bInSum;
        for (y = 0; y < h; y++) {
            rInSum = gInSum = bInSum = rOutSum = gOutSum = bOutSum = rSum = gSum = bSum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rSum += sir[0] * rbs;
                gSum += sir[1] * rbs;
                bSum += sir[2] * rbs;
                if (i > 0) {
                    rInSum += sir[0];
                    gInSum += sir[1];
                    bInSum += sir[2];
                } else {
                    rOutSum += sir[0];
                    gOutSum += sir[1];
                    bOutSum += sir[2];
                }
            }
            stackPointer = radius;
            for (x = 0; x < w; x++) {
                r[yi] = dv[rSum];
                g[yi] = dv[gSum];
                b[yi] = dv[bSum];
                rSum -= rOutSum;
                gSum -= gOutSum;
                bSum -= bOutSum;
                stackStart = stackPointer - radius + div;
                sir = stack[stackStart % div];
                rOutSum -= sir[0];
                gOutSum -= sir[1];
                bOutSum -= sir[2];
                if (y == 0) {
                    vMin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vMin[x]];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rInSum += sir[0];
                gInSum += sir[1];
                bInSum += sir[2];
                rSum += rInSum;
                gSum += gInSum;
                bSum += bInSum;
                stackPointer = (stackPointer + 1) % div;
                sir = stack[(stackPointer) % div];
                rOutSum += sir[0];
                gOutSum += sir[1];
                bOutSum += sir[2];
                rInSum -= sir[0];
                gInSum -= sir[1];
                bInSum -= sir[2];
                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rInSum = gInSum = bInSum = rOutSum = gOutSum = bOutSum = rSum = gSum = bSum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                sir = stack[i + radius];
                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];
                rbs = r1 - Math.abs(i);
                rSum += r[yi] * rbs;
                gSum += g[yi] * rbs;
                bSum += b[yi] * rbs;
                if (i > 0) {
                    rInSum += sir[0];
                    gInSum += sir[1];
                    bInSum += sir[2];
                } else {
                    rOutSum += sir[0];
                    gOutSum += sir[1];
                    bOutSum += sir[2];
                }
                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackPointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rSum] << 16) | (dv[gSum] << 8) | dv[bSum];
                rSum -= rOutSum;
                gSum -= gOutSum;
                bSum -= bOutSum;
                stackStart = stackPointer - radius + div;
                sir = stack[stackStart % div];
                rOutSum -= sir[0];
                gOutSum -= sir[1];
                bOutSum -= sir[2];
                if (x == 0) {
                    vMin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vMin[y];
                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];
                rInSum += sir[0];
                gInSum += sir[1];
                bInSum += sir[2];
                rSum += rInSum;
                gSum += gInSum;
                bSum += bInSum;
                stackPointer = (stackPointer + 1) % div;
                sir = stack[stackPointer];
                rOutSum += sir[0];
                gOutSum += sir[1];
                bOutSum += sir[2];
                rInSum -= sir[0];
                gInSum -= sir[1];
                bInSum -= sir[2];
                yi += w;
            }
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }
	
}
