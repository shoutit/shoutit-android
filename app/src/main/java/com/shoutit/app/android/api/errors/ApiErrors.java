package com.shoutit.app.android.api.errors;

import android.support.annotation.NonNull;

import java.util.List;

import javax.annotation.Nonnull;

public class ApiErrors {
    @Nonnull
    private final Error error;

    @Nonnull
    public Error getError() {
        return error;
    }

    public ApiErrors(@NonNull Error error) {
        this.error = error;
    }

    public class Error {
        private final int code;
        @Nonnull
        private final String message;
        @Nonnull
        private final String developerMessage;
        @Nonnull
        private final String requestId;
        @Nonnull
        private final List<SubError> errors;

        public Error(int code, @Nonnull String message, @Nonnull String developerMessage,
                     @Nonnull String requestId, @Nonnull List<SubError> errors) {
            this.code = code;
            this.message = message;
            this.developerMessage = developerMessage;
            this.requestId = requestId;
            this.errors = errors;
        }

        @Nonnull
        public String getMessage() {
            return message;
        }

        @Nonnull
        public List<SubError> getErrors() {
            return errors;
        }

        public int getCode() {
            return code;
        }

        @Nonnull
        public String getDeveloperMessage() {
            return developerMessage;
        }
    }

    public class SubError {
        @Nonnull
        private final String location;
        @Nonnull
        private final String locationType;
        @Nonnull
        private final String message;
        @Nonnull
        private final String reason;

        private SubError(@Nonnull String location, @Nonnull String locationType,
                         @Nonnull String message, @Nonnull String reason) {
            this.location = location;
            this.locationType = locationType;
            this.message = message;
            this.reason = reason;
        }

        @Nonnull
        public String getMessage() {
            return message;
        }
    }
}
