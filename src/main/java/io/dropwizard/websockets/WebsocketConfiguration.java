/**
 * The MIT License
 * Copyright (c) 2017 LivePerson, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.dropwizard.websockets;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.Duration;
import io.dropwizard.util.Size;
import io.dropwizard.validation.MinDuration;
import io.dropwizard.validation.MinSize;

public class WebsocketConfiguration {

    @MinDuration(0L)
    private Duration maxSessionIdleTimeout;

    @MinDuration(0L)
    private Duration asyncSendTimeout;

    @MinSize(1L)
    private Size maxBinaryMessageBufferSize;

    @MinSize(1L)
    private Size maxTextMessageBufferSize;

    @JsonProperty
    public Duration getMaxSessionIdleTimeout() {
        return maxSessionIdleTimeout;
    }

    @JsonProperty
    public void setMaxSessionIdleTimeout(Duration maxSessionIdleTimeout) {
        this.maxSessionIdleTimeout = maxSessionIdleTimeout;
    }

    @JsonProperty
    public Duration getAsyncSendTimeout() {
        return asyncSendTimeout;
    }

    @JsonProperty
    public void setAsyncSendTimeout(Duration asyncSendTimeout) {
        this.asyncSendTimeout = asyncSendTimeout;
    }

    @JsonProperty
    public Size getMaxBinaryMessageBufferSize() {
        return maxBinaryMessageBufferSize;
    }

    @JsonProperty
    public void setMaxBinaryMessageBufferSize(Size maxBinaryMessageBufferSize) {
        this.maxBinaryMessageBufferSize = maxBinaryMessageBufferSize;
    }

    @JsonProperty
    public Size getMaxTextMessageBufferSize() {
        return maxTextMessageBufferSize;
    }

    @JsonProperty
    public void setMaxTextMessageBufferSize(Size maxTextMessageBufferSize) {
        this.maxTextMessageBufferSize = maxTextMessageBufferSize;
    }

}
