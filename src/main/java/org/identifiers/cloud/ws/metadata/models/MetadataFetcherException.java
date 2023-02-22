package org.identifiers.cloud.ws.metadata.models;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-02-08 11:59
 * ---
 */
public class MetadataFetcherException extends RuntimeException {
    public enum ErrorCode {
        METADATA_NOT_FOUND(0, "Metadata NOT FOUND"),
        INTERNAL_ERROR(1, "An internal error occurred while fetching metadata"),
        METADATA_INVALID(2, "Invalid Metadata found");

        private final int value;
        private final String errorDescription;

        ErrorCode(int value, String errorDescription) {
            this.value = value;
            this.errorDescription = errorDescription;
        }

        public int getValue() {
            return value;
        }

        public String getErrorDescription() {
            return errorDescription;
        }
    }

    private ErrorCode errorCode;

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public MetadataFetcherException(String message) {
        super(message);
    }

    public MetadataFetcherException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
