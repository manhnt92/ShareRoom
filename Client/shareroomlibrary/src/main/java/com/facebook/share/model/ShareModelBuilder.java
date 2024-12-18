/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 *
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.facebook.share.model;

import android.os.Parcel;

import com.facebook.share.ShareBuilder;

/**
 * Interface for builders related to sharing.
 * @param <P> The model protocol to be built.
 * @param <E> The concrete builder class.
 */
@SuppressWarnings("rawtypes")
public interface ShareModelBuilder<P extends ShareModel, E extends ShareModelBuilder>
        extends ShareBuilder<P, E> {
    /**
     * Reads the values from a ShareModel into the builder.
     * @param model The source ShareModel
     * @return The builder.
     */
    E readFrom(P model);

    /**
     * Reads the values from a parcel into the builder.  The parcel must have packaged an instance
     * of the ShareModel that came from the same type of builder.
     * @param parcel The Parcel that contains the ShareModel.
     * @return The builder.
     */
    E readFrom(Parcel parcel);
}
